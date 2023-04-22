package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author cpz
 * @date 2020/12/2 9:30
 * @description
 */
public class StaticActExchange {
    @Getter
    @Setter
    private int keyId;  //keyId
    @Getter
    @Setter
    private List<Integer> award;    //奖励
    @Getter
    @Setter
    private int needNum;    //兑换需要数量
    @Getter
    @Setter
    private int maxNum;     //最大兑换次数
}
