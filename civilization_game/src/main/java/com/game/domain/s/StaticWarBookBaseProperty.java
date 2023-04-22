package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2020/12/8 17:37
 * 兵书基础属性配置值
 */
public class StaticWarBookBaseProperty {
    private int id;    //KeyId
    private int basePropType;    //主属性类型
    private String name;    //名字（基础属性都为基础）
    private int level;    //'等级
    private List<List<Integer>> affect;    //加成效果
    private int needStrengthenNum;    //强化到下一级所需强化道具
    private int isStrengthenSkill; //是否强化技能，1为强化，空为不强化

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBasePropType() {
        return basePropType;
    }

    public void setBasePropType(int basePropType) {
        this.basePropType = basePropType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<List<Integer>> getAffect() {
        return affect;
    }

    public void setAffect(List<List<Integer>> affect) {
        this.affect = affect;
    }

    public int getNeedStrengthenNum() {
        return needStrengthenNum;
    }

    public void setNeedStrengthenNum(int needStrengthenNum) {
        this.needStrengthenNum = needStrengthenNum;
    }

    public int getIsStrengthenSkill() {
        return isStrengthenSkill;
    }

    public void setIsStrengthenSkill(int isStrengthenSkill) {
        this.isStrengthenSkill = isStrengthenSkill;
    }

    @Override
    public String toString() {
        return "StaticWarBookBaseProperty{" +
               "id=" + id +
               ", basePropType=" + basePropType +
               ", name='" + name + '\'' +
               ", level=" + level +
               ", affect=" + affect +
               ", needStrengthenNum=" + needStrengthenNum +
               ", isStrengthenSkill=" + isStrengthenSkill +
               '}';
    }
}
