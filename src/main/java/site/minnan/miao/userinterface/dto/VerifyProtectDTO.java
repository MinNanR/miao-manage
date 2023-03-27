package site.minnan.miao.userinterface.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 验证保护码
 *
 * @author Minnan on 2023/03/27
 */
@Data
public class VerifyProtectDTO {

    @NotNull(message = "未指定记录")
    private Integer id;

    @NotEmpty(message = "保护码不能为空")
    private String protectCode;
}
