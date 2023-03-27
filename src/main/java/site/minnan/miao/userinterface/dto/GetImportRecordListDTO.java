package site.minnan.miao.userinterface.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetImportRecordListDTO {

    @NotNull(message = "未选定家族")
    private Integer guildId;

    @NotNull(message = "每页显示数量不能为空")
    private Integer pageSize;

    @NotNull(message = "页码不能为空")
    private Integer pageIndex;

}
