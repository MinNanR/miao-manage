package site.minnan.miao.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.minnan.miao.domain.entity.ImportRecordPage;

/**
 * 跑旗页vo
 *
 * @author Minnan on 2023/03/28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordPageVO {

    private Integer id;

    private String picUrl;

    private String createTime;

    public static RecordPageVO assemble(ImportRecordPage page) {
        return builder()
                .id(page.getId())
                .picUrl(page.getPicUrl())
                .createTime(page.getCreateTime())
                .build();
    }
}
