package com.game.domain.p;

import com.game.constant.BookEffectType;
import com.game.pb.CommonPb;
import lombok.Getter;
import lombok.Setter;

/**
 * 战斗属性:强攻、强防、暴击、闪避、抗暴击、命中
 *
 * @author other
 */
@Getter
@Setter
public class BattleProperty {
    private int strongAttack;   //强攻
    private int strongDefence;  //强防
    private int hit;            //命中,千分比
    private int miss;           //闪避,千分比
    private int criti;          //暴击,千分比
    private int tenacity;       //抗暴击,千分比

    public void add(BattleProperty param) {
        strongAttack += param.getStrongAttack();
        strongDefence += param.getStrongDefence();
        hit += param.getHit();
        miss += param.getMiss();
        criti += param.getCriti();
        tenacity += param.getTenacity();
    }

    @Override
    public String toString() {
        return "强攻:" + strongAttack +
                ",强防:" + strongAttack +
                ",命中:" + hit +
                ",闪避:" + miss +
                ",暴击:" + criti +
                ",抗暴:" + tenacity
                ;
    }

    public void unwrapDataPb(CommonPb.Property data) {
        this.strongAttack = data.getStrongAttack();
        this.strongDefence = data.getStrongDefence();
        this.hit = data.getHit();
        this.miss = data.getMiss();
        this.criti = data.getCriti();
        this.tenacity = data.getTenacity();
    }

    public void addValue(int attrId, int value) {
        switch (attrId) {
            case BookEffectType.STRONG_ATTACK:
                this.strongAttack += value;
                break;
            case BookEffectType.STRONG_DEFENCE:
                this.strongDefence += value;
                break;
            case BookEffectType.MISS:
                this.miss += value;
                break;
            case BookEffectType.CRITI:
                this.criti += value;
                break;
            default:
                break;
        }
    }
}
