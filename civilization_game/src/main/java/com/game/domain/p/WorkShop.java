package com.game.domain.p;

import java.security.Key;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.game.pb.CommonPb;

//补给站
public class WorkShop implements Cloneable {
    private BuildingBase base = new BuildingBase();
    private ConcurrentHashMap<Integer, WsWorkQue> workQues = new ConcurrentHashMap<Integer, WsWorkQue>(); // 生产队列1,2,3,4
    private ConcurrentHashMap<Integer, WsWaitQue> waitQues = new ConcurrentHashMap<Integer, WsWaitQue>(); // 预设队列1,2,3,4

    @Override
    public WorkShop clone() {
        WorkShop workShop = null;
        try {
            workShop = (WorkShop) super.clone();
            workShop.setBase(this.base.clone());

            ConcurrentHashMap<Integer, WsWorkQue> map = new ConcurrentHashMap<Integer, WsWorkQue>();
            this.workQues.forEach((key, value) -> {
                map.put(key, value.clone());
            });
            workShop.setWorkQues(map);

            ConcurrentHashMap<Integer, WsWaitQue> map1 = new ConcurrentHashMap<Integer, WsWaitQue>();
            this.waitQues.forEach((key, value) -> {
                map1.put(key, value.clone());
            });
            workShop.setWaitQues(map1);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return workShop;
    }

    public BuildingBase getBase() {
        return base;
    }

    public void setBase(BuildingBase base) {
        this.base = base;
    }

    public int getLv() {
        return base.getLevel();
    }

    public int getBuildingId() {
        return base.getBuildingId();
    }

    public void incrementLevel() {
        base.incrementLevel();
    }

    // building wrap
    public CommonPb.Building.Builder wrapBase() {
        return base.wrapPb();
    }

    public void initBase(int buildId, int buildingLevel) {
        base.setBuildingId(buildId);
        base.setLevel(buildingLevel);
    }

    public CommonPb.WorkShop.Builder wrapPb() {
        CommonPb.WorkShop.Builder builder = CommonPb.WorkShop.newBuilder();
        builder.setWorkShop(wrapBase());

        return builder;
    }

    public void unwrapPb(CommonPb.WorkShop builder) {
        base.unwrapPb(builder.getWorkShop());
    }

    public Map<Integer, WsWorkQue> getWorkQues() {
        return workQues;
    }

    public Map<Integer, WsWaitQue> getWaitQues() {
        return waitQues;
    }

    public void setWorkQues(ConcurrentHashMap<Integer, WsWorkQue> workQues) {
        this.workQues = workQues;
    }

    public void setWaitQues(ConcurrentHashMap<Integer, WsWaitQue> waitQues) {
        this.waitQues = waitQues;
    }
}
