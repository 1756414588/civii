package com.game.activity.actor;

import com.game.activity.BaseActivityActor;
import com.game.domain.Player;

/**
 * 建筑升级事件
 */
public class BuildingLevelUpActor extends BaseActivityActor {

    // 建筑类型
    private int buildType;
    // 建筑等级
    private int buildLv;

    public BuildingLevelUpActor(Player player, int buildType, int buildLv) {
        this.player = player;
        this.buildType = buildType;
        this.buildLv = buildLv;
    }

    public int getBuildType() {
        return buildType;
    }

    public void setBuildType(int buildType) {
        this.buildType = buildType;
    }

    public int getBuildLv() {
        return buildLv;
    }

    public void setBuildLv(int buildLv) {
        this.buildLv = buildLv;
    }
}
