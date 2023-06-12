package site.minnan.miao.domain.vo;

import lombok.Data;

/**
 * BA时间对象
 *
 * @author Minnan on 2023/06/12
 */
@Data
public class TimeDuration {

    private Integer hour;

    private Integer minute;

    private Integer second;

    public TimeDuration(String durationString) {
        this.hour = Integer.parseInt(durationString.substring(0, 2));
        this.minute = Integer.parseInt(durationString.substring(2, 4));
        this.second = Integer.parseInt(durationString.substring(4, 6));
    }

    public Integer getSecond() {
        return hour * 3600 + minute * 60 + second;
    }
}
