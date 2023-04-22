package com.game.domain.s;

import java.util.List;

// 抢夺名城
public class StaticActStealCity {
    private int keyId;
    private List<List<Integer>> time1;
    private List<List<Integer>> time2;
    private List<List<Integer>> time3;
    private List<Integer> city1;
    private List<Integer> city2;
    private List<Integer> city3;
    private List<List<Integer>> awards;
    private int num;
    private int warPeriod;//抢夺名称阵营战的时间(分钟)

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public List<List<Integer>> getTime1() {
        return time1;
    }

    public void setTime1(List<List<Integer>> time1) {
        this.time1 = time1;
    }

    public List<List<Integer>> getTime2() {
        return time2;
    }

    public void setTime2(List<List<Integer>> time2) {
        this.time2 = time2;
    }

    public List<List<Integer>> getTime3() {
        return time3;
    }

    public void setTime3(List<List<Integer>> time3) {
        this.time3 = time3;
    }

    public List<Integer> getCity1() {
        return city1;
    }

    public void setCity1(List<Integer> city1) {
        this.city1 = city1;
    }

    public List<Integer> getCity2() {
        return city2;
    }

    public void setCity2(List<Integer> city2) {
        this.city2 = city2;
    }

    public List<Integer> getCity3() {
        return city3;
    }

    public void setCity3(List<Integer> city3) {
        this.city3 = city3;
    }

    public List<List<Integer>> getAwards() {
        return awards;
    }

    public void setAwards(List<List<Integer>> awards) {
        this.awards = awards;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getWarPeriod() {
        return warPeriod;
    }

    public void setWarPeriod(int warPeriod) {
        this.warPeriod = warPeriod;
    }
}
