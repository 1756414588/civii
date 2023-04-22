package com.game.domain.s;

// 血战兑换
public class StaticPvpExchange {
    private Integer id;
    private Integer type;
    private Integer propId;
    private Integer num;
    private Integer score;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getPropId() {
        return propId;
    }

    public void setPropId(Integer propId) {
        this.propId = propId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "StaticPvpExchange{" +
                "id=" + id +
                ", type=" + type +
                ", propId=" + propId +
                ", num=" + num +
                ", score=" + score +
                '}';
    }
}
