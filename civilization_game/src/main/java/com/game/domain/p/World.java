package com.game.domain.p;

import com.game.domain.WorldData;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class World {
    private int keyId;
    private byte[] bossData;
    private long lastSaveTime;
    /**
     * 世界目标
     */
    private byte[] targetData;
    /**
     * 季节
     */
    private int season;
    /**
     * 季节结束时间
     */
    private long seasonEndTime;
    /**
     * 季节效果值
     */
    private int effect;
    /**
     * 走马灯展示
     */
    private byte[] chatShowData;
    /**
     * 皇城血战数据
     */
    private byte[] pvpBattleData;
    /**
     * 世界目标数据
     */
    private byte[] worldTargetData;
    /**
     * 世界活动数据
     */
    private byte[] worldActPlanData;
    /**
     * 总最高在线
     */
    private int totalMaxOnLineNum;
    /**
     * 今日最高在线
     */
    private int todayMaxOnLineNum;
    /**
     * 抢夺名城数据存盘
     */
    private String stealCityData;
    private Date refreshTime;
    private int seasonUp;
    private int riotLevel;
    /**
     * 巨型虫族活动
     */
    private String bigMonster;
    /**
     * 圣域争霸
     */
    private byte[] broodWar;
	/**
	 * 虫族主宰
	 */
	private byte[] zerg;
    private byte[] remark;

    public World() {
    }

    public World(WorldData data) {
        keyId = data.getKeyId();
        bossData = data.serBossData();
        lastSaveTime = data.getLastSaveTime();
        targetData = data.serTargetData();
        season = data.getSeason();
        seasonEndTime = data.getSeasonEndTime();
        effect = data.getEffect();
        chatShowData = data.serChatShowData();
        pvpBattleData = data.serPvpBattleData();
        worldTargetData = data.serWorldTarget();
        worldActPlanData = data.serWorldActPlanData();
        totalMaxOnLineNum = data.getTotalMaxOnLineNum();
        totalMaxOnLineNum = data.getTodayMaxOnLineNum();
        refreshTime = data.getRefreshTime();
        stealCityData = data.getStealCity();
        seasonUp = data.getSeasonUp();
        riotLevel = data.getRiotLevel();
        bigMonster = data.getBigMonster();
		zerg = data.serZergData();
        remark = data.cityRemarkDb();
    }
}
