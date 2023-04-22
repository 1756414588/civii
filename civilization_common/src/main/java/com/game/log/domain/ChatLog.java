package com.game.log.domain;

/**
 1）时间：该角色聊天信息/个人邮件发送时间
 2）服务器ID：该角色所属服务器ID
 3）聊天类型：“阵营聊天”“区域聊天”“私人邮件”三种类型
 4）角色ID：该角色userId
 5）角色名：该角色名称
 6）角色等级：该角色当前等级
 7）角色VIP等级：该角色当前VIP等级
 8）信息内容：该角色聊天信息/个人邮件具体内容
 9）是否是内部号：标记内部福利账号
 * */
public class ChatLog {
    private int serId; //服务器ID
    private int type; //聊天类型   1)“阵营聊天”and“区域聊天” //0.国家频道聊天 1.喇叭聊天 2.系统公告滚屏 3.系统公告不滚屏 2)“私人邮件” 4.邮件聊天
    private long roleId; //该角色userId
    private String nick; //角色名
    private int level; //角色等级
    private int vip; //角色VIP等级
    private String msg; //信息内容
    private int isGm; //是否是内部号
    private int channel;//渠道ID
    private int accountKey;//用户的唯一Key
    private int camp;   //用户阵营
    public ChatLog(){

    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(int accountKey) {
        this.accountKey = accountKey;
    }

    public int getSerId() {
        return serId;
    }

    public void setSerId(int serId) {
        this.serId = serId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getIsGm() {
        return isGm;
    }

    public void setIsGm(int isGm) {
        this.isGm = isGm;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public ChatLog(int serId,long roleId,String nick,int level,int vip,int isGm,int channel,int accountKey,int camp, int type, String msg) {
        this.serId = serId;
        this.type = type;
        this.roleId = roleId;
        this.nick = nick;
        this.level = level;
        this.vip = vip;
        this.msg = msg;
        this.isGm = isGm;
        this.channel = channel;
        this.accountKey = accountKey;
        this.camp = camp;
    }



}
