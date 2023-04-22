package com.game.domain.p;


import com.game.pb.CommonPb;

import java.util.ArrayList;
import java.util.List;

public class FightBefore {
    private List<BattleEntity> leftEntities = new ArrayList<BattleEntity>();
    private List<BattleEntity> rightEntities = new ArrayList<BattleEntity>();


    public List<BattleEntity> getLeftEntities() {
        return leftEntities;
    }

    public void setLeftEntities(List<BattleEntity> leftEntities) {
        this.leftEntities = leftEntities;
    }

    public List<BattleEntity> getRightEntities() {
        return rightEntities;
    }

    public void setRightEntities(List<BattleEntity> rightEntities) {
        this.rightEntities = rightEntities;
    }

    public CommonPb.FightBefore.Builder wrapPb() {
        CommonPb.FightBefore.Builder builder = CommonPb.FightBefore.newBuilder();
        int leftLen = leftEntities.size();
        int rightLen = rightEntities.size();
        BattleEntity battleEntity;
        for (int i = 0; i < leftLen; i++) {
            battleEntity = leftEntities.get(i);
            if (battleEntity == null)
                continue;
            builder.addLeftEntities(battleEntity.wrapPb());
        }

        for (int i = 0; i < rightLen; i++) {
            battleEntity = rightEntities.get(i);
            if (battleEntity == null)
                continue;
            builder.addRightEntities(battleEntity.wrapPb());
        }

        return builder;
    }

    public void unwrapPb(CommonPb.FightBefore build) {
        int leftLen = build.getLeftEntitiesCount();
        int rightLen = build.getRightEntitiesCount();
        CommonPb.BattleEntity battleEntity;
        for (int i = 0; i < leftLen; i++) {
            battleEntity = build.getLeftEntities(i);
            if (battleEntity == null)
                continue;
            BattleEntity data = new BattleEntity();
            data.unwrapPb(battleEntity);
            leftEntities.add(data);
        }

        for (int i = 0; i < rightLen; i++) {
            battleEntity = build.getRightEntities(i);
            if (battleEntity == null)
                continue;
            BattleEntity data = new BattleEntity();
            data.unwrapPb(battleEntity);
            rightEntities.add(data);
        }
    }

}
