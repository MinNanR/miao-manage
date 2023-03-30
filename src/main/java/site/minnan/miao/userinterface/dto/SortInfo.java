package site.minnan.miao.userinterface.dto;

import lombok.Data;

/**
 * 排序信息
 *
 * @author Minnan on 2023/03/30
 */
@Data
public class SortInfo {

    /**
     * 排序列，使用者自定义
     */
    private String sortType;

    /**
     * 1-降序，2-降序
     */
    private Integer order;
}
