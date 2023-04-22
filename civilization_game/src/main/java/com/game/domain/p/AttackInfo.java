package com.game.domain.p;

import com.game.pb.CommonPb;
import lombok.Getter;
import lombok.Setter;

// 攻击信息
@Getter
@Setter
public class AttackInfo {
    private int entityId;
    private int entityType;
    private int damage;
    private int status; // 1.闪避 2.被暴击  3.当前排死亡 4.当前实体是否死亡
    private int techLv;                            //科技等级

    public CommonPb.AttackInfo.Builder wrapPb() {
        CommonPb.AttackInfo.Builder builder = CommonPb.AttackInfo.newBuilder();
        builder.setEntityId(entityId);
        builder.setEntityType(entityType);
        builder.setDamage(damage);
        builder.setStatus(status);
        builder.setTechLv(techLv);
        return builder;
    }

    public void unwrapPb(CommonPb.AttackInfo attackInfo) {
        entityId = attackInfo.getEntityId();
        entityType = attackInfo.getEntityType();
        damage = attackInfo.getDamage();
        status = attackInfo.getStatus();
        techLv = attackInfo.getTechLv();
    }

    @Override
    public String toString() {
        return "AttackInfo{" +
            "entityId=" + entityId +
            ", entityType=" + entityType +
            ", damage=" + damage +
            ", status=" + status +
            ", techLv=" + techLv +
            '}';
    }
}
