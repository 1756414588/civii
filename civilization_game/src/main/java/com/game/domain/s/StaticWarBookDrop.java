package com.game.domain.s;

/**
 * @author CaoBing
 * @date 2020/12/19 18:40
 */
public class StaticWarBookDrop {
    private int id;    //keyId
    private int quality;    //掉落的兵书品质
    private int proability;	//掉落概率
    private int num;	//一次掉落的数量

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

    public int getProability() {
        return proability;
    }

    public void setProability(int proability) {
        this.proability = proability;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "StaticWarBookDrop{" +
               "id=" + id +
               ", quality=" + quality +
               ", proability=" + proability +
               ", num=" + num +
               '}';
    }
}
