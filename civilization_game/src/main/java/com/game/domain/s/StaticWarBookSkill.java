package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2020/12/3 10:05
 * 兵书技能的配置类
 */
public class StaticWarBookSkill {
    private int id; //KeyId
    private String name;    //技能名字
    private int skillType;    //技能类型
    private int isSoldierSkill;//是否是兵种技能，1为是，0为不是
    private int isSpecialSkill; //是否是特殊技能，1为是，0为不是
    private int level;    //技能等级
    private int soldierType;//技能生效需要的兵种
    private List<List<Integer>> affect;    //该技能在该等级的加成效果
    private int nextSkill;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSkillType() {
        return skillType;
    }

    public void setSkillType(int skillType) {
        this.skillType = skillType;
    }

    public int getIsSoldierSkill() {
        return isSoldierSkill;
    }

    public void setIsSoldierSkill(int isSoldierSkill) {
        this.isSoldierSkill = isSoldierSkill;
    }

    public int getIsSpecialSkill() {
        return isSpecialSkill;
    }

    public void setIsSpecialSkill(int isSpecialSkill) {
        this.isSpecialSkill = isSpecialSkill;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getSoldierType() {
        return soldierType;
    }

    public void setSoldierType(int soldierType) {
        this.soldierType = soldierType;
    }

    public List<List<Integer>> getAffect() {
        return affect;
    }

    public void setAffect(List<List<Integer>> affect) {
        this.affect = affect;
    }

    public int getNextSkill() {
        return nextSkill;
    }

    public void setNextSkill(int nextSkill) {
        this.nextSkill = nextSkill;
    }

    @Override
    public String toString() {
        return "StaticWarBookSkill{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", skillType=" + skillType +
               ", isSoldierSkill=" + isSoldierSkill +
               ", isSpecialSkill=" + isSpecialSkill +
               ", level=" + level +
               ", soldierType=" + soldierType +
               ", affect=" + affect +
               '}';
    }
}
