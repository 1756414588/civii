package com.game.worldmap;

import com.game.flame.NodeType;
import com.game.pb.DataPb;

public class Monster extends Entity {
    private int status;  // AI 状态 0 未被攻击 1 被攻击

    @Override
    public NodeType getNodeType() {
        return null;
    }

    @Override
    public int getNodeState() {
        return 0;
    }

    public Monster() {
        super();
    }

    public Monster(int type, int id, int level) {
        super(type, id, level);
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public DataPb.MonsterData.Builder writeData() {
        DataPb.MonsterData.Builder builder = DataPb.MonsterData.newBuilder();
        builder.setEntityType(getEntityType());
        builder.setLevel(getLevel());
        builder.setId((int) getId());
        builder.setPos(getPos().writeData());
        builder.setStatus(status);
        return builder;
    }

    public void readData(DataPb.MonsterData data) {
        setEntityType(data.getEntityType());
        setLevel(data.getLevel());
        setId(data.getId());
        Pos pos = new Pos();
        pos.readData(data.getPos());
        setPos(pos);
    }
}
