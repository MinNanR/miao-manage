package site.minnan.miao.userinterface.fascade;

import cn.hutool.core.map.MapBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.minnan.miao.application.service.RecordService;
import site.minnan.miao.domain.entity.ImportRecord;
import site.minnan.miao.domain.vo.ImportRecordListVO;
import site.minnan.miao.domain.vo.ListQueryVO;
import site.minnan.miao.infrastructure.utils.JwtUtil;
import site.minnan.miao.userinterface.dto.GetImportRecordListDTO;
import site.minnan.miao.userinterface.dto.VerifyProtectDTO;
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
    public ResponseEntity<?> uploadFile(MultipartFile file, String token, HttpServletRequest request) throws Exception {
        System.out.println(file.getName());
        System.out.println(file.getSize());
        System.out.println(token);
        ImportRecord importRecord = recordService.validateToken(token);
        recordService.handleUploadFile(file, importRecord);
        return ResponseEntity.success();
    }

    @PostMapping("verifyProtectCode")
    public ResponseEntity<?> verifyProtectCode(@RequestBody @Validated VerifyProtectDTO dto, HttpServletResponse response) {
        String token = recordService.verifyProtectCode(dto, response);
        boolean verified = token != null;
        MapBuilder<Object, Object> builder = MapBuilder.create().put("result", verified);
        if(verified) {
            builder.put("token", token);
        }
        return ResponseEntity.success(builder.build());
    }
}
