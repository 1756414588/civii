package com.game.rank;

/**
 * @author jyb
 * @date 2020/5/7 14:40
 * @description
 */
public class CountryScore implements Comparable<CountryScore> {

    private int countryId;


    private int score;

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public CountryScore(int countryId, int score) {
        this.countryId = countryId;
        this.score = score;
    }

    @Override
    public int compareTo(CountryScore o) {
        return o.score - score;
    }
}
