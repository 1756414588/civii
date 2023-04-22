package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2021/3/9 16:59
 */
public class StaticBeautyDateSkills {
    private int id;//主键
    private String desc;//描述
    private List<List<Integer>> value;//技能影响值
    private int needNum;//需要的亲密度
    private String level;
    private int beautyId;//美女ID
    private int star;//美女星级

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<List<Integer>> getValue() {
        return value;
    }

    public void setValue(List<List<Integer>> value) {
        this.value = value;
    }

    public int getNeedNum() {
        return needNum;
    }

    public void setNeedNum(int needNum) {
        this.needNum = needNum;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getBeautyId() {
        return beautyId;
    }

    public void setBeautyId(int beautyId) {
        this.beautyId = beautyId;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    @Override
    public String toString() {
        return "StaticBeautyDateSkills{" +
                "id=" + id +
                ", desc='" + desc + '\'' +
                ", value=" + value +
                ", needNum=" + needNum +
                ", level='" + level + '\'' +
                ", beautyId=" + beautyId +
                ", star=" + star +
                '}';
    }
}
