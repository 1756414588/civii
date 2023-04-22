package com.game.log.domain;

import lombok.Data;

import java.util.Date;

@Data
public class KillEquipLog {
  // 角色ID
  private Long roleId;

  // 角色名称
  private String roleName;

  // 角色等级
  private int roleLv;

  // 玩家军衔
  private int title;

  // 玩家阵营
  private int country;

  // vip等级
  private int vip;

  // 神器ID
  private int equipId;

  // 神器等级
  private int equipLevel;

  // 玩家晶体数
  private long stone;

  // 升级消耗数量
  private long cost;

  // 角色创建时间
  private Date roleCreateTime;

  // 渠道
  private int channel;
  public KillEquipLog(){}

  public KillEquipLog(
      long roleId,
      String roleName,
      int roleLv,
      int title,
      int country,
      int vip,
      long stone,
      Date roleCreateTime,
      int channel,
      int equipId,
      int equipLevel,
      long cost) {
    this.roleId = roleId;
    this.roleName = roleName;
    this.roleLv = roleLv;
    this.title = title;
    this.country = country;
    this.vip = vip;
    this.equipId = equipId;
    this.equipLevel = equipLevel;
    this.stone = stone;
    this.cost = cost;
    this.roleCreateTime = roleCreateTime;
    this.channel = channel;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public int getRoleLv() {
    return roleLv;
  }

  public void setRoleLv(int roleLv) {
    this.roleLv = roleLv;
  }

  public int getTitle() {
    return title;
  }

  public void setTitle(int title) {
    this.title = title;
  }

  public int getCountry() {
    return country;
  }

  public void setCountry(int country) {
    this.country = country;
  }

  public int getVip() {
    return vip;
  }

  public void setVip(int vip) {
    this.vip = vip;
  }

  public int getEquipId() {
    return equipId;
  }

  public void setEquipId(int equipId) {
    this.equipId = equipId;
  }

  public int getEquipLevel() {
    return equipLevel;
  }

  public void setEquipLevel(int equipLevel) {
    this.equipLevel = equipLevel;
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

  public long getStone() {
    return stone;
  }

  public void setStone(long stone) {
    this.stone = stone;
  }

  public long getCost() {
    return cost;
  }

  public void setCost(long cost) {
    this.cost = cost;
  }
}
