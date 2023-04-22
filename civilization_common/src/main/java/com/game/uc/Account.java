package com.game.uc;

import com.alibaba.fastjson.JSONArray;
import io.netty.util.internal.StringUtil;

import java.util.Date;

public class Account {
    private int keyId;
    private int channel;
    private String account;
    private int childNo;
    private int forbid;
    private int active;
    private String baseVersion;
    private String versionNo;
    private int white;
    private int firstSvr;
    private int secondSvr;
    private int thirdSvr;
    private String token;
    private String deviceNo;
    private Date loginDate;
    private Date createDate;
    private Date gameDate;
    private int platType;
    private boolean isCreate;

    private long closeSpeakTime;

    private String imodel;
    private String imei;
    private String ip;
    private String cpu;
    private String deviceUuid;
    private String idfa;
    private String resolution;
    //用户所在区服列表
    private String serverInfos;
    //包名
    private String packageName;
    //玩家的历史服务器
    private String loggedServer;


    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }


    public int getForbid() {
        return forbid;
    }

    public void setForbid(int forbid) {
        this.forbid = forbid;
    }

    public String getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }


    public int getFirstSvr() {
        return firstSvr;
    }

    public void setFirstSvr(int firstSvr) {
        this.firstSvr = firstSvr;
    }

    public int getSecondSvr() {
        return secondSvr;
    }

    public void setSecondSvr(int secondSvr) {
        this.secondSvr = secondSvr;
    }

    public int getThirdSvr() {
        return thirdSvr;
    }

    public void setThirdSvr(int thirdSvr) {
        this.thirdSvr = thirdSvr;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public int getWhite() {
        return white;
    }

    public void setWhite(int white) {
        this.white = white;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(String baseVersion) {
        this.baseVersion = baseVersion;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getChildNo() {
        return childNo;
    }

    public void setChildNo(int childNo) {
        this.childNo = childNo;
    }


    public Date getGameDate() {
        return gameDate;
    }

    public void setGameDate(Date gameDate) {
        this.gameDate = gameDate;
    }

    public int getPlatType() {
        return platType;
    }

    public void setPlatType(int platType) {
        this.platType = platType;
    }


    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public boolean isCreate() {
        return isCreate;
    }

    public void setCreate(boolean create) {
        isCreate = create;
    }

    public String getImodel() {
        return imodel;
    }

    public void setImodel(String imodel) {
        this.imodel = imodel;
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

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getIdfa() {
        return idfa;
    }

    public void setIdfa(String idfa) {
        this.idfa = idfa;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public long getCloseSpeakTime() {
        return closeSpeakTime;
    }

    public void setCloseSpeakTime(long closeSpeakTime) {
        this.closeSpeakTime = closeSpeakTime;
    }

    public String getServerInfos() {
        return serverInfos;
    }

    public void setServerInfos(String serverInfos) {
        this.serverInfos = serverInfos;
    }

    public JSONArray serverInfoList() {
        if (StringUtil.isNullOrEmpty(serverInfos)) {
            return new JSONArray();
        } else {
            return JSONArray.parseArray(serverInfos);
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getLoggedServer() {
        return loggedServer;
    }

    public void setLoggedServer(String loggedServer) {
        this.loggedServer = loggedServer;
    }
}
