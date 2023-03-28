package site.minnan.miao.infrastructure.utils;

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

import java.time.Duration;
import java.util.ArrayList;
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

        Function<String, Integer> getNumber = s -> Integer.parseInt(ReUtil.replaceAll(s, "\\D", ""));

        List<ContributionRecord> recordList = memberDataList.stream()
                .map(l -> {
                    int length = l.size();
                    return ContributionRecord.builder()
                            .name(StrUtil.sub(l.get(0).getStr("words"), 0 ,12))
                            .culvert(getNumber.apply(l.get(length - 2).getStr("words")))
                            .flagRace(getNumber.apply(l.get(length - 1).getStr("words")))
                            .build();
                })
                .collect(Collectors.toList());
        return recordList;
    }


    private String getToken() {
        String token = ((String) redisUtil.getValue("pic_token"));
        if (StrUtil.isNotBlank(token)) {
            return token;
        }
        String responseString = HttpUtil.get(tokenUrl);
        JSONObject responseJson = JSONUtil.parseObj(responseString);
        token = responseJson.getStr("access_token");
        redisUtil.valueSet("pic_token", token, Duration.ofMinutes(30));
        return token;
    }

    public static void main(String[] args) {
        String s = "1,852";
        int number = Integer.parseInt(ReUtil.replaceAll(s, "\\D", ""));
        System.out.println(number);
    }
}
