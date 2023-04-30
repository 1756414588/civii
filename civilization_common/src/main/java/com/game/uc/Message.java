package com.game.uc;

import com.game.constant.UcCodeEnum;

/**
 * .
 *
 *
 * @date 2020/4/9 13:48
 * @description
 */
public class Message {
    /**
     * 状态码 0为 success  其他为error
     */
    private int code = 0;

    /**
     * 状态码描述
     */
    private String desc;

    /**
     * 内容
     */
    private String data;

    private String version;

    public Message(String desc, String data) {
        this.code = code;
        this.desc = desc;
        this.data = data;
    }

    public Message() {
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Message(UcCodeEnum codeEnum) {
        this.code = codeEnum.getCode();
        this.desc = codeEnum.getDesc();
    }

    public Message(String data) {
        this.code = UcCodeEnum.SUCCESS.getCode();
        this.desc = UcCodeEnum.SUCCESS.getDesc();
        this.data = data;
    }

    public Message(UcCodeEnum codeEnum, String data) {
        this.code = codeEnum.getCode();
        this.desc = codeEnum.getDesc();
        this.data = data;
    }

    public Message(UcCodeEnum codeEnum, String version, String data) {
        this.code = codeEnum.getCode();
        this.desc = codeEnum.getDesc();
        this.version = version;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message [code=" + code + ", desc=" + desc + ", data=" + data + ",version=" + version + "]";
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


}
