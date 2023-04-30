package com.game.uc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @date 2020/4/7 11:02
 * @description
 */
public class Server {
    public static final int SERVER_TYPE_TEST= 0;

    public static final int SERVER_TYPE_ACTIVE= 1;

    /**
     * 服务Id
     */
    private int serverId;

    /**
     * 服务器类型 0 测试服 1 正式服
     */
    private int serverType;

    private int zoneId;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 服务器ip
     */
    private String ip;

    /**
     * tcp端口
     */
    private int port;

    /**
     * http端口
     */
    private int httpPort;

    /**
     * 操作时间
     */
    private Date operateTime;

    /**
     * 上一次启动时间
     */
    private Date lastStartTime;

    /**
     * 开服时间
     */
    private Date openTime;

    /**
     * 服务器状态 1流畅 2爆满 3维护
     */
    private int state;

    /**
     * 数据库URL
     */
    private String dbName;

    /**
     * 数据库用户名称
     */
    private String userName;


    /**
     * 用户密码
     */
    private String userPwd;

    /**
     * 服务器创建时间
     */
    private Date createTime;

    /**
     * 最低可视版本号
     */
    private String minVersion;

    /**
     * 可视最大版本号
     */
    private String maxVersion;

    /**
     * ip白名单
     */
    private String ipWhiteList;

    /**
     * 服务器标签1新服 2推荐 3拥挤 0无标记
     */
    private int label;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 域名
     */
    private String domainName;

    /**
     * 活动版号
     */
    private int actMold;

    /**
     * 账号白名单
     */
    private String accountWhiteList;


    /**
     * 最大注册上限
     */
    private int maxRegisterNum;

    /**
     * 最大同时在线上限
     */
    private int maxOnlineNum;

    /**
     * 服务器对于版本
     */
    private int version;

    /**
     * 服务器维护结束时间
     */
    private Date repairEndTime;


    private boolean gmOpen =false;

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getServerType() {
        return serverType;
    }

    public void setServerType(int serverType) {
        this.serverType = serverType;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public Date getLastStartTime() {
        return lastStartTime;
    }

    public void setLastStartTime(Date lastStartTime) {
        this.lastStartTime = lastStartTime;
    }

    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    public String getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(String maxVersion) {
        this.maxVersion = maxVersion;
    }

    public String getIpWhiteList() {
        return ipWhiteList;
    }

    public void setIpWhiteList(String ipWhiteList) {
        this.ipWhiteList = ipWhiteList;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }


    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public int getActMold() {
        return actMold;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public void setActMold(int actMold) {
        this.actMold = actMold;
    }

    public int getZoneId() {
        return zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }


    public List<Integer> getChannels() {
        if (channel != null && !channel.equals("")) {
            String[] channels = channel.split(",");
            List<Integer>  result =new ArrayList<>();
            for (int i = 0; i < channels.length; i++) {
                result.add(Integer.valueOf(channels[i]));
            }
            return result;
        }
        return null;
    }


    public List<Integer> getWhiteList(){
        if (accountWhiteList != null && !accountWhiteList.equals("") ) {
            String[] channels = accountWhiteList.split(",");
            List<Integer>  result =new ArrayList<>();
            for (int i = 0; i < channels.length; i++) {
                result.add(Integer.valueOf(channels[i]));
            }
            return result;
        }
        return null;
    }
    public List<String> getIpWhiteLists(){
        if (ipWhiteList != null && !ipWhiteList.equals("") ) {
            String[] channels = ipWhiteList.split(",");
            List<String>  result =new ArrayList<>();
            for (int i = 0; i < channels.length; i++) {
                result.add(channels[i]);
            }
            return result;
        }
        return null;
    }



    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getMaxRegisterNum() {
        return maxRegisterNum;
    }

    public void setMaxRegisterNum(int maxRegisterNum) {
        this.maxRegisterNum = maxRegisterNum;
    }

    public String getAccountWhiteList() {
        return accountWhiteList;
    }

    public void setAccountWhiteList(String accountWhiteList) {
        this.accountWhiteList = accountWhiteList;
    }

    public int getMaxOnlineNum() {
        return maxOnlineNum;
    }

    public void setMaxOnlineNum(int maxOnlineNum) {
        this.maxOnlineNum = maxOnlineNum;
    }

    /**
     * 验证是否可见
     *
     * @return
     */
    public boolean isVisable(String version) {
        if (version != null && minVersion != null && minVersion.trim().length() > 0 && minVersion.trim().compareTo(version) > 0) {
            return false;
        }
        if (version != null && maxVersion != null && maxVersion.trim().length() > 0 && maxVersion.trim().compareTo(version) < 0) {
            return false;
        }
        return true;
    }

    public boolean isGmOpen() {
        return gmOpen;
    }

    public void setGmOpen(boolean gmOpen) {
        this.gmOpen = gmOpen;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getRepairEndTime() {
        return repairEndTime;
    }

    public void setRepairEndTime(Date repairEndTime) {
        this.repairEndTime = repairEndTime;
    }

    @Override
    public String toString() {
        return "Server{" +
                "serverId=" + serverId +
                ", serverType=" + serverType +
                ", zoneId=" + zoneId +
                ", serverName='" + serverName + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", httpPort=" + httpPort +
                ", operateTime=" + operateTime +
                ", lastStartTime=" + lastStartTime +
                ", openTime=" + openTime +
                ", state=" + state +
                ", dbName='" + dbName + '\'' +
                ", userName='" + userName + '\'' +
                ", userPwd='" + userPwd + '\'' +
                ", createTime=" + createTime +
                ", minVersion='" + minVersion + '\'' +
                ", maxVersion='" + maxVersion + '\'' +
                ", ipWhiteList='" + ipWhiteList + '\'' +
                ", label=" + label +
                ", channel='" + channel + '\'' +
                ", domainName='" + domainName + '\'' +
                ", actMold=" + actMold +
                ", accountWhiteList='" + accountWhiteList + '\'' +
                ", maxRegisterNum=" + maxRegisterNum +
                ", maxOnlineNum=" + maxOnlineNum +
                ", version=" + version +
                ", repairEndTime=" + repairEndTime +
                ", gmOpen=" + gmOpen +
                '}';
    }
}
