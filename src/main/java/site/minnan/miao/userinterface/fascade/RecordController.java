package site.minnan.miao.userinterface.fascade;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.util.ObjectUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.minnan.miao.application.service.RecordService;
import site.minnan.miao.domain.entity.ImportRecord;
import site.minnan.miao.domain.vo.*;
import site.minnan.miao.infrastructure.utils.JwtUtil;
import site.minnan.miao.userinterface.dto.*;
import site.minnan.miao.userinterface.response.ResponseEntity;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@RequestMapping("/miao-api/record")
@RestController
public class RecordController {

    @Autowired
    private RecordService recordService;

    @PostMapping("/getImportRecordList")
    public ResponseEntity<?> getImportRecordList(@RequestBody GetImportRecordListDTO dto) {
        ListQueryVO<ImportRecordListVO> vo = recordService.getImportRecordList(dto);
        return ResponseEntity.success(vo);
    }

    @PostMapping("/uploadPic")
    public ResponseEntity<?> uploadFile(MultipartFile file, String token) throws Exception {
        System.out.println(file.getName());
        System.out.println(token);
        ImportRecord importRecord = recordService.validateToken(token);
        recordService.handleUploadFile(file, importRecord);
        return ResponseEntity.success();
    }

    @PostMapping("/verifyProtectCode")
    public ResponseEntity<?> verifyProtectCode(@RequestBody @Validated VerifyProtectDTO dto,
                                               HttpServletResponse response) {
        String token = recordService.verifyProtectCode(dto, response);
        boolean verified = token != null;
        MapBuilder<Object, Object> builder = MapBuilder.create().put("result", verified);
        if (verified) {
            builder.put("token", token);
        }
        return ResponseEntity.success(builder.build());
    }

    /**
     * 查询记录页
     *
     * @param dto
     * @return
     */
    @PostMapping("/getRecordPageList")
    public ResponseEntity<ListQueryVO<RecordPageVO>> getRecordPageList(@RequestBody @Validated DetailsQueryDTO dto) {
        ListQueryVO<RecordPageVO> vo = recordService.getRecordPageList(dto);
        return ResponseEntity.success(vo);
    }

    /**
     * 识别结果
     *
     * @param dto
     * @return
     */
    @PostMapping("/getContributionResult")
    public ResponseEntity<ListQueryVO<ContributionVO>> getContributionResult(@RequestBody @Validated DetailsQueryDTO dto) {
        ListQueryVO<ContributionVO> vo = recordService.getContributionList(dto);
        return ResponseEntity.success(vo);
    }

    @PostMapping("/updateContribution")
    public ResponseEntity<?> updateContribution(@RequestBody @Validated UpdateContributionDTO dto) {
        ImportRecord importRecord = recordService.validateToken(dto.getToken());
        recordService.updateContribution(dto, importRecord);
        return ResponseEntity.success();
    }

    /**
     * 查询遗漏名单
     *
     * @param dto
     * @return
     */
    @PostMapping("/getOmitName")
    public ResponseEntity<ListQueryVO<String>> getOmitName(@RequestBody @Validated DetailsQueryDTO dto) {
        ListQueryVO<String> vo = recordService.getOmitName(dto);
        return ResponseEntity.success(vo);
    }

    @PostMapping("/getContributionList")
    public ResponseEntity<ListQueryVO<ContributionVO>> getContributionList(@RequestBody @Validated GetContributionListDTO dto) {
        ListQueryVO<ContributionVO> vo = recordService.getContributionList(dto);
        return ResponseEntity.success(vo, vo.getList().size() > 0 ? "操作成功" : "暂无数据");
    }

    @PostMapping("/getFocusList")
    public ResponseEntity<ListQueryVO<FocusVO>> getFocusMemberList(@RequestBody @Validated GetFocusDTO dto) {
        ListQueryVO<FocusVO> focusMemberList = recordService.getFocusMemberList(dto);
        return ResponseEntity.success(focusMemberList);
    }
}
