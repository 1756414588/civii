package com.game.log.domain;

import lombok.Data;

import java.util.Date;

/**
 * 角色资源产出和消耗日志类
 *
 * @author CaoBing 2020年4月27日 RoleResourceLog.java
 */
@Data
public class RoleResourceLog {
  // 产出
  public static final int OPERATE_IN = 0;
  // 消耗
  public static final int OPERATE_OUT = 1;

  /** 角色ID：玩家角色ID */
  private Long roleId;

  /** 角色名称 */
  private String nick;

  /** 角色等级 */
  private int rolelv;

  /** 角色VIP等级 */
  private int viplv;

  /** 角色所属阵营：该角色所属阵营countryId */
  private int countryId;

  /** 角色军衔 */
  private int title;

  /** 角色军功值 */
  private long honor;

  /** 角色创建时间 */
  private Date roleCreateTime;

  private int channel;
  /**
   * 资源类型 1 金币 金币可以用于升级建筑、研究科技、升级军衔、打造装备等 2 钢铁 钢铁可以用于升级建筑、研究科技、阵营建设等 3 食物 食物可以用于训练士兵、行军消耗、阵营建设等 4 晶体
   * 晶体可用于神器升级
   */
  private int resourceType;

  /** 操作类型 1.产出 2.消耗 */
  private int operateType;

  /** 具体操作类型 */
  private int infoType;

  /** 资源数量 */
  private long resourceCount;

  /** 改变数量 */
  private long changeCount;

  public RoleResourceLog() {
    super();
  }

  public RoleResourceLog(
      long roleId,
      Date roleCreateTime,
      int rolelv,
      String nick,
      int viplv,
      int countryId,
      int title,
      long honor,
      long resourceCount,
      int operateType,
      int resourceType,
      int infoType,
      long changeCount,
      int channel) {
    this.roleId = roleId;
    this.roleCreateTime = roleCreateTime;
    this.rolelv = rolelv;
    this.nick = nick;
    this.viplv = viplv;
    this.countryId = countryId;
    this.title = title;
    this.honor = honor;
    this.resourceType = resourceType;
    this.infoType = infoType;
    this.resourceCount = resourceCount;
    this.operateType = operateType;
    this.resourceType = resourceType;
    this.changeCount = changeCount;
    this.channel = channel;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public String getNick() {
    return nick;
  }

  public void setNick(String nick) {
    this.nick = nick;
  }

  public int getRolelv() {
    return rolelv;
  }

  public void setRolelv(int rolelv) {
    this.rolelv = rolelv;
  }

  public int getTitle() {
    return title;
  }

  public void setTitle(int title) {
    this.title = title;
  }

  public int getViplv() {
    return viplv;
  }

  public void setViplv(int viplv) {
    this.viplv = viplv;
  }

  public int getCountryId() {
    return countryId;
  }

  public void setCountryId(int countryId) {
    this.countryId = countryId;
  }

  public long getHonor() {
    return honor;
  }

  public void setHonor(long honor) {
    this.honor = honor;
  }

  public Date getRoleCreateTime() {
    return roleCreateTime;
  }

  public void setRoleCreateTime(Date roleCreateTime) {
    this.roleCreateTime = roleCreateTime;
  }

  public int getResourceType() {
    return resourceType;
  }

  public void setResourceType(int resourceType) {
    this.resourceType = resourceType;
  }

  public int getOperateType() {
    return operateType;
  }

  public void setOperateType(int operateType) {
    this.operateType = operateType;
  }

  public long getResourceCount() {
    return resourceCount;
  }

  public void setResourceCount(long resourceCount) {
    this.resourceCount = resourceCount;
  }

  public long getChangeCount() {
    return changeCount;
  }

  public void setChangeCount(long changeCount) {
    this.changeCount = changeCount;
  }

  public int getInfoType() {
    return infoType;
  }

  public void setInfoType(int infoType) {
    this.infoType = infoType;
  }

  public int getChannel() {
    return channel;
  }

  public void setChannel(int channel) {
    this.channel = channel;
  }

  @Override
  public String toString() {
    return "RoleResourceLog [roleId="
        + roleId
        + ", nick="
        + nick
        + ", rolelv="
        + rolelv
        + ", viplv="
        + viplv
        + ", countryId="
        + countryId
        + ", title="
        + title
        + ", honor="
        + honor
        + ", roleCreateTime="
        + roleCreateTime
        + ", channel="
        + channel
        + ", resourceType="
        + resourceType
        + ", operateType="
        + operateType
        + ", infoType="
        + infoType
        + ", resourceCount="
        + resourceCount
        + ", changeCount="
        + changeCount
        + "]";
  }
}
