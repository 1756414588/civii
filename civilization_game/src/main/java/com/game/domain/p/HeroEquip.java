package com.game.domain.p;

import java.util.ArrayList;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.worldmap.Pos;


public class HeroEquip implements Cloneable {
    private int pos;                    //装备槽位,TO DELETE
    private Equip equip;                //装备,类型决定位置

    public HeroEquip() {
        equip = new Equip();
    }

    public HeroEquip(int pos, boolean hasEquip) {
        this.setPos(pos);
        this.setEquip(equip);
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Equip getEquip() {
        return equip;
    }

    public void setEquip(Equip equip) {
        this.equip = equip;
    }

    public HeroEquip cloneInfo() {
        HeroEquip heroEquip = new HeroEquip();
        heroEquip.pos = pos;
        heroEquip.equip = equip.cloneInfo();
        return heroEquip;
    }

    public CommonPb.HeroEquip.Builder wrapPb() {
        CommonPb.HeroEquip.Builder builder = CommonPb.HeroEquip.newBuilder();
        builder.setPos(pos);
        if (equip != null) {
            builder.setEquip(equip.wrapPb());
        }
        return builder;
    }

    public void unwrapPb(CommonPb.HeroEquip build) {
        pos = build.getPos();
        if (build.hasEquip()) {
            equip.unwrapPb(build.getEquip());
        }
    }

    public void cloneEquip(Equip equip) {
        this.equip = equip.cloneInfo();
    }

    public Equip getEquipClone() {
        return this.equip.cloneInfo();
    }

    public ArrayList<Integer> getEquipSkill() {
        return equip.getSkills();
    }


    public DataPb.HeroEquipData.Builder writeData() {
        DataPb.HeroEquipData.Builder builder = DataPb.HeroEquipData.newBuilder();
        builder.setPos(pos);
        if (equip != null) {
            builder.setEquip(equip.writeData());
        }
        return builder;
    }

    public void readData(DataPb.HeroEquipData build) {
        pos = build.getPos();
        if (build.hasEquip()) {
            equip.readData(build.getEquip());
        }
    }

    @Override
    protected HeroEquip clone() {
        HeroEquip heroEquip = null;
        try {
            heroEquip = (HeroEquip) super.clone();
            heroEquip.setEquip(this.equip.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return heroEquip;
    }
}
