package com.game.log.domain;

import lombok.Data;

import java.util.Date;

/** 2020年5月13日 @CaoBing halo_game EquipWash.java 装备洗练日志类 */
@Data
public class EquipWashLog {

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

  // 装备ID
  private int equipId;

  // 装备品阶 1.白、2.蓝、3.绿、4.金、5.红、6.紫
  private int quality;

  // 角色当前精研剩余次数
  private int surTimes;

  // 精研类型 1免费 2.付费
  private int washType;

  private int isfull;

  private Date roleCreateTime;

  private int keyId;

  private int channel;

  public EquipWashLog(){

  }

  public EquipWashLog(
      long roleId,
      String roleName,
      int roleLv,
      int title,
      int country,
      int vip,
      int equipId,
      int quality,
      int surTimes,
      Date roleCreateTime,
      Boolean isfull,
      int washType,
      int keyId,
      int channel) {
    this.roleId = roleId;
    this.roleName = roleName;
    this.roleLv = roleLv;
    this.title = title;
    this.country = country;
    this.vip = vip;
    this.equipId = equipId;
    this.quality = quality;
    this.surTimes = surTimes;
    this.washType = washType;
    this.isfull = isfull ? 1 : 0;
    this.roleCreateTime = roleCreateTime;
    this.keyId = keyId;
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

  public int getQuality() {
    return quality;
  }

  public void setQuality(int quality) {
    this.quality = quality;
  }

  public int getSurTimes() {
    return surTimes;
  }

  public void setSurTimes(int surTimes) {
    this.surTimes = surTimes;
  }

  public int getWashType() {
    return washType;
  }

  public void setWashType(int washType) {
    this.washType = washType;
  }

  public int getIsfull() {
    return isfull;
  }

  public void setIsfull(int isfull) {
    this.isfull = isfull;
  }

  public Date getRoleCreateTime() {
    return roleCreateTime;
  }

  public int getKeyId() {
    return keyId;
  }

  public void setKeyId(int keyId) {
    this.keyId = keyId;
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
    return "EquipWashLog [roleId="
        + roleId
        + ", roleName="
        + roleName
        + ", roleLv="
        + roleLv
        + ", title="
        + title
        + ", country="
        + country
        + ", vip="
        + vip
        + ", equipId="
        + equipId
        + ", quality="
        + quality
        + ", surTimes="
        + surTimes
        + ", washType="
        + washType
        + ", isfull="
        + isfull
        + ", roleCreateTime="
        + roleCreateTime
        + ", keyId="
        + keyId
        + ", channel="
        + channel
        + "]";
  }
}
