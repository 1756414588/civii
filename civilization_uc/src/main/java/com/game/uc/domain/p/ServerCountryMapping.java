package com.game.uc.domain.p;

/**
 * @Description TODO
 * @Date 2021/3/12 10:51
 **/
public class ServerCountryMapping {
    private Integer countyId;
    private String ip;
    private Integer Port;

    public ServerCountryMapping(Integer countyId, String ip, Integer port) {
        this.countyId = countyId;
        this.ip = ip;
        Port = port;
    }

    public Integer getCountyId() {
        return countyId;
    }

    public void setCountyId(Integer countyId) {
        this.countyId = countyId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return Port;
    }

    public void setPort(Integer port) {
        Port = port;
    }

    @Override
    public String toString() {
        return "ServerCountryMapping{" +
                "countyId=" + countyId +
                ", ip='" + ip + '\'' +
                ", Port=" + Port +
                '}';
    }
}
