package com.game.uc.domain.p;

import com.game.pb.AccountLoginPb;

import java.util.List;

/**
 * @author jyb
 * @date 2020/4/8 14:06
 * @description
 */
public class ServerList {

    private int keyId;


    private String token;

    /**
     * 具体的服务器
     */
    private List<ServerInfo> servers;

    /**
     * 上一次登录的服务器id
     */
    private List<Integer> lastLogin;

    private int open;//1 开启加群好礼

    private int isOpenPackageLog;//1 开启日志

    private String loginLayout;// 登录界面布局

    private String teamNum;//qq号

    public List<ServerInfo> getServers() {
        return servers;
    }

    public void setServers(List<ServerInfo> servers) {
        this.servers = servers;
    }

    public List<Integer> getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(List<Integer> lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getIsOpenPackageLog() {
        return isOpenPackageLog;
    }

    public void setIsOpenPackageLog(int isOpenPackageLog) {
        this.isOpenPackageLog = isOpenPackageLog;
    }

    public String getLoginLayout() {
        return loginLayout;
    }

    public void setLoginLayout(String loginLayout) {
        this.loginLayout = loginLayout;
    }

    public String getTeamNum() {
        return teamNum;
    }

    public void setTeamNum(String teamNum) {
        this.teamNum = teamNum;
    }

    public AccountLoginPb.AccountLoginRs encode() {
        AccountLoginPb.AccountLoginRs.Builder builder = AccountLoginPb.AccountLoginRs.newBuilder();
        builder.setKeyId(this.getKeyId());
        builder.setToken(this.getToken());
        builder.setOpen(this.getOpen());
        builder.setIsOpenPackageLog(this.getIsOpenPackageLog());
        builder.setLoginLayout(this.getLoginLayout());
        builder.setTeamNum(this.getTeamNum() == null ? "D613uh9nKB93eMfwHubGuEa5i66rVe1i" : this.getTeamNum());
        List<ServerInfo> servers1 = this.getServers();
        servers1.forEach(x -> {
            builder.addServers(x.encode());
        });
        builder.addAllLastLogin(this.getLastLogin());
        return builder.build();
    }
}
