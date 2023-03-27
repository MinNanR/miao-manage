package site.minnan.miao.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 导入记录
 *
 * @author minnan on 2023/03/20
 */
@Data
@TableName("import_record")
public class ImportRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 周一日期
     */
    @TableField("week_start_date")
    private String weekStartDate;

    /**
     * 时间描述
     */
    @TableField("time_desc")
    private String timeDesc;

    /**
     * 保护密码
     */
    @TableField("protect_code")
    private String protectCode;

    /**
     * 记录条数
     */
    @TableField("record_count")
    private Integer recordCount;

    /**
     * 截图数
     */
    @TableField("page_count")
    private Integer pageCount;

    /**
     * 家族id
     */
    @TableField("guild_id")
    private Integer guildId;

    /**
     * 家族名称
     */
    private String guildName;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private String createTime;
}
