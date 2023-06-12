package site.minnan.miao.infrastructure.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.minnan.miao.domain.entity.ContributionRecord;
import site.minnan.miao.domain.vo.BattleAnalysisSecond;
import site.minnan.miao.domain.vo.TimeDuration;

import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PicParseUtil {

    private static final String appKey = "7EnShXnREzlifPKwI5Yrqmpl";
    private static final String appScreet = "7olyleriGGRVlPY2uZumoT2Xu13e3WLf";
    private static final String tokenUrl = StrUtil.format("https://aip.baidubce.com/oauth/2" +
            ".0/token?client_id={}&client_secret={}&grant_type=client_credentials", appKey, appScreet);
    private static final String ocr_url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general?access_token=";

    @Autowired
    RedisUtil redisUtil;

    public List<ContributionRecord> parsePic(String picUrl) {
        String token = getToken();
        String payload = "url=" + picUrl;
        String url = ocr_url + token;
        HttpResponse ocrResponse = HttpUtil.createPost(url)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .body(payload)
                .execute();
        String ocrResponseString = ocrResponse.body();
        JSONObject json = JSONUtil.parseObj(ocrResponseString);

        JSONArray wordsResultArray = json.getJSONArray("words_result");

        List<List<JSONObject>> memberDataList = new ArrayList<>();
        List<JSONObject> memberDataTemp = new ArrayList<>();
        memberDataList.add(memberDataTemp);

        for (int i = 0; i < wordsResultArray.size(); i++) {
            JSONObject wordsResult = wordsResultArray.getJSONObject(i);
            Integer locationTop = wordsResult.getJSONObject("location").getInt("top");
            if (memberDataTemp.size() > 0) {
                Integer currentLineTop = memberDataTemp.get(0).getJSONObject("location").getInt("top");
                if (Math.abs(locationTop - currentLineTop) > 10) {
                    memberDataTemp = new ArrayList<>();
                    memberDataList.add(memberDataTemp);
                }
            }
            memberDataTemp.add(wordsResult);
        }

        Function<String, Integer> getNumber = s -> {
            String number = ReUtil.replaceAll(s, "\\D", "");
            return StrUtil.isBlank(number) ? 0 : Integer.parseInt(ReUtil.replaceAll(s, "\\D", ""));
        };

        List<ContributionRecord> recordList = memberDataList.stream()
                .map(l -> {
                    int length = l.size();
                    return ContributionRecord.builder()
                            .name(StrUtil.sub(l.get(0).getStr("words"), 0, 12))
                            .culvert(getNumber.apply(l.get(length - 2).getStr("words")))
                            .flagRace(getNumber.apply(l.get(length - 1).getStr("words")))
                            .build();
                })
                .collect(Collectors.toList());
        return recordList;
    }


    private String getToken() {
        String token = "";
//        String token = ((String) redisUtil.getValue("pic_token"));
        if (StrUtil.isNotBlank(token)) {
            return token;
        }
        String responseString = HttpUtil.get(tokenUrl);
        JSONObject responseJson = JSONUtil.parseObj(responseString);
        token = responseJson.getStr("access_token");
//        redisUtil.valueSet("pic_token", token, Duration.ofMinutes(30));
        return token;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        PicParseUtil picParseUtil = new PicParseUtil();
        String token = picParseUtil.getToken();


        List<BattleAnalysisSecond> secondList = new ArrayList<>();
        for (int sec = 11; sec < 198; sec++) {
            System.out.println("开始解析第" + (sec - 10) + "秒数据");
            String filePath = "F:\\go-cqhttp\\loop_img\\Mercedes\\" + sec + ".jpg";
            if (!FileUtil.exist(filePath)) {
                continue;
            }
            BufferedImage image = ImgUtil.read(filePath);
            BufferedImage subimage = image.getSubimage(463, 271, 185, 89);
//        String time = DateTime.now().toString("HH-mm-ss");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos);
            ImgUtil.write(subimage, ImgUtil.IMAGE_TYPE_JPG, mcios);
            byte[] subBytes = baos.toByteArray();

            String encode = Base64Util.encode(subBytes);
            String imgParam = URLEncoder.encode(encode, "UTF-8");
            String payload = "image=" + imgParam;

            String url = ocr_url + token;
            HttpResponse ocrResponse = HttpUtil.createPost(url)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .body(payload)
                    .execute();
            String ocrResponseString = ocrResponse.body();
            JSONObject json = JSONUtil.parseObj(ocrResponseString);

            List<String> lines = new ArrayList<>();
            JSONArray wordsResultArray = json.getJSONArray("words_result");
            Integer lastTop = null;
            StringBuilder lineBuilder = new StringBuilder();
            for (int i = 0; i < wordsResultArray.size(); i++) {
                JSONObject wordsResult = wordsResultArray.getJSONObject(i);
                Integer locationTop = wordsResult.getJSONObject("location").getInt("top");
                if (lastTop == null) {
                    lastTop = locationTop;
                    lineBuilder.append(wordsResult.getStr("words")).append(" ");
                    continue;
                }
                if (Math.abs(locationTop - lastTop) > 10) {
                    lines.add(lineBuilder.toString());
                    lineBuilder = new StringBuilder();
                }
                lineBuilder.append(wordsResult.getStr("words")).append(" ");
                lastTop = locationTop;
            }


            lines.add(lineBuilder.toString());

            String timeStr = ReUtil.replaceAll(lines.get(0), "\\D", "");
            String damageStr = ReUtil.replaceAll(lines.get(1), "\\D", "");

            TimeDuration duration = new TimeDuration(timeStr);
            BattleAnalysisSecond analysisSecond = new BattleAnalysisSecond(duration,
                    StrUtil.isBlank(damageStr) ? 0L : Long.parseLong(damageStr));
            System.out.println("第" + (sec - 10) + "秒解析结果：" + analysisSecond);
            secondList.add(analysisSecond);
            Thread.sleep(1000);
        }


        secondList.sort(Comparator.comparing(BattleAnalysisSecond::getTime));
        secondList.get(0).setDamageDiffer(0L);
        for (int i = 1; i < secondList.size(); i++) {
            BattleAnalysisSecond lastSecond = secondList.get(i - 1);
            BattleAnalysisSecond thisSecond = secondList.get(i);
            int timeDiffer = thisSecond.getTime() - lastSecond.getTime();
            long damageDiffer = (thisSecond.getDamage() - lastSecond.getDamage()) / (timeDiffer == 0 ? 1 : timeDiffer);
            thisSecond.setDamageDiffer(damageDiffer);
        }
        List<Integer> timeList = secondList.stream().map(e -> e.getTime()).collect(Collectors.toList());
        List<Long> damageList = secondList.stream().map(e -> (long) e.getDamage() / 1000).collect(Collectors.toList());
        List<Long> differList =
                secondList.stream().map(e -> (long) e.getDamageDiffer() / 1000).collect(Collectors.toList());
        HashMap<String, List<?>> map = new HashMap<>();
        map.put("time", timeList);
        map.put("damage", damageList);
        map.put("differ", differList);
        System.out.println(JSONUtil.toJsonPrettyStr(map));
    }
}
