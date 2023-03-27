package site.minnan.miao.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.minnan.miao.domain.entity.ImportRecord;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImportRecordListVO {

    private Integer id;

    private String timeDesc;

    public static ImportRecordListVO assemble(ImportRecord record) {
        return builder().id(record.getId())
                .timeDesc(record.getTimeDesc())
                .build();
    }
}
