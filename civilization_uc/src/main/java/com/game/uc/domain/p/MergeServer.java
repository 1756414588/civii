package com.game.uc.domain.p;


/**
 * @Description TODO
 * @Date 2021/3/8 21:56
 **/
public class MergeServer {
    private Integer serverId;
    private Integer countryId;
    private Integer mergerServerId;
    private String ip;
    private Integer jettyPort;
    private Integer netPort;


    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Integer getMergerServerId() {
        return mergerServerId;
    }

    public void setMergerServerId(Integer mergerServerId) {
        this.mergerServerId = mergerServerId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getJettyPort() {
        return jettyPort;
    }

    public void setJettyPort(Integer jettyPort) {
        this.jettyPort = jettyPort;
    }

    public Integer getNetPort() {
        return netPort;
    }

    public void setNetPort(Integer netPort) {
        this.netPort = netPort;
    }

    @Override
    public String toString() {
        return "MergeServer{" +
                "serverId=" + serverId +
                ", countryId=" + countryId +
                ", mergerServerId=" + mergerServerId +
                ", ip='" + ip + '\'' +
                ", jettyPort=" + jettyPort +
                ", netPort=" + netPort +
                '}';
    }
}
