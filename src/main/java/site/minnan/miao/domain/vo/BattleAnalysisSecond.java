package site.minnan.miao.domain.vo;

import cn.hutool.core.util.StrUtil;

/**
 * 战斗分析每秒对象
 *
 * @author Minnan on 2023/06/12
 */
public class BattleAnalysisSecond {

    /**
     * 时间坐标
     */
    private Integer time;

    /**
     * 伤害
     */
    private Long damage;

    /**
     * 与上一秒的伤害差异
     * 伤害函数曲线导数值
     */
    private Long damageDiffer;

    public Long getDamage() {
        return damage;
    }

    public Long getDamageDiffer() {
        return damageDiffer;
    }

    public void setDamageDiffer(Long damageDiffer) {
        this.damageDiffer = damageDiffer;
    }

    public Integer getTime() {
        return time;
    }

    public BattleAnalysisSecond(TimeDuration time, Long damage) {
        this.time = time.getSecond();
        this.damage = damage;
    }

    public BattleAnalysisSecond() {
    }

    @Override
    public String toString() {
        return "{" + time + ":" + damage + "}";
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public void setDamage(Long damage) {
        this.damage = damage;
    }
}
