package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * @date 2021/1/19 2:17
 * @description
 */
@Getter
@Setter
public class StaticWorldBox {
    private int boxId;//宝箱Id
    private String name;//宝箱名称
    private String desc;//宝箱描述
    private List<List<Integer>> stapleAward;//基础奖励
    private List<List<List<Integer>>> randomAward;//随机奖励
    private List<List<Integer>> randomAwardNum;//随机奖励数量
    private int openTime;//宝箱开启时间
    private int abandonTime;//宝箱丢弃减少的时间
    private String asset;//宝箱图标
    private int quality;
    private List<List<Integer>> needChat;
    private List<List<Integer>> need;
}
