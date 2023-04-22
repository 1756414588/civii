package com.game.domain.p;


import com.game.constant.AwardType;
import com.game.constant.ResourceType;
import com.game.domain.Award;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;

import java.util.ArrayList;
import java.util.List;

// 资源
public class LostRes {
    private long iron;
    private long copper;
    private long oil;

    public LostRes(long iron, long copper, long oil) {
        this.iron = iron;
        this.copper = copper;
        this.oil = oil;
    }

    public LostRes() {
    }


    public long getIron() {
        return iron;
    }

    public void setIron(long iron) {
        this.iron = iron;
    }

    public long getCopper() {
        return copper;
    }

    public void setCopper(long copper) {
        this.copper = copper;
    }

    public long getOil() {
        return oil;
    }

    public void setOil(long oil) {
        this.oil = oil;
    }

    public DataPb.LostRes.Builder writeData() {
        DataPb.LostRes.Builder builder = DataPb.LostRes.newBuilder();
        builder.setIron(iron);
        builder.setCopper(copper);
        builder.setOil(oil);
        return builder;
    }

    public void readData(DataPb.LostRes data) {
        iron = data.getIron();
        copper = data.getCopper();
        oil = data.getOil();
    }

    public void clear() {
        iron = 0;
        copper = 0;
        oil = 0;
    }

    public void add(LostRes rhs) {
        iron += rhs.getIron();
        copper += rhs.getCopper();
        oil += rhs.getOil();
    }

    public List<CommonPb.Award> createAward() {
        List<CommonPb.Award> awards = new ArrayList<CommonPb.Award>();
        Award iron = new Award(AwardType.RESOURCE, ResourceType.IRON, (int)getIron());
        Award copper = new Award(AwardType.RESOURCE, ResourceType.COPPER, (int)getCopper());
        Award oil = new Award(AwardType.RESOURCE, ResourceType.OIL, (int)getOil());
        if (iron.getCount() > 0) {
            awards.add(iron.ser());
        }

        if (copper.getCount() > 0) {
            awards.add(copper.ser());
        }

        if (oil.getCount() > 0) {
            awards.add(oil.ser());
        }

        return awards;
    }


    public List<Award> getAward() {
        List<Award> awards = new ArrayList<Award>();
        Award iron = new Award(AwardType.RESOURCE, ResourceType.IRON, (int)getIron());
        Award copper = new Award(AwardType.RESOURCE, ResourceType.COPPER, (int)getCopper());
        Award oil = new Award(AwardType.RESOURCE, ResourceType.OIL, (int)getOil());
        awards.add(iron);
        awards.add(copper);
        awards.add(oil);

        return awards;
    }

    public boolean isEmpty() {
        return iron == 0 && copper == 0 && oil == 0;
    }
}
