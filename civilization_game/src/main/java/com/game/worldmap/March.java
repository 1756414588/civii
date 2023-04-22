package com.game.worldmap;

import java.util.ArrayList;
import java.util.List;

import com.game.domain.Award;
import com.game.pb.DataPb;
import lombok.Getter;
import lombok.Setter;

/**
 * 行军信息
 */
@Getter
@Setter
public class March {
    private int keyId;        // 行军Id
    private long lordId;     // 行军Lord Id
    private List<Integer> heroIds = new ArrayList<Integer>(); // 英雄Id
    private int state;      // 行军状态机：1启程中 2.回城中 3.采集资源等待 4.守城等待 5.行军完成 6.战斗中 7.战斗完
    private long endTime;   // 行军结束时间
    private Pos startPos;   // 起始坐标
    private Pos endPos;     // 结束坐标
    private long period;    // 行军时长
    private int marchType;  // 行军目的: 1.攻打叛军
    private long defencerId;  // 攻打的玩家Id
    private int side;         // 攻击或者防守 1.攻击 2.防守
    private List<Award> awards = new ArrayList<Award>();
    private long warId;          // 战争Id
    private int country;
    private long fightTime;
    private long attackerId;
    private long assistId;      // 驻防的玩家Id
    private int addFactor;      // 活动加成 /100
    private boolean collectDone; // 是否可以采集完[false:采集不完, true:采集完成]
    /**
     * 行军类型 1立即出击 0普通出击
     */
    private int type;

	private long nextCalTime;// 下一次结算个人时间,战火
	private long buildId;

    public void setStartPos(Pos startPos) {
        this.startPos = startPos;
    }

    public void setEndPos(Pos endPos) {
        this.endPos = endPos;
    }


    public DataPb.MarchData.Builder writeMarch() {
        DataPb.MarchData.Builder builder = DataPb.MarchData.newBuilder();
        builder.setLordId(lordId);
        builder.setState(getState());
        Pos startPos = getStartPos();
        builder.setStartPos(startPos.writeData());
        Pos endPos = getEndPos();
        builder.setEndPos(endPos.writeData());
        builder.setEndTime(getEndTime());
        List<Integer> heroIds = getHeroIds();
        for (Integer heroId : heroIds) {
            builder.addHeroId(heroId);
        }

        for (Award award : awards) {
            builder.addAwards(award.writeData());
        }

        builder.setMarchType(marchType);
        builder.setKeyId(keyId);
        builder.setFightTime(fightTime);
        builder.setPeriod(period);
        builder.setCountry(country);
        builder.setWarId(warId);
        builder.setAttackerId(attackerId);
        builder.setSide(side);
        builder.setDefencerId(defencerId);
        builder.setAssistId(assistId);
        builder.setAddFactor(addFactor);
        builder.setCollectDone(collectDone);
        builder.setType(type);

        return builder;
    }

    public void readMarch(DataPb.MarchData builder) {
        setLordId(builder.getLordId());
        setState(builder.getState());
        Pos startPos = new Pos();
        startPos.readData(builder.getStartPos());
        setStartPos(startPos);
        Pos endPos = new Pos();
        endPos.readData(builder.getEndPos());
        setEndPos(endPos);
        setEndTime(builder.getEndTime());
        List<Integer> heroIds = builder.getHeroIdList();
        List<Integer> marchHero = getHeroIds();
        marchHero.clear();
        for (Integer heroId : heroIds) {
            marchHero.add(heroId);
        }

        awards.clear();
        for (DataPb.AwardData awardData : builder.getAwardsList()) {
            Award award = new Award();
            award.readData(awardData);
            awards.add(award);
        }

        marchType = builder.getMarchType();
        keyId = builder.getKeyId();
        fightTime = builder.getFightTime();
        period = builder.getPeriod();
        country = builder.getCountry();
        warId = builder.getWarId();
        defencerId = builder.getDefencerId();
        side = builder.getSide();
        attackerId = builder.getAttackerId();
        assistId = builder.getAssistId();
        addFactor = builder.getAddFactor();
        collectDone = builder.getCollectDone();
        type = builder.getType();
    }


    public void addAwards(Award award) {
        awards.add(award);
    }

    public void addAllAwards(List<Award> award) {
        awards.addAll(award);
    }

    public void swapPos(int reason) {
        Pos.swapPos(startPos, endPos);
    }


    public void setFightTime(long fightTime, int reason) {
        this.fightTime = fightTime;
    }

    public boolean hasHero(int heroId) {
        for (Integer curId : heroIds) {
            if (curId == heroId) {
                return true;
            }
        }

        return false;
    }
}
