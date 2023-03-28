package site.minnan.miao.application.service;

import org.springframework.web.multipart.MultipartFile;
import site.minnan.miao.domain.entity.ImportRecord;
import site.minnan.miao.domain.vo.ContributionVO;
import site.minnan.miao.domain.vo.ImportRecordListVO;
import site.minnan.miao.domain.vo.ListQueryVO;
import site.minnan.miao.domain.vo.RecordPageVO;
import site.minnan.miao.userinterface.dto.DetailsQueryDTO;
import site.minnan.miao.userinterface.dto.GetImportRecordListDTO;
import site.minnan.miao.userinterface.dto.VerifyProtectDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * 记录服务
 *
 * @author Minnan on 2023/03/27
 */
public interface RecordService {

    /**
     * 查询导入记录
     *
     * @param dto
     * @return
     */
    ListQueryVO<ImportRecordListVO> getImportRecordList(GetImportRecordListDTO dto);

    /**
     * 验证保护码
     * @param dto
     * @return
     */
    String verifyProtectCode(VerifyProtectDTO dto, HttpServletResponse response);

    /**
     * 验证token合法性并获取操作记录
     *
     * @param token
     * @return
     */
    ImportRecord validateToken(String token) throws Exception;

    /**
     * 处理上传文件
     *
     * @param file
     * @param record
     */
    void handleUploadFile(MultipartFile file, ImportRecord record);

    /**
     * 查询导入页数据（根据导入记录id）
     * @param dto
     * @return
     */
    ListQueryVO<RecordPageVO> getRecordPageList(DetailsQueryDTO dto);

    /**
     * 查询识别结果
     *
     * @param dto
     * @return
     */
    ListQueryVO<ContributionVO> getContributionList(DetailsQueryDTO dto);
}
