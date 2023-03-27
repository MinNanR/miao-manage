package site.minnan.miao.application.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.minnan.miao.application.service.RecordService;
import site.minnan.miao.domain.entity.ContributionRecord;
import site.minnan.miao.domain.entity.ImportRecord;
import site.minnan.miao.domain.entity.ImportRecordPage;
import site.minnan.miao.domain.entity.NickCorrect;
import site.minnan.miao.domain.repository.ContributionRecordMapper;
import site.minnan.miao.domain.repository.ImportRecordMapper;
import site.minnan.miao.domain.repository.ImportRecordPageMapper;
import site.minnan.miao.domain.repository.NickCorrectMapper;
import site.minnan.miao.domain.vo.ImportRecordListVO;
import site.minnan.miao.domain.vo.ListQueryVO;
import site.minnan.miao.infrastructure.exception.EntityNotExistException;
import site.minnan.miao.infrastructure.utils.JwtUtil;
import site.minnan.miao.infrastructure.utils.PicParseUtil;
import site.minnan.miao.userinterface.dto.GetImportRecordListDTO;
import site.minnan.miao.userinterface.dto.VerifyProtectDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecordServiceImpl implements RecordService {

    @Autowired
    private ImportRecordMapper importRecordMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NickCorrectMapper nickCorrectMapper;

    @Autowired
    private ImportRecordPageMapper importRecordPageMapper;

    @Autowired
    private ContributionRecordMapper contributionRecordMapper;


    @Value("${aliyun.baseUrl}")
    private String baseUrl;

    @Value("${aliyun.bucketName}")
    private String bucketName;

    @Value("${aliyun.folder}")
    private String folder;

    @Autowired
    private OSS oss;

    @Autowired
    private PicParseUtil picParseUtil;

    /**
     * 查询导入记录
     *
     * @param dto
     * @return
     */
    @Override
    public ListQueryVO<ImportRecordListVO> getImportRecordList(GetImportRecordListDTO dto) {
        LambdaQueryWrapper<ImportRecord> queryWrapper = Wrappers.<ImportRecord>lambdaQuery()
                .eq(ImportRecord::getGuildId, dto.getGuildId())
                .orderByDesc(ImportRecord::getWeekStartDate);
        Page<ImportRecord> page = new Page<>(dto.getPageIndex(), dto.getPageSize());
        IPage<ImportRecord> result = importRecordMapper.selectPage(page, queryWrapper);
        List<ImportRecordListVO> list =
                result.getRecords().stream().map(ImportRecordListVO::assemble).collect(Collectors.toList());
        return new ListQueryVO<>(list, result.getTotal());
    }

    /**
     * 验证保护码
     *
     * @param dto
     * @return
     */
    @Override
    public String verifyProtectCode(VerifyProtectDTO dto, HttpServletResponse response) {
        ImportRecord importRecord = importRecordMapper.selectById(dto.getId());
        if (importRecord == null) {
            throw new EntityNotExistException("记录不存在");
        }
        String protectCode = importRecord.getProtectCode();
        boolean verified = protectCode.equals(dto.getProtectCode());
        if (!verified) {
            return null;
        }
        return jwtUtil.generateToken(importRecord);
    }

    @Override
    public ImportRecord validateToken(String token) throws Exception {
        String idStr = jwtUtil.getSubjectFromtoken(token);
        int id = Integer.parseInt(idStr);
        ImportRecord importRecord = importRecordMapper.selectById(id);
        Boolean validated = jwtUtil.validateToken(token, importRecord);
        if (!validated) {
            throw new Exception("非法token");
        }
        return importRecord;
    }

    /**
     * 处理上传文件
     *
     * @param file
     * @param record
     */
    @Override
    @Async
    @SneakyThrows
    public void handleUploadFile(MultipartFile file, ImportRecord record) {
        InputStream inputStream = file.getInputStream();
        String today = DateUtil.today().replaceAll("-", "");
        String originalFilename = file.getOriginalFilename();
        String randomName = RandomUtil.randomString(10);
        String extension = originalFilename == null ? "png" :
                originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        String ossKey = StrUtil.format("{}/{}/{}.{}", folder, today, randomName, extension);
        oss.putObject(bucketName, ossKey, inputStream);
        inputStream.close();
        log.info("上传文件：" + ossKey);
        String ossUrl = StrUtil.format("{}/{}", baseUrl, ossKey);

        List<ContributionRecord> newRecordList = picParseUtil.parsePic(ossUrl);
        Map<String, ContributionRecord> newNameMap =
                newRecordList.stream().collect(Collectors.toMap(e -> e.getName().toLowerCase(), e -> e));

        List<NickCorrect> allCorrectRecord = nickCorrectMapper.selectList(null);
        allCorrectRecord.stream()
                .filter(e -> newNameMap.containsKey(e.getOriginal()))
                .forEach(e -> {
                    String original = e.getOriginal();
                    newNameMap.get(original).correct(e);
                });

        Integer recordId = record.getId();
        Integer guildId = record.getGuildId();
        String guildName = record.getGuildName();
        String now = DateTime.now().toString("yyyy-MM-dd");

        String lastMonday = record.getWeekStartDate();
        String lastSunday = DateUtil.endOfWeek(DateUtil.parseDate(lastMonday)).toString("yyyy-mm-dd");


        ImportRecordPage recordPage = ImportRecordPage.builder()
                .importRecordId(recordId)
                .guildId(guildId)
                .guildName(guildName)
                .createTime(now)
                .picUrl(ossUrl)
                .build();
        importRecordPageMapper.insert(recordPage);


        String towWeeksAgo =
                DateUtil.beginOfWeek(DateUtil.offsetWeek(DateUtil.parseDate(lastMonday), -1)).toString("yyyy-mm-dd");

        LambdaQueryWrapper<ImportRecord> lastWeekImportQuery = Wrappers.<ImportRecord>lambdaQuery()
                .eq(ImportRecord::getWeekStartDate, towWeeksAgo);
        ImportRecord lastWeekRecord = importRecordMapper.selectOne(lastWeekImportQuery);

        LambdaQueryWrapper<ContributionRecord> lastWeekRecordQuery = Wrappers.<ContributionRecord>lambdaQuery()
                .eq(ContributionRecord::getImportRecordId, lastWeekRecord.getId());
        List<ContributionRecord> lastWeekRecordList = contributionRecordMapper.selectList(lastWeekRecordQuery);
        List<String> lastWeekNameList = lastWeekRecordList.stream().map(e -> e.getName()).collect(Collectors.toList());

        Integer recordPageId = recordPage.getId();
        newRecordList.stream()
                .peek(e -> {
                    e.setStatus(1);
                    e.setImportRecordPageId(recordPageId);
                })
                .filter(e -> !lastWeekNameList.contains(e.getName()))
                .forEach(e -> {
                    e.setStatus(0);
                    e.setCorrected(0);
                });




    }

    public static void main(String[] args) {
        System.out.println(DateUtil.today());
    }
}
