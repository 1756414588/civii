package com.game.log.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.Date;

/**
 * @author cpz
 * @date 2020/9/22 16:16
 * @description
 */
@Builder
@Data
public class RoleLoginLog {
    private long roleId;
    private String roleNme;
    private int serverId;
    private int account;
    private String channel;
    private String childNo;
    private int country;
    private int titile;
    private long honor;
    private long maxScore;
    private int level;
    private long exp;
    private int vip;
    private int vipExp;
    private int mapId;
    private long newState;
    private int nowMission;
    private long gold;
    private int heroCount;
    private int equipsCount;
    private int ladyCount;
    private int advanceCard;
    private long res1Count;
    private long res2Count;
    private long res3Count;
    private long res4Count;
    private long soldier1Count;
    private long soldier2Count;
    private long soldier3Count;
    private Date roleCreateTime;
    private String model;
    private String imei;
    private String ip;
    private long onlineSecond;
    private long onlineDay;
    private String idfa;
    private String uuId;
    @Tolerate
    public RoleLoginLog(){}
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getRoleNme() {
        return roleNme;
    }

    public void setRoleNme(String roleNme) {
        this.roleNme = roleNme;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getAccount() {
        return account;
    }

    public void setAccount(int account) {
        this.account = account;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChildNo() {
        return childNo;
    }

    public void setChildNo(String childNo) {
        this.childNo = childNo;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public int getTitile() {
        return titile;
    }

    public void setTitile(int titile) {
        this.titile = titile;
    }

    public long getHonor() {
        return honor;
    }

    public void setHonor(long honor) {
        this.honor = honor;
    }

    public long getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(long maxScore) {
        this.maxScore = maxScore;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getVipExp() {
        return vipExp;
    }

    public void setVipExp(int vipExp) {
        this.vipExp = vipExp;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public long getNewState() {
        return newState;
    }

    public void setNewState(long newState) {
        this.newState = newState;
    }

    public int getNowMission() {
        return nowMission;
    }

    public void setNowMission(int nowMission) {
        this.nowMission = nowMission;
    }

    public long getGold() {
        return gold;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }

    public int getHeroCount() {
        return heroCount;
    }

    public void setHeroCount(int heroCount) {
        this.heroCount = heroCount;
    }

    public int getEquipsCount() {
        return equipsCount;
    }

    public void setEquipsCount(int equipsCount) {
        this.equipsCount = equipsCount;
    }

    public int getLadyCount() {
        return ladyCount;
    }

    public void setLadyCount(int ladyCount) {
        this.ladyCount = ladyCount;
    }

    public int getAdvanceCard() {
        return advanceCard;
    }

    public void setAdvanceCard(int advanceCard) {
        this.advanceCard = advanceCard;
    }

    public long getRes1Count() {
        return res1Count;
    }

    public void setRes1Count(long res1Count) {
        this.res1Count = res1Count;
    }

    public long getRes2Count() {
        return res2Count;
    }

    public void setRes2Count(long res2Count) {
        this.res2Count = res2Count;
    }

    public long getRes3Count() {
        return res3Count;
    }

    public void setRes3Count(long res3Count) {
        this.res3Count = res3Count;
    }

    public long getRes4Count() {
        return res4Count;
    }

    public void setRes4Count(long res4Count) {
        this.res4Count = res4Count;
    }

    public long getSoldier1Count() {
        return soldier1Count;
    }

    public void setSoldier1Count(long soldier1Count) {
        this.soldier1Count = soldier1Count;
    }

    public long getSoldier2Count() {
        return soldier2Count;
    }

    public void setSoldier2Count(long soldier2Count) {
        this.soldier2Count = soldier2Count;
    }

    public long getSoldier3Count() {
        return soldier3Count;
    }

    public void setSoldier3Count(long soldier3Count) {
        this.soldier3Count = soldier3Count;
    }

    public Date getRoleCreateTime() {
        return roleCreateTime;
    }

    public void setRoleCreateTime(Date roleCreateTime) {
        this.roleCreateTime = roleCreateTime;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getOnlineSecond() {
        return onlineSecond;
    }

    public void setOnlineSecond(long onlineSecond) {
        this.onlineSecond = onlineSecond;
    }

    public long getOnlineDay() {
        return onlineDay;
    }

    public void setOnlineDay(long onlineDay) {
        this.onlineDay = onlineDay;
    }

    public String getIdfa() {
        return idfa;
    }

    public void setIdfa(String idfa) {
        this.idfa = idfa;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }
}
