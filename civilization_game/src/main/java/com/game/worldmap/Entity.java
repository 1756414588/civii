package com.game.worldmap;

import com.game.flame.NodeType;
import com.game.pb.CommonPb;

// 世界实体
public abstract class Entity implements Comparable<Entity> {
    protected int entityType; // 实体类型: 玩家城池,怪物,资源,NPC城池,
    protected int level; // 实体等级: 玩家等级, 怪物等级,资源等级,Npc城池等级
    protected long id; // 实体Id: 玩家Id, 怪物Id,资源Id,Npc城池Id,初级资源点Id
    protected Pos pos = new Pos();
    protected int distance; //跟某个点的距离，临时数据 用来排序
    protected int country;// 目前被哪个国家占领

    public abstract NodeType getNodeType();
    public abstract int getNodeState();

    public Entity() {
    }

    public Entity(int type, int id, int level) {
        this.entityType = type;
        this.id = id;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }


    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public Pos getPos() {
        return pos;
    }

    public void setPos(Pos pos) {
        this.pos = pos;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getString() {
        return "entityType = " + entityType + ", level = " + level + ", id = " + id + ", pos =" + pos;
    }

    public boolean isExceptEntity() {
        return entityType == EntityType.NpcCity ||
                entityType == EntityType.PrimaryCollect;
    }

    public String getPosStr() {
        return pos.getX() + "," + pos.getY();
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(Pos pos) {
        this.distance = Math.abs(pos.getX() - this.pos.getX()) + Math.abs(pos.getY() - this.pos.getY());
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    @Override
    public int compareTo(Entity o) {
        return this.getDistance() - o.getDistance();
    }

    public CommonPb.WorldEntity.Builder wrapPb() {
        CommonPb.WorldEntity.Builder builder = CommonPb.WorldEntity.newBuilder();
        builder.setEntityType(entityType);
        builder.setId(getId());
        builder.setLevel(level);
        builder.setCountry(this.country);
        if (pos != null) {
            builder.setPos(pos.wrapPb());
        }
        return builder;
    }
}
