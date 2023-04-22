package com.game.worldmap;

import com.game.domain.Player;
import com.game.flame.NodeType;
import com.game.manager.PlayerManager;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.spring.SpringUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Resource extends Entity {
    private long count; // 当前资源资源
    private int status; // status 0 未被采集 1 正在被采集
    private Player player;  //采集的玩家
    private int flush;
    private int type;//小类型 1.金矿



    public Resource(long count) {

        this.count = count;
    }

    @Override
    public NodeType getNodeType() {
        return null;
    }

    @Override
    public int getNodeState() {
        return 0;
    }

    public Resource() {
        super();
    }

    public Resource(int type, int id, int level, long count) {
        super(type, id, level);
        this.count = count;
    }

    public DataPb.ResourceData.Builder writeData() {
        DataPb.ResourceData.Builder builder = DataPb.ResourceData.newBuilder();
        builder.setEntityType(getEntityType());
        builder.setLevel(getLevel());
        builder.setId((int) getId());
        builder.setPos(getPos().writeData());
        builder.setStatus(status);
        builder.setCount(count);
        if (player != null) {
            builder.setRoleId(player.roleId);
        }
        return builder;
    }

    public void readData(DataPb.ResourceData data) {
        setEntityType(data.getEntityType());
        setLevel(data.getLevel());
        setId(data.getId());
        Pos pos = new Pos();
        pos.readData(data.getPos());
        setPos(pos);
        status = data.getStatus();
        count = data.getCount();
        this.player = SpringUtil.getBean(PlayerManager.class).getPlayer(data.getRoleId());
    }

    @Override
    public CommonPb.WorldEntity.Builder wrapPb() {
        CommonPb.WorldEntity.Builder builder = super.wrapPb();
        if (player != null) {
            builder.setName(player.getNick());
            builder.setCountry(player.getCountry());
        }
        builder.setFlush(flush);//更新的
        return builder;
    }
}
