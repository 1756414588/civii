package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2020/12/10 15:11
 * 兵书分解的配置
 */
public class StaticWarBookDecom {
    private int id;        //主键id
    private int quality; //被分解兵书的质量
    private int level;        //被分解兵书的等级
    private List<List<Integer>> baseStrongProp;    //兵书分解得到的基础强化道具数量
    private List<List<Integer>> decomposeAward;    //分解兵书得到的兵书强化道具和兵书购买道具数量
    private List<List<Integer>> warbookProability;    //获得新的兵书的概率

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<List<Integer>> getBaseStrongProp() {
        return baseStrongProp;
    }

    public void setBaseStrongProp(List<List<Integer>> baseStrongProp) {
        this.baseStrongProp = baseStrongProp;
    }

    public List<List<Integer>> getDecomposeAward() {
        return decomposeAward;
    }

    public void setDecomposeAward(List<List<Integer>> decomposeAward) {
        this.decomposeAward = decomposeAward;
    }

    public List<List<Integer>> getWarbookProability() {
        return warbookProability;
    }

    public void setWarbookProability(List<List<Integer>> warbookProability) {
        this.warbookProability = warbookProability;
    }

    @Override
    public String toString() {
        return "StaticWarBookDecom{" +
               "id=" + id +
               ", quality=" + quality +
               ", level=" + level +
               ", baseStrongProp=" + baseStrongProp +
               ", decomposeAward=" + decomposeAward +
               ", warbookProability=" + warbookProability +
               '}';
    }
}
