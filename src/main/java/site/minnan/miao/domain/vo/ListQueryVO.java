package site.minnan.miao.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListQueryVO<T> {

    List<T> list;

    Integer totalCount;

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount.intValue();
    }

    public ListQueryVO(List<T> list, Long totalCount) {
        this.list = list;
        this.totalCount = totalCount.intValue();
    }
}
