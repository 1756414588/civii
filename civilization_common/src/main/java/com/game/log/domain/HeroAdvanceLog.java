package com.game.log.domain;

import lombok.Data;

import java.util.Date;

/**
 * 英雄突破日志类
 *
 * @author CaoBing 2020年4月27日 HeroAdvanceLog.java
 */
@Data
public class HeroAdvanceLog {
  // 晋升卡类型
  public static final int ADVANCE_CARD_TYPE = 82;

  /** 角色ID：玩家角色ID */
  private Long roleId;

  /** 角色等级 */
  private int rolelv;

  /** 根据userId&该英雄heroType生成KeyId;例如：userId为101085；heroType为1；则KeyId为1010851 */
  private long heroKeyId;

  /** 英雄ID：该英雄heroId */
  private long heroId;

  /** 英雄名称：该英雄名称 */
  private String heroName;

  /** 英雄类型：该英雄heroType */
  private int heroType;

  /** 英雄等级：该英雄当前等级 */
  private int heroLev;

  /** 英雄品质：该英雄当前品质 */
  private int quality;

  /** 玩家角色当前晋升卡数量：该英雄所属玩家角色当前晋升卡数量 */
  private int advanceCard;

  /** 角色创建时间 */
  private Date roleCreateTime;

  /** 消耗的晋升卡数量 */
  private int advanceCost;

  private int channel;

  public HeroAdvanceLog(){

  }

  public HeroAdvanceLog(
      long roleId,
      Date roleCreateTime,
      int rolelv,
      int heroType,
      int heroId,
      String heroName,
      int heroLev,
      int quality,
      int advanceCost,
      int channel,
      int advanceCard) {
    this.roleId = roleId;
    this.roleCreateTime = roleCreateTime;
    this.rolelv = rolelv;
    this.heroKeyId = Long.parseLong(roleId + "" + heroType);
    this.heroId = heroId;
    this.heroName = heroName;
    this.heroType = heroType;
    this.heroLev = heroLev;
    this.quality = quality;
    this.advanceCost = advanceCost;
    this.advanceCard = advanceCard;
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

  public int getAdvanceCard() {
    return advanceCard;
  }

  public void setAdvanceCard(int advanceCard) {
    this.advanceCard = advanceCard;
  }

  public Date getRoleCreateTime() {
    return roleCreateTime;
  }

  public void setRoleCreateTime(Date roleCreateTime) {
    this.roleCreateTime = roleCreateTime;
  }

  public int getAdvanceCost() {
    return advanceCost;
  }

  public void setAdvanceCost(int advanceCost) {
    this.advanceCost = advanceCost;
  }

  public int getChannel() {
    return channel;
  }

  public void setChannel(int channel) {
    this.channel = channel;
  }

  @Override
  public String toString() {
    return "HeroAdvanceLog [roleId="
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
        + ", quality="
        + quality
        + ", advanceCard="
        + advanceCard
        + ", roleCreateTime="
        + roleCreateTime
        + ", advanceCost="
        + advanceCost
        + ", channel="
        + channel
        + "]";
  }
}
