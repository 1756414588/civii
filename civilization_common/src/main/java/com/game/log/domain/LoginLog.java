package com.game.log.domain;

import com.game.util.DateHelper;
import io.netty.util.internal.StringUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class LoginLog {
    @Getter
    @Setter
    private long lordId;
    @Getter
    @Setter
    private String nick;
    @Getter
    @Setter
    private int lv;
    @Getter
    @Setter
    private Date createDate;    //角色创建时间
    @Getter
    @Setter
    private Date loginDate;    //角色登录时间
    @Getter
    @Setter
    private Date logoutDate;    //角色离线时间
    @Getter
    @Setter
    private long onlinTime;//在线时长登录时间-离线时间

    public LoginLog() {

    }

    @Builder
    public LoginLog(long lordId, String nick, int lv, Date createDate, Date loginDate, Date logoutDate) {
        this.lordId = lordId;
        this.nick = nick;
        this.lv = lv;
        this.createDate = createDate;
        this.loginDate = loginDate;
        this.logoutDate = logoutDate;
        this.onlinTime = logoutDate.getTime() / 1000 - loginDate.getTime() / 1000;
    }

    @Override
    public String toString() {
        return lordId + ","
                + nick + ","
                + lv + ","
                + DateHelper.formatDateTime(createDate, DateHelper.format1) + ","
                + DateHelper.formatDateTime(loginDate, DateHelper.format1) + ","
                + DateHelper.formatDateTime(logoutDate, DateHelper.format1) + ","
                + onlinTime;
    }
}
