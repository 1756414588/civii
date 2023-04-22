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
    private int status; // 1.闪避 2.被暴击 3.当前排死亡 4.当前实体是否死亡
    private int techLv; // 科技等级
    private int type; // 1.技能 2.给别人放技能
    private int skillId;

    public CommonPb.AttackInfo.Builder wrapPb() {
        CommonPb.AttackInfo.Builder builder = CommonPb.AttackInfo.newBuilder();
        builder.setEntityId(entityId);
        builder.setEntityType(entityType);
        builder.setDamage(damage);
        builder.setStatus(status);
        builder.setTechLv(techLv);
        builder.setType(this.type);
        builder.setSkillId(this.skillId);
        return builder;
    }

    public void unwrapPb(CommonPb.AttackInfo attackInfo) {
        entityId = attackInfo.getEntityId();
        entityType = attackInfo.getEntityType();
        damage = attackInfo.getDamage();
        status = attackInfo.getStatus();
        techLv = attackInfo.getTechLv();
        this.type = attackInfo.getType();
        this.skillId = attackInfo.getSkillId();
    }

    @Override
    public String toString() {
        return "AttackInfo{" + "entityId=" + entityId + ", entityType=" + entityType + ", damage=" + damage + ", status=" + status + ", techLv=" + techLv + '}';
    }
}
