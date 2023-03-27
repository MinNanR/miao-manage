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
public class ImportRecordPage {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("import_record_id")
    private Integer importRecordId;

    @TableField("pic_url")
    private String picUrl;

    @TableField("guild_id")
    private Integer guildId;

    @TableField("guild_name")
    private String guildName;

    @TableField("create_time")
    private String createTime;


}
