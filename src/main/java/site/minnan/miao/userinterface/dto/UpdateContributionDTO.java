package site.minnan.miao.userinterface.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 修改贡献记录参数
 *
 * @author Minnan on 2023/03/29
 */
@Data
public class UpdateContributionDTO {

    @NotNull(message = "id不能为空")
    private Integer id;

    @NotEmpty(message = "名称不能为空")
    private String name;

    @NotNull(message = "跑旗分不能为空")
    private Integer flagRace;

    @NotNull(message = "水路分不能为空")
    private Integer culvert;

    @NotNull(message = "认真信息不能为空")
    private String token;
}
