package site.minnan.miao.userinterface.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 跑旗查询参数
 *
 * @author Minnan on 2023/03/29
 */
@Data
public class GetContributionListDTO {

    /**
     * 查询类型（1-前置匹配，2-模糊匹配，3-群查询)
     */
    private Integer queryType;

    /**
     * 查询名称
     */
    private String name;

    /**
     * 查询日期
     */
    private String weekStartDate;

    /**
     * 家族id
     */
    private Integer guildId;

    private Integer pageIndex;

    private Integer pageSize;

    /**
     * 排序信息，
     * 1-按水路
     * 2-按跑旗
     */
    private List<SortInfo> sortInfoList;

    public Integer getStart() {
        return (pageIndex - 1) * pageSize;
    }
}
