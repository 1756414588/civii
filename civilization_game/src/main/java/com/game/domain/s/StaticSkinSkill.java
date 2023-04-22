package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2021/3/18 9:57
 * 主城皮肤技能的配置类
 */
public class StaticSkinSkill {
    private int id;
    private int baseId;
    private int level;
    private List<List<Integer>> effectValue;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBaseId() {
        return baseId;
    }

    public void setBaseId(int baseId) {
        this.baseId = baseId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<List<Integer>> getEffectValue() {
        return effectValue;
    }

    public void setEffectValue(List<List<Integer>> effectValue) {
        this.effectValue = effectValue;
    }

    @Override
    public String toString() {
        return "StaticSkinSkill{" +
               "id=" + id +
               ", baseId=" + baseId +
               ", level=" + level +
               ", effectValue=" + effectValue +
               '}';
    }
}
