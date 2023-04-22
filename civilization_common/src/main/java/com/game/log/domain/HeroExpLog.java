package com.game.log.domain;

import com.game.constant.Reason;
import com.game.log.constant.HeroExpType;
import lombok.Data;

import java.util.Date;

/**
 * 英雄升级日志类
 *
 * @author CaoBing 2020年4月27日 HeroExpLog.java
 */
@Data
public class HeroExpLog {
    /**
     * 角色ID：玩家角色ID
     */
    private Long roleId;

    /**
     * 角色等级
     */
    private int rolelv;

    /**
     * 根据userId&该英雄heroType生成KeyId;例如：userId为101085；heroType为1；则KeyId为1010851
     */
    private long heroKeyId;

    /**
     * 英雄ID：该英雄heroId
     */
    private long heroId;

    /**
     * 英雄名称：该英雄名称
     */
    private String heroName;

    /**
     * 英雄类型：该英雄heroType
     */
    private int heroType;

    /**
     * 英雄等级：该英雄当前等级
     */
    private int heroLev;

    /**
     * 英雄经验：该英雄当前经验值
     */
    private long heroExp;

    /**
     * 增加经验
     */
    private long increaseExp;

    /**
     * 获得经验的途径
     */
    private int expType;

    /**
     * 角色创建时间
     */
    private Date roleCreateTime;

    private int channel;

    public HeroExpLog() {
    }

    public HeroExpLog(
            long roleId,
            Date roleCreateTime,
            int rolelv,
            int heroType,
            int heroId,
            String heroName,
            int heroLev,
            long heroExp,
            long exp,
            int reason,
            int channel) {
        this.roleId = roleId;
        this.roleCreateTime = roleCreateTime;
        this.rolelv = rolelv;
        this.heroKeyId = Long.parseLong(roleId + "" + heroType);
        this.heroId = heroId;
        this.heroName = heroName;
        this.heroType = heroType;
        this.heroLev = heroLev;
        this.heroExp = heroExp;
        this.increaseExp = exp;
        this.channel = channel;
        this.expType = reason;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public int getRolelv() {
        return rolelv;
    }

    public void setRolelv(int rolelv) {
        this.rolelv = rolelv;
    }

    public long getHeroKeyId() {
        return heroKeyId;
    }

    public void setHeroKeyId(long heroKeyId) {
        this.heroKeyId = heroKeyId;
    }

    public long getHeroId() {
        return heroId;
    }

    public void setHeroId(long heroId) {
        this.heroId = heroId;
    }

    public String getHeroName() {
        return heroName;
    }

    public void setHeroName(String heroName) {
        this.heroName = heroName;
    }

    public int getHeroType() {
        return heroType;
    }

    public void setHeroType(int heroType) {
        this.heroType = heroType;
    }

    public int getHeroLev() {
        return heroLev;
    }

    public void setHeroLev(int heroLev) {
        this.heroLev = heroLev;
    }

    public long getHeroExp() {
        return heroExp;
    }

    public void setHeroExp(long heroExp) {
        this.heroExp = heroExp;
    }

    public long getIncreaseExp() {
        return increaseExp;
    }

    public void setIncreaseExp(long increaseExp) {
        this.increaseExp = increaseExp;
    }

    public int getExpType() {
        return expType;
    }

    public void setExpType(int expType) {
        this.expType = expType;
    }

    public Date getRoleCreateTime() {
        return roleCreateTime;
    }

    public void setRoleCreateTime(Date roleCreateTime) {
        this.roleCreateTime = roleCreateTime;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "HeroExpLog [roleId="
                + roleId
                + ", rolelv="
                + rolelv
                + ", heroKeyId="
                + heroKeyId
                + ", heroId="
                + heroId
                + ", heroName="
                + heroName
                + ", heroType="
                + heroType
                + ", heroLev="
                + heroLev
                + ", heroExp="
                + heroExp
                + ", increaseExp="
                + increaseExp
                + ", expType="
                + expType
                + ", roleCreateTime="
                + roleCreateTime
                + ", channel="
                + channel
                + "]";
    }
}
