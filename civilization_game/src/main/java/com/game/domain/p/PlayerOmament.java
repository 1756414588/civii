package com.game.domain.p;

import com.game.pb.CommonPb;

/**
 * 2020年8月5日
 *
 *    halo_game PlayerOmament.java
 * <p>
 * 玩家穿戴配飾信息
 **/
public class PlayerOmament implements Cloneable {
    private int pos; // 穿戴的位置

    private int omamentId; // 配飾Id

    public int getPos() {
        return pos;
    }

    public PlayerOmament() {
    }

    public PlayerOmament(int pos, int omamentId) {
        this.pos = pos;
        this.omamentId = omamentId;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getOmamentId() {
        return omamentId;
    }

    public void setOmamentId(int omamentId) {
        this.omamentId = omamentId;
    }

    public PlayerOmament cloneInfo() {
        PlayerOmament playerOmament = new PlayerOmament();
        playerOmament.omamentId = omamentId;
        playerOmament.pos = pos;
        return playerOmament;
    }

    public CommonPb.PlayerOmament.Builder wrapPb() {
        CommonPb.PlayerOmament.Builder builder = CommonPb.PlayerOmament.newBuilder();
        builder.setOmamentId(omamentId);
        builder.setPos(pos);
        return builder;
    }

    public void unwrapPb(CommonPb.PlayerOmament build) {
        omamentId = build.getOmamentId();
        pos = build.getPos();
    }

    public void copyData(PlayerOmament PlayerOmament) {
        omamentId = PlayerOmament.getOmamentId();
        pos = PlayerOmament.getPos();
    }

    public CommonPb.PlayerOmament.Builder writeData() {
        CommonPb.PlayerOmament.Builder builder = CommonPb.PlayerOmament.newBuilder();
        builder.setOmamentId(omamentId);
        builder.setPos(pos);
        return builder;
    }

    public void readData(CommonPb.PlayerOmament build) {
        omamentId = build.getOmamentId();
        pos = build.getPos();
    }

    @Override
    public String toString() {
        return "PlayerOmament [pos=" + pos + ", omamentId=" + omamentId + "]";
    }

    @Override
    public PlayerOmament clone() {
        PlayerOmament playerOmament = null;
        try {
            playerOmament = (PlayerOmament) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return playerOmament;
    }
}
