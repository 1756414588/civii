package com.game.domain.s;

/**
 *
 * @date 2020/1/16 14:22
 * @description
 */
public class StaticOpen {
    /**
     * '索引ID'
     */
    private int keyId;

    /**
     * 开启类型
     */
    private int type;
    /**
     * 条件
     */
    private int condition;

    /**
     * 功能开放类型，1功能，2建筑
     */
    private int functionType;

    /**
     * 值 如果是开放建筑的话  就是建筑id
     */
    private int value;


    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public int getFunctionType() {
        return functionType;
    }

    public void setFunctionType(int functionType) {
        this.functionType = functionType;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
