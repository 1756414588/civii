package com.game.domain.s;

import java.util.List;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/7 11:39
 **/
public class StaticEndlessAward {

	private int id;
	private int rankup;
	private int rankdown;
	private List<List<Integer>> award;
    private String title;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRankup() {
        return rankup;
    }

    public void setRankup(int rankup) {
        this.rankup = rankup;
    }

    public int getRankdown() {
        return rankdown;
    }

    public void setRankdown(int rankdown) {
        this.rankdown = rankdown;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
