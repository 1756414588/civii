package com.game.worldmap;

import com.game.domain.p.Team;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * 巨型虫族
 */
@Getter
@Setter
public class BigMonster extends Monster {
    private long leaveTime;
    private long rebornTime;
    Team team;
    int mapId;
    int soldierType;
    int state;
    int totalHp;

    @Override
    public DataPb.MonsterData.Builder writeData() {
        DataPb.MonsterData.Builder builder = DataPb.MonsterData.newBuilder();
        builder.setEntityType(getEntityType());
        builder.setTeam(team.wrapPb());
        builder.setLevel(getLevel());
        builder.setId((int) getId());
        builder.setPos(getPos().writeData());
        builder.setStatus(this.getStatus());
        builder.setLeaveTime(leaveTime);
        builder.setRebornTime(rebornTime);
        builder.setMapId(mapId);
        builder.setSoldierType(soldierType);
        builder.setState(state);
        return builder;
    }

    public void unWriteData(DataPb.MonsterData data) {
        this.setEntityType(data.getEntityType());
        Team team = new Team();
        team.unWrapPb(data.getTeam());
        this.team = team;
        this.setLevel(data.getLevel());
        this.setId(data.getId());
        this.setPos(new Pos(data.getPos().getX(), data.getPos().getY()));
        this.setStatus(data.getStatus());
        this.leaveTime = data.getLeaveTime();
        this.rebornTime = data.getRebornTime();
        this.mapId = data.getMapId();
        this.soldierType = data.getSoldierType();
        this.state = data.getState();
    }

    @Override
    public CommonPb.WorldEntity.Builder wrapPb() {
        CommonPb.WorldEntity.Builder builder = super.wrapPb();
        builder.setLessHp(team.getLessSoldier());
        builder.setTotalHp(totalHp);
        return builder;
    }
}
