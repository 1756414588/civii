package com.game.domain.p;


import com.game.domain.Award;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.util.PbHelper;

import java.util.ArrayList;
import java.util.List;

public class LevelAward implements Cloneable {
    private int level;
    private List<Award> awards = new ArrayList<Award>();
    private int status;  // 0未领取 1领取

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<Award> getAwards() {
        return awards;
    }

    public void setAwards(List<Award> awards) {
        this.awards = awards;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public DataPb.LevelAwards.Builder writeData() {
        DataPb.LevelAwards.Builder data = DataPb.LevelAwards.newBuilder();
        data.setLevel(level);
        data.addAllAwards(PbHelper.createAwardDataList(awards));
        data.setStatus(status);
        return data;
    }

    public CommonPb.LevelupAwards wrapPb() {
        CommonPb.LevelupAwards.Builder data = CommonPb.LevelupAwards.newBuilder();
        data.setLevel(level);
        if (this.awards != null) {
            data.addAllAwards(PbHelper.createAwardList(awards));
        }
        return data.build();
    }

    public void readData(DataPb.LevelAwards data) {
        level = data.getLevel();
        for (DataPb.AwardData awardData : data.getAwardsList()) {
            if (awardData == null)
                continue;
            Award award = new Award(awardData);
            awards.add(award);
        }
        status = data.getStatus();
    }

    @Override
    public String toString() {
        return "LevelAward{" +
                "level=" + level +
                ", awards=" + awards +
                ", status=" + status +
                '}';
    }

    @Override
    public LevelAward clone() {
        LevelAward levelAward = null;
        try {
            levelAward = (LevelAward) super.clone();
            ArrayList<Award> list = new ArrayList<>();
            this.getAwards().forEach(e -> {
                list.add(e.clone());
            });
            levelAward.setAwards(list);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return levelAward;
    }
}
