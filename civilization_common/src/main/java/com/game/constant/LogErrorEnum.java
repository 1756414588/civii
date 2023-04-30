package com.game.constant;

/**
 *
 * @date 2020/1/15 14:32
 * @description
 */
public enum LogErrorEnum {

    PLAYER_NOT_EXIST(1001,"玩家不存在");

    private String msg;

    private int  code;

    LogErrorEnum(int code,String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }}
