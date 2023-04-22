package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.worldmap.Pos;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportHead {
    private String name = "unkown";        // 玩家名字(服务器主动下发), 叛军和城池读取配置
    private int soldierNum;     // 兵力
    private int lost;           // 损兵
    private int country;        // 吴蜀魏
    private int type;           // 1.玩家 2.叛军 3.城池
    private int portrait;       // 玩家头像
    private int monsterId;      // 怪物Id
    private int cityId;         // 城池Id
    private Pos pos = new Pos();// 位置
    private int headSculpture; //玩家头像框 默认0 无头像框

    public ReportHead() {

    }

    public CommonPb.ReportHead.Builder wrapPb() {
        CommonPb.ReportHead.Builder builder = CommonPb.ReportHead.newBuilder();
        builder.setName(name);
        builder.setSoldierNum(soldierNum);
        builder.setLost(lost);
        builder.setCountry(country);
        builder.setType(type);
        builder.setPortrait(portrait);
        builder.setMonsterId(monsterId);
        builder.setCityId(cityId);
        builder.setPos(pos.wrapPb());
        builder.setHeadSculpture(headSculpture);
        return builder;
    }


    public void unwrapPb(CommonPb.ReportHead builder) {
        name = builder.getName();
        soldierNum = builder.getSoldierNum();
        lost = builder.getLost();
        country = builder.getCountry();
        type = builder.getType();
        portrait = builder.getPortrait();
        monsterId = builder.getMonsterId();
        cityId = builder.getCityId();
        pos.unwrapPb(builder.getPos());
        headSculpture = builder.getHeadSculpture();
    }

    public Pos getPos() {
        return pos;
    }

    public void setPos(Pos pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return new StringBuilder("兵力:").append(soldierNum)
                .append(".损兵：").append(lost)
                .toString();
    }
}
