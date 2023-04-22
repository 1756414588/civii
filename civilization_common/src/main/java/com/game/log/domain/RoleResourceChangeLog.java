package com.game.log.domain;

import lombok.Builder;
import lombok.Data;

@Data
public class RoleResourceChangeLog {

  // 角色ID
  private long roleId;

  // 角色名
  private String nick;

  // 角色等级
  private int level;

  // 角色军衔
  private int title;

  // 角色军功
  private long honor;

  // 变化类型 0. add  1. cost
  private long changeType;

  // 当前资源数量
  private long count;

  // 变化数量
  private long changeCount;

  // 类型
  private int type;

  // 角色所属阵营
  private int country;

  // 角色VIP等级
  private int vip;

  // 渠道
  private int channel;
  public RoleResourceChangeLog(){}

  @Builder
  public RoleResourceChangeLog(
          long roleId,
          String nick,
          int level,
          int title,
          long honor,
          int country,
          int vip,
          int channel,
          int changeType,
          long changeCount,
          int type) {
    this.roleId = roleId;
    this.nick = nick;
    this.level = level;
    this.title = title;
    this.honor = honor;
    this.changeCount = changeCount;
    this.type = type;
    this.country = country;
    this.vip = vip;
    this.channel = channel;
    this.changeType = changeType;
  }

  public long getRoleId() {
    return roleId;
  }

  public void setRoleId(long roleId) {
    this.roleId = roleId;
  }

  public String getNick() {
    return nick;
  }

  public void setNick(String nick) {
    this.nick = nick;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getTitle() {
    return title;
  }

  public void setTitle(int title) {
    this.title = title;
  }

  public long getHonor() {
    return honor;
  }

  public void setHonor(long honor) {
    this.honor = honor;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public long getChangeCount() {
    return changeCount;
  }

  public void setChangeCount(long changeCount) {
    this.changeCount = changeCount;
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

  public int getChannel() {
    return channel;
  }

  public void setChannel(int channel) {
    this.channel = channel;
  }

  public long getChangeType() {
    return changeType;
  }

  public void setChangeType(long changeType) {
    this.changeType = changeType;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }
}
