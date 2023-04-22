package com.game.domain.p;

import com.game.domain.Award;
import com.game.flame.NodeType;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.worldmap.Entity;
import com.game.worldmap.EntityType;
import com.game.worldmap.Pos;

// 初级采集区域
public class PrimaryCollect extends Entity {
    private int keyId; // 唯一Id
    private long flushEndTime; // 结束的刷新时间
    private long collectEndTime; // 结束的采集时间, 如果小于now表示没有采集, 除了募兵加速田
    private int randId; // 神秘矿洞随机Id
    private Award award = new Award(); // 奖励
    private long cdTime;
    private int isCollected;
    private long lordId;

    public long getFlushEndTime() {
        return flushEndTime;
    }

    public void setFlushEndTime(long flushEndTime) {
        this.flushEndTime = flushEndTime;
    }

    public long getCollectEndTime() {
        return collectEndTime;
    }

    public void setCollectEndTime(long collectEndTime) {
        this.collectEndTime = collectEndTime;
    }

    public DataPb.PrimaryCollectData.Builder writeData() {
        DataPb.PrimaryCollectData.Builder builder = DataPb.PrimaryCollectData.newBuilder();
        builder.setKeyId(keyId);
        builder.setId((int) getId());
        builder.setFlushEndTime(flushEndTime);
        builder.setCollectEndTime(collectEndTime);
        builder.setRandId(randId);
        builder.setAward(award.writeData());
        builder.setPos(getPos().writeData());
        builder.setCdTime(cdTime);
        builder.setLordId(lordId);
        return builder;
    }

    // entityType, level, id, pos
    public void readData(DataPb.PrimaryCollectData builder) {
        keyId = builder.getKeyId();
        setId(builder.getId());
        flushEndTime = builder.getFlushEndTime();
        collectEndTime = builder.getCollectEndTime();
        randId = builder.getRandId();
        award.readData(builder.getAward());
        Pos pos = getPos();
        if (pos != null) {
            pos.readData(builder.getPos());
        }
        cdTime = builder.getCdTime();
        lordId = builder.getLordId();
        setEntityType(EntityType.PrimaryCollect);
    }

    public int getRandId() {
        return randId;
    }

    public void setRandId(int randId) {
        this.randId = randId;
    }

    @Override
    public NodeType getNodeType() {
        return null;
    }

    @Override
    public int getNodeState() {
        return 0;
    }

    @Override
    public CommonPb.WorldEntity.Builder wrapPb() {
        return super.wrapPb();
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    @Override
    public int hashCode() {
        return keyId ^ (int) getId();
    }


    public Award getAward() {
        return award;
    }

    public void setAward(Award award) {
        this.award = award;
    }

    public long getCdTime() {
        return cdTime;
    }

    public void setCdTime(long cdTime) {
        this.cdTime = cdTime;
    }

    public int getIsCollected() {
        return isCollected;
    }

    public void setIsCollected(int isCollected) {
        this.isCollected = isCollected;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }
}
