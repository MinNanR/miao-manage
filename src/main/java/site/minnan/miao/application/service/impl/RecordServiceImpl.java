package site.minnan.miao.application.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.OSS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.minnan.miao.application.service.RecordService;
import site.minnan.miao.domain.entity.*;
import site.minnan.miao.domain.repository.*;
import site.minnan.miao.domain.vo.*;
import site.minnan.miao.infrastructure.exception.EntityNotExistException;
import site.minnan.miao.infrastructure.exception.InvalidOperationException;
import site.minnan.miao.infrastructure.utils.JwtUtil;
import site.minnan.miao.infrastructure.utils.PicParseUtil;
import site.minnan.miao.userinterface.dto.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
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

    @Autowired
    private GuildMapper guildMapper;


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
        String salt = DateTime.now().toString("dd");
        String encodedProtectCode = DigestUtil.md5Hex(protectCode + salt);
        boolean verified = encodedProtectCode.equals(dto.getProtectCode());
        if (!verified) {
            return null;
        }
        return jwtUtil.generateToken(importRecord);
    }

    @Override
    public ImportRecord validateToken(String token) {
        String idStr = jwtUtil.getSubjectFromtoken(token);
        int id = Integer.parseInt(idStr);
        ImportRecord importRecord = importRecordMapper.selectById(id);
        Boolean validated = jwtUtil.validateToken(token, importRecord);
        if (!validated) {
            throw new InvalidOperationException("非法token");
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
    public String handleUploadFile(MultipartFile file, ImportRecord record) throws IOException {
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
                .filter(e -> newNameMap.containsKey(e.getOriginal().toLowerCase()))
                .forEach(e -> {
                    String original = e.getOriginal();
                    newNameMap.get(original.toLowerCase()).correct(e);
                });

        Integer recordId = record.getId();
        Integer guildId = record.getGuildId();
        String guildName = record.getGuildName();
        String now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");

        String lastMonday = record.getWeekStartDate();

        ImportRecordPage recordPage = ImportRecordPage.builder()
                .importRecordId(recordId)
                .guildId(guildId)
                .guildName(guildName)
                .createTime(now)
                .picUrl(ossUrl)
                .build();
        importRecordPageMapper.insert(recordPage);

        String towWeeksAgo =
                DateUtil.beginOfWeek(DateUtil.offsetWeek(DateUtil.parseDate(lastMonday), -1)).toString("yyyy-MM-dd");

        LambdaQueryWrapper<ImportRecord> lastWeekImportQuery = Wrappers.<ImportRecord>lambdaQuery()
                .eq(ImportRecord::getWeekStartDate, towWeeksAgo);
        ImportRecord lastWeekRecord = importRecordMapper.selectOne(lastWeekImportQuery);

        LambdaQueryWrapper<ContributionRecord> lastWeekRecordQuery = Wrappers.<ContributionRecord>lambdaQuery()
                .eq(ContributionRecord::getImportRecordId, lastWeekRecord.getId());
        List<ContributionRecord> lastWeekRecordList = contributionRecordMapper.selectList(lastWeekRecordQuery);
        List<String> lastWeekNameList =
                lastWeekRecordList.stream().map(e -> e.getName().toLowerCase()).collect(Collectors.toList());

        Integer recordPageId = recordPage.getId();
        long errorCount = newRecordList.stream()
                .peek(e -> {
                    e.setStatus(1);
                    e.setImportRecordPageId(recordPageId);
                    e.setImportRecordId(recordId);
                    e.setGuildId(guildId);
                    e.setGuildName(guildName);
                    e.setCreateTime(now);
                })
                .filter(e -> !lastWeekNameList.contains(e.getName().toLowerCase()))
                .peek(e -> {
                    e.setStatus(2);
                    e.setCorrected(0);
                })
                .count();

        newRecordList.forEach(e -> contributionRecordMapper.insert(e));

        record.addPage(newRecordList.size());
        importRecordMapper.updateById(record);

        return StrUtil.format("本次解析数据{}条，异常数据{}条", newRecordList.size(), errorCount);
    }

    /**
     * 查询导入页数据（根据导入记录id）
     *
     * @param dto
     * @return
     */
    @Override
    public ListQueryVO<RecordPageVO> getRecordPageList(DetailsQueryDTO dto) {
        LambdaQueryWrapper<ImportRecordPage> listQuery = Wrappers.<ImportRecordPage>lambdaQuery()
                .eq(ImportRecordPage::getImportRecordId, dto.getId());
        List<ImportRecordPage> pageList = importRecordPageMapper.selectList(listQuery);
        List<RecordPageVO> vos = pageList.stream().map(RecordPageVO::assemble).collect(Collectors.toList());
        return new ListQueryVO<>(vos, vos.size());
    }

    /**
     * 查询识别结果
     *
     * @param dto
     * @return
     */
    @Override
    public ListQueryVO<ContributionVO> getContributionList(DetailsQueryDTO dto) {
        LambdaQueryWrapper<ContributionRecord> listQuery = Wrappers.<ContributionRecord>lambdaQuery()
                .eq(ContributionRecord::getImportRecordPageId, dto.getId());
        List<ContributionRecord> pageList = contributionRecordMapper.selectList(listQuery);
        List<ContributionVO> vos = pageList.stream().map(ContributionVO::assemble).collect(Collectors.toList());
        return new ListQueryVO<>(vos, vos.size());
    }

    /**
     * 修改跑旗记录
     *
     * @param dto
     */
    @Override
    @Transactional
    public void updateContribution(UpdateContributionDTO dto, ImportRecord importRecord) {
        ContributionRecord contribution = contributionRecordMapper.selectById(dto.getId());
        if (contribution == null) {
            throw new EntityNotExistException("miao贡记录不存在");
        }
        if (!importRecord.getId().equals(contribution.getImportRecordId())) {
            throw new InvalidOperationException("非法操作token");
        }
        String originalName = contribution.getName();
        String newName = dto.getName();
        if (!StrUtil.equalsAnyIgnoreCase(originalName, newName)) {
            NickCorrect nickCorrect = NickCorrect.builder()
                    .original(originalName)
                    .correct(newName)
                    .build();
            nickCorrectMapper.addCorrect(nickCorrect);
        }
        contribution.setName(dto.getName());
        contribution.setFlagRace(dto.getFlagRace());
        contribution.setCulvert(dto.getCulvert());
        contribution.setCorrected(1);
        contributionRecordMapper.updateById(contribution);
    }

    /**
     * 查询本周遗漏名单
     *
     * @param dto
     * @return
     */
    @Override
    public ListQueryVO<String> getOmitName(DetailsQueryDTO dto) {
        ImportRecord importRecord = importRecordMapper.selectById(dto.getId());
        String weekStartDate = importRecord.getWeekStartDate();
        String lastWeekStartDate = DateUtil.offsetWeek(DateTime.of(weekStartDate, "yyyy-MM-dd"), -1).toString("yyyy" +
                "-MM-dd");

        LambdaQueryWrapper<ImportRecord> queryWrapper = Wrappers.<ImportRecord>lambdaQuery()
                .eq(ImportRecord::getWeekStartDate, lastWeekStartDate);
        ImportRecord lastWeekRecord = importRecordMapper.selectOne(queryWrapper);

        List<String> omitNameList = contributionRecordMapper.getOmitRecord(importRecord.getId(),
                lastWeekRecord.getId());
        return new ListQueryVO<>(omitNameList, omitNameList.size());
    }

    /**
     * 查询跑旗记录
     *
     * @param dto
     * @return
     */
    @Override
    public ListQueryVO<ContributionVO> getContributionList(GetContributionListDTO dto) {
        if (dto.getQueryType() == 3) {
            DateTime now = DateTime.now();
            DateTime lastThreeWeek = DateUtil.offsetWeek(now, -3);
            String date = DateUtil.beginOfWeek(lastThreeWeek).toString("yyyy-MM-dd");
            dto.setWeekStartDate(date);
        }
        Integer totalCount = contributionRecordMapper.countContribution(dto);
        List<ContributionVO> list = totalCount > 0 ? contributionRecordMapper.getContributionList(dto) :
                Collections.emptyList();
        return new ListQueryVO<>(list, totalCount);
    }

    /**
     * 查询重点关注对象
     *
     * @param dto
     * @return
     */
    @Override
    public FocusDataVO getFocusMemberList(GetFocusDTO dto) {
        Integer guildId = dto.getGuildId();
        Integer queryType = dto.getQueryType();
        DateTime now = DateTime.now();
        DateTime lastThreeWeek = DateUtil.offsetWeek(now, -3);
        String date = DateUtil.beginOfWeek(lastThreeWeek).toString("yyyy-MM-dd");
        List<ContributionVO> contributionList = contributionRecordMapper.getLatestContribution(date, guildId);
        List<FocusVO> focusList = contributionList.stream()
                .collect(Collectors.groupingBy(e -> e.getName().toLowerCase()))
                .entrySet().stream()
                .filter(entry -> {
                    List<ContributionVO> list = entry.getValue();
                    list.sort(Comparator.comparing(ContributionVO::getWeekStartDate).reversed());
                    int i = 0;
                    for (ContributionVO vo : list) {
                        if (vo.getCulvert() == 0 && vo.getFlagRace() == 0) {
                            i++;
                        } else {
                            break;
                        }
                    }
                    return i >= queryType;
                })
                .map(e -> new FocusVO(e.getKey(),
                        e.getValue().stream().collect(Collectors.toMap(ContributionVO::getWeekStartDate, e1 -> e1))))
                .collect(Collectors.toList());

        List<String> timeList = new ArrayList<>();
        DateTime beginOfThisWeek = DateUtil.beginOfWeek(now);
        timeList.add(DateUtil.offsetWeek(beginOfThisWeek, -1).toString("yyyy-MM-dd"));
        timeList.add(DateUtil.offsetWeek(beginOfThisWeek, -2).toString("yyyy-MM-dd"));
        timeList.add(date);
        return new FocusDataVO(timeList, focusList);
    }

    /**
     * 定时任务，生成导入记录
     */
    @Override
    public void generateRecord() {
        List<Guild> guildList = guildMapper.selectList(null);

        DateTime now = DateTime.now();
        DateTime lastWeek = DateUtil.offsetWeek(now, -1);
        String lastWeekString = DateUtil.beginOfWeek(lastWeek).toString("yyyy-MM-dd");
        String lastWeekSunday = DateUtil.endOfWeek(lastWeek).toString("yyyy-MM-dd");
        String timeDesc = StrUtil.format("{}至{}", lastWeekString, lastWeekSunday);

        for (Guild guild : guildList) {
            Integer guildId = guild.getId();
            LambdaQueryWrapper<ImportRecord> queryWrapper = Wrappers.<ImportRecord>lambdaQuery()
                    .eq(ImportRecord::getWeekStartDate, lastWeekString)
                    .eq(ImportRecord::getGuildId, guildId);
            ImportRecord importRecord = importRecordMapper.selectOne(queryWrapper);
            if (importRecord != null) {
                log.warn("{} 上传记录已存在", guild.getName());
                continue;
            }

//            String rawProtectCode = StrUtil.format("4953{}*{}", guild.getName(), lastWeekString);
//            String protectCode = DigestUtil.md5Hex(rawProtectCode, Charset.defaultCharset());

            ImportRecord newRecord = ImportRecord.builder()
                    .guildId(guildId)
                    .guildName(guild.getName())
                    .weekStartDate(lastWeekString)
                    .timeDesc(timeDesc)
                    .pageCount(0)
                    .protectCode(lastWeekString.replaceAll("-", ""))
                    .createTime(now.toString("yyyy-MM-dd HH:mm:ss"))
                    .build();
            importRecordMapper.insert(newRecord);
            log.info("生成导入记录：{}", JSONUtil.toJsonStr(newRecord));
        }

    }
}
