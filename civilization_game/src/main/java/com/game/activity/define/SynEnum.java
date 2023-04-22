package com.game.activity.define;

/**
 * 推送提示
 */
public enum SynEnum {
    ACT_TIP_DISAPEAR(1, "红点消失/显示"),
    ACT_DISAPEAR(2, "活动消失"),
    ;

    private int type;
    private String desc;

    private SynEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
