package com.game.domain;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.util.random.BaseBoxItem;

import java.util.List;

/**
 * @author 陈奎
 * @version 1.0
 * @filename
 * @time 2016-12-17 下午7:59:49
 */
public class Award extends BaseBoxItem implements Cloneable {
    private int keyId;
    private int type;
    private int id;
    private int count;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Award() {
    }

    public Award(int keyId, int type, int id, int count) {
        this.keyId = keyId;
        this.type = type;
        this.id = id;
        this.count = count;
    }

    public Award(Award award) {
        this.keyId = award.getKeyId();
        this.type = award.getType();
        this.id = award.getId();
        this.count = award.getCount();
    }


    public Award(CommonPb.Award pbAward) {
        this.keyId = pbAward.getKeyId();
        this.type = pbAward.getType();
        this.id = pbAward.getId();
        this.count = pbAward.getCount();
    }

    public Award(DataPb.AwardData pbAward) {
        this.keyId = pbAward.getKeyId();
        this.type = pbAward.getType();
        this.id = pbAward.getId();
        this.count = pbAward.getCount();
    }

    public Award(List<Integer> award) {
        this.keyId = 0;
        this.type = award.get(0);
        this.id = award.get(1);
        this.count = award.get(2);
    }

    public CommonPb.Award ser() {
        CommonPb.Award.Builder builder = CommonPb.Award.newBuilder();
        builder.setType(type);
        builder.setId(id);
        builder.setCount(count);
        return builder.build();
    }

    public CommonPb.Award serAddKey() {
        CommonPb.Award.Builder builder = CommonPb.Award.newBuilder();
        builder.setKeyId(keyId);
        builder.setType(type);
        builder.setId(id);
        builder.setCount(count);
        return builder.build();
    }

    public CommonPb.Award.Builder wrapPb() {
        CommonPb.Award.Builder builder = CommonPb.Award.newBuilder();
        builder.setKeyId(keyId);
        builder.setType(type);
        builder.setId(id);
        builder.setCount(count);
        return builder;
    }

    public void unwrapPb(CommonPb.Award builder) {
        keyId = builder.getKeyId();
        type = builder.getType();
        id = builder.getId();
        count = builder.getCount();
    }

    public Award(int type, int id, int count) {
        this.keyId = 0;
        this.type = type;
        this.id = id;
        this.count = count;
    }

    public void readData(DataPb.AwardData builder) {
        keyId = builder.getKeyId();
        type = builder.getType();
        id = builder.getId();
        count = builder.getCount();
    }

    public DataPb.AwardData.Builder writeData() {
        DataPb.AwardData.Builder builder = DataPb.AwardData.newBuilder();
        builder.setKeyId(keyId);
        builder.setType(type);
        builder.setId(id);
        builder.setCount(count);
        return builder;
    }

    public String toString() {
        return ("keyId = " + keyId + ", type = " + type + ", id =" + id + ", count =" + count);
    }

    public boolean isOk() {
        return count > 0;
    }

    public static void swap(Award lhs, Award rhs) {
        int temp = lhs.getKeyId();
        lhs.setKeyId(rhs.getKeyId());
        rhs.setKeyId(temp);

        temp = lhs.getType();
        lhs.setType(rhs.getType());
        rhs.setType(temp);

        temp = lhs.getId();
        lhs.setId(rhs.getId());
        rhs.setId(temp);

        temp = lhs.getCount();
        lhs.setCount(rhs.getCount());
        rhs.setCount(temp);
    }

    @Override
    public Award clone() {
        Award award = null;
        try {
            award = (Award) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return award;
    }
}
