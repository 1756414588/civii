package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author cpz
 * @date 2021/1/26 14:11
 * @description 个性化 头像 聊天框
 */
@Getter
@Setter
public class StaticPersonality {
    private int id;
    private int type;
    private int menu;
    private int sort;
    private String menuDesc;
    private String name;
    private String desc;
    private int time;
    private String icon;
    private List<Integer> openCond;
}
