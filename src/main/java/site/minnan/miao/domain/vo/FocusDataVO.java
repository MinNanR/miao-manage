package site.minnan.miao.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 重点关注对象
 *
 * @author Minnan on 2023/03/29
 */
@Data
@AllArgsConstructor
public class FocusDataVO {

    private List<String> dateList;

    List<FocusVO> list;
}
