package com.game.domain.s;

import com.game.domain.Award;

import java.util.ArrayList;
import java.util.List;

// 世界地图野怪
public class StaticWorldMonster {
    private int id;
    private int level;
    private int type;
    private List<List<Integer>> dropList;
    private List<Integer> monsterIds;
    private int iron;
    private int copper;
    private String name;         //怪物名字
    private String params;       //参数
    private List<List<Integer>> extraDrop;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<List<Integer>> getDropList() {
        return dropList;
    }

    public void setDropList(List<List<Integer>> dropList) {
        this.dropList = dropList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getMonsterIds() {
        return monsterIds;
    }

    public void setMonsterIds(List<Integer> monsterIds) {
        this.monsterIds = monsterIds;
    }

    public int getIron() {
        return iron;
    }

    public void setIron(int iron) {
        this.iron = iron;
    }

    public int getCopper() {
        return copper;
    }

    public void setCopper(int copper) {
        this.copper = copper;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public List<Award> getAwards() {
        List<Award> awards = new ArrayList<>();
        if (dropList != null && dropList.size() > 0) {
            for (List<Integer> drop : dropList) {
                if (drop.get(3) == 100) {
                    Award award = new Award(drop.get(0), drop.get(1), drop.get(2));
                    awards.add(award);
                }
            }
        }
        return awards;
    }

    public List<List<Integer>> getExtraDrop() {
        return extraDrop;
    }

    public void setExtraDrop(List<List<Integer>> extraDrop) {
        this.extraDrop = extraDrop;
    }
}
