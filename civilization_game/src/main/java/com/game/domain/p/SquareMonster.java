package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;

// square monster
public class SquareMonster {
    private int monsterLv;
    private int monsterId;
    public int getMonsterLv() {
        return monsterLv;
    }

    public void setMonsterLv(int monsterLv) {
        this.monsterLv = monsterLv;
    }

    public int getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public CommonPb.SquareMonster.Builder wrapPb() {
        CommonPb.SquareMonster.Builder builder = CommonPb.SquareMonster.newBuilder();
        builder.setLv(monsterLv);
        builder.setMonsterId(monsterId);

        return builder;
    }

    public void unwrapPb(CommonPb.SquareMonster data) {
        monsterLv  = data.getLv();
        monsterId = data.getMonsterId();
    }

    public DataPb.SquareMonsterData.Builder write() {
        DataPb.SquareMonsterData.Builder builder = DataPb.SquareMonsterData.newBuilder();
        builder.setLv(monsterLv);
        builder.setMonsterId(monsterId);
        return builder;
    }

    public void read(DataPb.SquareMonsterData data) {
        monsterLv  = data.getLv();
        monsterId = data.getMonsterId();
    }

}
