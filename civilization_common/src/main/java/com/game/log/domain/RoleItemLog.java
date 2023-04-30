package com.game.log.domain;

import lombok.Data;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @date 2020/8/14 11:35
 * @description
 */
@Data
public class RoleItemLog {
    public final static int ITEM_ADD = 0;   //道具产出
    public final static int ITEM_USE = 1;   //道具使用

    private Date createDate;
    private long roleId;
    private int itemId;
    private int state;
    private int item_count;
    private int item_resource; //使用类型

    public RoleItemLog(){}
    public RoleItemLog(long roleId, int itemId, int item_count,int state, int item_resource) {
        this.createDate = Calendar.getInstance().getTime();
        this.roleId = roleId;
        this.itemId = itemId;
        this.state = state;
        this.item_count = item_count;
        this.item_resource = item_resource;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getItem_count() {
        return item_count;
    }

    public void setItem_count(int item_count) {
        this.item_count = item_count;
    }

    public int getItem_resource() {
        return item_resource;
    }

    public void setItem_resource(int item_resource) {
        this.item_resource = item_resource;
    }
}
