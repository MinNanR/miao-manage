package site.minnan.miao.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ContributionRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("import_record_id")
    private Integer importRecordId;

    @TableField("import_record_page_id")
    private Integer importRecordPageId;


    private String name;

    private Integer flagRace;

    private Integer culvert;

    private Integer status;

    private Integer corrected;

    @TableField("guild_id")
    private Integer guildId;

    @TableField("guild_name")
    private String guildName;

    @TableField("create_time")
    private String createTime;

    public void correct(NickCorrect correct) {
        this.name = correct.getCorrect();
    }
}
