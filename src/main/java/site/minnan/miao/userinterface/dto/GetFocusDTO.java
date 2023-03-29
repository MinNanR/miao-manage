package site.minnan.miao.userinterface.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 查询重点关注对象参数
 *
 * @author Minnan on 2023/03/29
 */
@Data
public class GetFocusDTO {

    private Integer guildId;

    /**
     * 1 - 双蛋人
     * 2 - 优化对象
     * 3 - 2W4
     */
    @NotNull(message = "查询类型不能为空")
    private Integer queryType;
}
