package com.game.domain.s;

import java.util.List;

public class StaticCapacityTimes {
    private int times;
    private int price;
    private int capacity;
    private int queue;
    private int star;
    private List<List<Integer>> award;

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getQueue() {
        return queue;
    }

    public void setQueue(int queue) {
        this.queue = queue;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public List<List<Integer>> getAward () {
        return award;
    }

    public void setAward (List<List<Integer>> award) {
        this.award = award;
    }
}
