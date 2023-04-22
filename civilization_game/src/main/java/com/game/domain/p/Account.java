package com.game.domain.p;

import com.game.util.TimeHelper;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Account {
    private int keyId;
    private int accountKey;
    private int serverId;
    private int channel;
    private int childNo;
    private int forbid;
    private int whiteName;
    private long lordId;
    private int created;
    private String deviceNo;
    private Date createDate;
    private Date loginDate;
    private int loginDays;
    private int isGm;
    private int isGuider;
    private long closeSpeakTime;
    private String registerIp;    //註冊Ip
    private String lastLoginIp;    //上次登陸ip
    private int minCoutry;
    private int isDelete;//角色是否删除(0,未删除  1.已删除)
    /**
     * 渠道账号
     */
    private String channelAccount;
    /**
     * 包名
     */
    private String pkgName;
    /**
     * 当天首次登录时间
     */
    private Date firstLoginDate;

    /**
     * 从创建时间算起过了多少天，用来做七日登录用
     *
     * @return
     */
    public int getTimeFromCreat() {
        return TimeHelper.equation(createDate.getTime(), System.currentTimeMillis()) + 1;
    }

}
