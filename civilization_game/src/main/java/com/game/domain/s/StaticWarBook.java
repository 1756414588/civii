package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2020/12/8 16:
 * 兵书配置类
 */
public class StaticWarBook {
    /**
     * 1	1	2	绿色兵书1	[[1,50],[2,50]]	[[1,10],[2,10],[3,10]]	[[1,50],[2,30]]	3	兵书
     * 41	1	5	红色兵书1	[[1,50],[2,50]]	[[1,10],[2,10],[3,10],[11,8]]	[[1,50],[2,30]]	4	兵书
     */
    private int id;    //KeyId
    private int type;  //兵书类型，决定槽位（暂且只做一个槽位）
    private int quality;  //品质：2-5对应绿蓝紫橙红
    private String name;  //兵书名字
    private List<List<Integer>> baseProperty; //主属性随机范围
    private List<List<Integer>> skill;  //技能随机范围
    private List<List<Integer>> randSkillNum; //生成时随机激活技能数量
    private int maxSkillNum;  //最大激活技能数量

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<List<Integer>> getBaseProperty() {
        return baseProperty;
    }

    public void setBaseProperty(List<List<Integer>> baseProperty) {
        this.baseProperty = baseProperty;
    }

    public List<List<Integer>> getSkill() {
        return skill;
    }

    public void setSkill(List<List<Integer>> skill) {
        this.skill = skill;
    }

    public List<List<Integer>> getRandSkillNum() {
        return randSkillNum;
    }

    public void setRandSkillNum(List<List<Integer>> randSkillNum) {
        this.randSkillNum = randSkillNum;
    }

    public int getMaxSkillNum() {
        return maxSkillNum;
    }

    public void setMaxSkillNum(int maxSkillNum) {
        this.maxSkillNum = maxSkillNum;
    }

    @Override
    public String toString() {
        return "StaticWarBook{" +
               "id=" + id +
               ", type=" + type +
               ", quality=" + quality +
               ", name='" + name + '\'' +
               ", baseProperty=" + baseProperty +
               ", skill=" + skill +
               ", randSkillNum=" + randSkillNum +
               ", maxSkillNum=" + maxSkillNum +
               '}';
    }
}
