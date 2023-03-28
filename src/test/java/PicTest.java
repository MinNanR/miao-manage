import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import site.minnan.miao.MiaoManageApplication;
import site.minnan.miao.domain.entity.ContributionRecord;
import site.minnan.miao.infrastructure.utils.PicParseUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest(classes = MiaoManageApplication.class)
@Slf4j
public class PicTest {

    @Value("${aliyun.baseUrl}")
    private String baseUrl;

    @Value("${aliyun.bucketName}")
    private String bucketName;

    @Value("${aliyun.folder}")
    private String folder;

    @Autowired
    PicParseUtil picParseUtil;

    @Autowired
    OSS oss;

    @Test
    public void parse() throws IOException {
//        File file = new File("F:\\Minnan\\miao-manage\\202230313\\1.png");
//        InputStream inputStream = new FileInputStream(file);
//        String today = DateUtil.today().replaceAll("-", "");
//        String originalFilename = file.getName();
//        String randomName = RandomUtil.randomString(10);
//        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
//        String ossKey = StrUtil.format("{}/{}/{}.{}", folder, today, randomName, extension);
//        oss.putObject(bucketName, ossKey, inputStream);
//        inputStream.close();
//        log.info("上传文件：" + ossKey);
        String ossKey = "miao/20230328/n7b3p95cqa.png";
        String ossUrl = StrUtil.format("{}/{}", baseUrl, ossKey);

        List<ContributionRecord> newRecordList = picParseUtil.parsePic(ossUrl);
        Map<String, ContributionRecord> newNameMap =
                newRecordList.stream().collect(Collectors.toMap(e -> e.getName().toLowerCase(), e -> e));

        Console.log(newNameMap);
    }
}
