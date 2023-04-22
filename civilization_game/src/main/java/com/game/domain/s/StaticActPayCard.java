package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 购买月卡的计费点
 * 2020年5月26日
 *
 * @CaoBing halo_game StaticActPayCard.java
 **/
@Getter
@Setter
public class StaticActPayCard {
    private int payCardId;
    private int cardType;
    private int money;
    private int diamond;
    private int awardId;
    private List<List<Integer>> sellList;
    private int period;
    private int vipExp;
    private String assetBg;
    private String assetFont;
    private String desc;
    private String name;
    private int limitDate;
    private int index;

    @Override
    public String toString() {
        return "StaticActPayCard [payCardId=" + payCardId + ", cardType=" + cardType + ", money=" + money + ", diamond=" + diamond + ", awardId=" + awardId
                + ", sellList=" + sellList + ", period=" + period + ", vipExp=" + vipExp + "]";
    }
}
