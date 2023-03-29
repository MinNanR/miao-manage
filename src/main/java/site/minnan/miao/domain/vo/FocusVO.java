package site.minnan.miao.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 重点关注人员值对象
 *
 * @author Minnan on 2023/03/29
 */
@Data
@AllArgsConstructor
public class FocusVO {

    private String name;

    private List<ContributionVO> recordList;


}
