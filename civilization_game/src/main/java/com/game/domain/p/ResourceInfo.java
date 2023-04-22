package com.game.domain.p;


import com.game.constant.AwardType;
import com.game.constant.ResourceType;

import com.game.domain.Award;
import java.util.ArrayList;
import java.util.List;

// 资源辅助类
public class ResourceInfo {
    private long iron;
    private long copper;
    private long oil;
    private long stone;

    public ResourceInfo() {

    }

    public ResourceInfo(long iron, long copper, long oil, long stone) {
        this.iron = iron;
        this.copper = copper;
        this.oil = oil;
        this.stone = stone;
    }

    public long getIron () {
        return iron;
    }

    public void setIron (long iron) {
        this.iron = iron;
    }

    public long getCopper () {
        return copper;
    }

    public void setCopper (long copper) {
        this.copper = copper;
    }

    public long getOil () {
        return oil;
    }

    public void setOil (long oil) {
        this.oil = oil;
    }

    public long getStone () {
        return stone;
    }

    public void setStone (long stone) {
        this.stone = stone;
    }

    public List<Award> getAward() {
        List<Award> awards = new ArrayList<Award>();
        awards.add(new Award(AwardType.RESOURCE, ResourceType.IRON, (int) iron));
        awards.add(new Award(AwardType.RESOURCE, ResourceType.COPPER, (int) copper));
        awards.add(new Award(AwardType.RESOURCE, ResourceType.OIL, (int) oil));
        awards.add(new Award(AwardType.RESOURCE, ResourceType.STONE, (int) stone));
        return awards;
    }
}
