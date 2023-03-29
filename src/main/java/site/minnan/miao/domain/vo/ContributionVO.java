package site.minnan.miao.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.minnan.miao.domain.entity.ContributionRecord;

/**
 * miao贡显示参数
 *
 * @author Minnan on 2023/03/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributionVO {

    private Integer id;

    private String name;

    private Integer culvert;

    private Integer flagRace;

    private String timeDesc;

    private String weekStartDate;

    private Integer status;

    private Integer corrected;

    public static ContributionVO assemble(ContributionRecord c) {
        return builder()
                .id(c.getId())
                .name(c.getName())
                .culvert(c.getCulvert())
                .flagRace(c.getFlagRace())
                .status(c.getStatus())
                .corrected(c.getCorrected())
                .build();
    }

}
