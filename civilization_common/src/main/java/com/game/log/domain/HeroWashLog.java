package com.game.log.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 英雄洗练日志类
 *
 * @author CaoBing 2020年4月27日 HeroWashLog.java
 */
public class HeroWashLog {
    /**
     * 角色ID：玩家角色ID
     */
    private Long roleId;

    /**
     * 角色等级
     */
    private int rolelv;

    /**
     * 角色创建时间
     */
    private Date roleCreateTime;

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
     * 英雄品质：该英雄当前品质
     */
    private int quality;

    // 攻击
    private int attack;

    // 防御
    private int defence;

    // 总兵力
    private int soldierNum;

    // 英雄总资质上限：该英雄总资质上限
    private int maxTotalLimit;

    // 英雄资质上限是否满：1代表满了，0代表没满
    private int maxTotalFlag;

    private int channel;

    @Getter
    @Setter
    private int costGold;

    public HeroWashLog() {
    }

    @Builder
    public HeroWashLog(
            long roleId,
            Date roleCreateTime,
            int rolelv,
            int heroType,
            int heroId,
            String heroName,
            int heroLev,
            int quality,
            int attack,
            int defence,
            int soldierNum,
            int maxTotalLimit,
            boolean flag,
            int channel,
            int costGold) {
        this.roleId = roleId;
        this.roleCreateTime = roleCreateTime;
        this.rolelv = rolelv;
        this.heroKeyId = Long.parseLong(roleId + "" + heroType);
        this.heroId = heroId;
        this.heroName = heroName;
        this.heroType = heroType;
        this.heroLev = heroLev;
        this.quality = quality;
        this.attack = attack;
        this.defence = defence;
        this.soldierNum = soldierNum;
        this.maxTotalLimit = maxTotalLimit;
        this.maxTotalFlag = flag ? 1 : 0;
        this.channel = channel;
        this.costGold = costGold;
    }

    public HeroWashLog(
            long roleId,
            Date roleCreateTime,
            int rolelv,
            int heroType,
            int heroId,
            String heroName,
            int heroLev,
            int quality,
            int attack,
            int defence,
            int soldierNum,
            int maxTotalLimit,
            boolean flag,
            int channel) {
        this.roleId = roleId;
        this.roleCreateTime = roleCreateTime;
        this.rolelv = rolelv;
        this.heroKeyId = Long.parseLong(roleId + "" + heroType);
        this.heroId = heroId;
        this.heroName = heroName;
        this.heroType = heroType;
        this.heroLev = heroLev;
        this.quality = quality;
        this.attack = attack;
        this.defence = defence;
        this.soldierNum = soldierNum;
        this.maxTotalLimit = maxTotalLimit;
        this.maxTotalFlag = flag ? 1 : 0;
        this.channel = channel;
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

    public Date getRoleCreateTime() {
        return roleCreateTime;
    }

    public void setRoleCreateTime(Date roleCreateTime) {
        this.roleCreateTime = roleCreateTime;
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

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefence() {
        return defence;
    }

    public void setDefence(int defence) {
        this.defence = defence;
    }

    public int getSoldierNum() {
        return soldierNum;
    }

    public void setSoldierNum(int soldierNum) {
        this.soldierNum = soldierNum;
    }

    public int getMaxTotalLimit() {
        return maxTotalLimit;
    }

    public void setMaxTotalLimit(int maxTotalLimit) {
        this.maxTotalLimit = maxTotalLimit;
    }

    public int getMaxTotalFlag() {
        return maxTotalFlag;
    }

    public void setMaxTotalFlag(int maxTotalFlag) {
        this.maxTotalFlag = maxTotalFlag;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "HeroWashLog [roleId="
                + roleId
                + ", rolelv="
                + rolelv
                + ", roleCreateTime="
                + roleCreateTime
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
                + ", quality="
                + quality
                + ", attack="
                + attack
                + ", defence="
                + defence
                + ", soldierNum="
                + soldierNum
                + ", maxTotalLimit="
                + maxTotalLimit
                + ", maxTotalFlag="
                + maxTotalFlag
                + ", channel="
                + channel
                + "]";
    }
}
