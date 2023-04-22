package com.game.constant;

/**
 * @author jyb
 * @date 2020/4/9 15:15
 * @description
 */
public enum UcCodeEnum {

    SUCCESS(0, "success"),
    SUCCESS1(100, "SUCCESS"),
    ACCOUNT_NOT_EXIST(1, "账号不存在"),

    TOKEN_IS_ERROR(2, "TOKEN 校验失败"),

    ERROR(-100, "登录服访问出错"),

    SDK_LOGIN_ERROR(-101, "SDK登录校验失败"),

    PARAM_ERROR(3, "参数异常"),

    SERVER_NOT_EXIST(4, "区服不存在"),

    SYS_ERROR(5, "服务器异常"),

    MIAL_TYPE_NOT_EXIST(8, "邮件类型不存在"),

    PAY_ORDER_NOT_EXIST(9, "订单不存在"),

    PAY_ORDER_SUCCESSED_ERROR(10, "已完成不需要处理"),

    PAY_ORDER_CONSUME_ERROR(11, "订单商品发放失败"),

    PAY_SIGN_ERROR(12, "签名错误"),


    CDK_CONFIG_ERROR(13, "cdk 配置有错误"),

    CDK_NOT_EXIST(14, "cdk 不存在"),

    CDK_SERVER_ERROR(15, "该cdk 区服错误"),

    CDK_IS_USE(16, "该cdk 已经被使用"),

    CDK_AWARD_NOT_EXIST(17, "该cdk 奖励不存在"),

    CDK_CHANNEL_ERROR(18, "不能使用非本渠道的cdk"),

    CDK_IS_OVER_TIME(19, "cdk 已经过期"),

    ROLE_IS_CLOSE(20, "角色已经被封掉"),

    SERVER_IS_MAINTAIN(21, "服务器已经维护"),

    SERVER_IS_NOT_OPEN(22, "服务器未开启"),

    PLAYER_NICK_EXIST(23, "玩家昵称已经存在"),

    PLAYER_IS_NOT_EXIST(25, "玩家不存在"),

    GM_COMMAND_NOT_EXIST(26, "GM命令不存在"),

    PAY_BACK_CREATE_ERROR(27, "回调插入订单失败"),

    SERVER_NOT_OPEN(28, "服务器未开启"),

    PLAYER_IS_NOT_WHITE(29, "用户不是白名单"),

    PAY_PRICE_ERROR(30, "应付金额和实付金额不一致"),

    CHANNEL_CONFIG_ERROR(31, "渠道配置错误"),

    PAY_CHANNEl_NOT_OPEN_ERROR(32, "渠道未接入"),

    SERVER_BLOCK(33, "服务器拥挤,请更换其他服务器"),
    SERVER_CLOSE(34, "服务器已关闭,请更换其他服务器"),
    
    ACTIVITY_NOT_OPEN(35, "活动未开启"),
    
    PAY_BACK_PRODUCTID_ERROR(36, "购买商品与实际不符"),

    CHANNEL_CONFIG_NOT_EXIST(37, "渠道配置不存在"),

    ACCOUNT_ISFORIBD(38,"账号已封禁"),
    ROLE_ISFORIBD(39,"角色已封禁"),
    VESION_LOW(40,"客户端版本过低"),
    CDK_LEVEL_NOT_ENOUTH(41,"CDK领取等级不足"),
    PLAYER_ONLINE(42,"玩家在线"),
    ;

    private int code;

    private String desc;

    UcCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
