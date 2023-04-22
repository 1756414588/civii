package com.game.log.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 *
 * @date 2020/9/15 18:06
 * @description
 */
@Builder
@Data
public class TDLog {
    /**
     * 玩家id
     */
    private long roleId;
    /**
     * 是否进入
     */
    private int isEnter;
    /**
     * 关卡id
     */
    private int tdId;
    /**
     * 进入0 还是通关1
     */
    private int type;
    /**
     * 通关状态 0 通关 1未通关
     */
    private int state;
    /**
     * 通关剩余血量
     */
    private int lessHp;
    @Tolerate
    public TDLog(){}
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getIsEnter() {
        return isEnter;
    }

    public void setIsEnter(int isEnter) {
        this.isEnter = isEnter;
    }

    public int getTdId() {
        return tdId;
    }

    public void setTdId(int tdId) {
        this.tdId = tdId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getLessHp() {
        return lessHp;
    }

    public void setLessHp(int lessHp) {
        this.lessHp = lessHp;
    }
}
