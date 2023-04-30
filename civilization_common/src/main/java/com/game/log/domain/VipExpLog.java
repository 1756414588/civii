package com.game.log.domain;
/** 2020年6月1日    halo_game vipExpLog.java */

import com.game.util.DateHelper;
import lombok.Data;

import java.util.Date;

@Data
public class VipExpLog {
  // 角色ID
  private Long roleId;
  // 角色名称
  private String roleName;
  // 所属渠道
  private int channel;
  // 区服
  private int server;
  // 玩家等级
  private int roleLev;
  // vip等级
  private int vipLev;
  // 玩家:该玩家当前经验值
  private long vipExp;
  // 玩家增加的VIP经验
  private long increaseExp;
  // 充值金额
  private int money;
  // 充值的钻石数量
  private int topup;
  // 当前总钻石数量
  private long gold;
  // 最后登录时间
  private String lastLoginTime;
  // 付费还是赠送
  private int free;

  public VipExpLog() {
    super();
  }

  public VipExpLog(
      Long roleId,
      String roleName,
      int channel,
      int server,
      int roleLev,
      int vipLev,
      long vipExp,
      long increaseExp,
      int money,
      int topup,
      long gold,
      Date lastLoginTime,
      boolean free) {
    super();
    this.roleId = roleId;
    this.roleName = roleName;
    this.channel = channel;
    this.server = server;
    this.roleLev = roleLev;
    this.vipLev = vipLev;
    this.vipExp = vipExp;
    this.increaseExp = increaseExp;
    this.money = money;
    this.gold = gold;
    this.topup = topup;
    this.lastLoginTime = lastLoginTime == null ? "" : DateHelper.dateFormat1.format(lastLoginTime);
    this.channel = channel;
    this.free = free ? 0 : 1;
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

  public int getChannel() {
    return channel;
  }

  public void setChannel(int channel) {
    this.channel = channel;
  }

  public int getServer() {
    return server;
  }

  public void setServer(int server) {
    this.server = server;
  }

  public int getRoleLev() {
    return roleLev;
  }

  public void setRoleLev(int roleLev) {
    this.roleLev = roleLev;
  }

  public int getVipLev() {
    return vipLev;
  }

  public void setVipLev(int vipLev) {
    this.vipLev = vipLev;
  }

  public long getVipExp() {
    return vipExp;
  }

  public void setVipExp(long vipExp) {
    this.vipExp = vipExp;
  }

  public long getIncreaseExp() {
    return increaseExp;
  }

  public void setIncreaseExp(long increaseExp) {
    this.increaseExp = increaseExp;
  }

  public int getMoney() {
    return money;
  }

  public void setMoney(int money) {
    this.money = money;
  }

  public int getTopup() {
    return topup;
  }

  public void setTopup(int topup) {
    this.topup = topup;
  }

  public long getGold() {
    return gold;
  }

  public void setGold(long gold) {
    this.gold = gold;
  }

  public String getLastLoginTime() {
    return lastLoginTime;
  }

  public void setLastLoginTime(String lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }

  public int getFree() {
    return free;
  }

  public void setFree(int free) {
    this.free = free;
  }

  @Override
  public String toString() {
    return "VipExpLog [roleId="
        + roleId
        + ", roleName="
        + roleName
        + ", channel="
        + channel
        + ", server="
        + server
        + ", roleLev="
        + roleLev
        + ", vipLev="
        + vipLev
        + ", vipExp="
        + vipExp
        + ", increaseExp="
        + increaseExp
        + ", money="
        + money
        + ", topup="
        + topup
        + ", gold="
        + gold
        + ", lastLoginTime="
        + lastLoginTime
        + "]";
  }
}
