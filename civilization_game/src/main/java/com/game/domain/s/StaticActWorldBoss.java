package com.game.domain.s;

import com.game.domain.Award;

import java.util.List;
import java.util.Random;

/**
 *
 * @date 2019/12/23 18:57
 * @description
 */
public class StaticActWorldBoss {
    private int id;
    private int bossHP;
    private int challengeNumber;
    private int challengeCost;
    private int awardPlayerExp;
    private int bossModel;
    private List<List<Integer>> randomDrop;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getChallengeNumber() {
        return challengeNumber;
    }

    public void setChallengeNumber(int challengeNumber) {
        this.challengeNumber = challengeNumber;
    }

    public int getBossHP() {
        return bossHP;
    }

    public void setBossHP(int bossHP) {
        this.bossHP = bossHP;
    }

    public int getChallengeCost() {
        return challengeCost;
    }

    public void setChallengeCost(int challengeCost) {
        this.challengeCost = challengeCost;
    }

    public int getAwardPlayerExp() {
        return awardPlayerExp;
    }

    public void setAwardPlayerExp(int awardPlayerExp) {
        this.awardPlayerExp = awardPlayerExp;
    }

    public int getBossModel() {
        return bossModel;
    }

    public void setBossModel(int bossModel) {
        this.bossModel = bossModel;
    }

    public List<List<Integer>> getRandomDrop() {
        return randomDrop;
    }

    public void setRandomDrop(List<List<Integer>> randomDrop) {
        this.randomDrop = randomDrop;
    }


    public Award getAward() {
        int rand = 0;
        int random = new Random().nextInt(1000);
        for (List<Integer> integers : randomDrop) {
            rand += integers.get(3);
            if (random < rand) {
                return new Award(integers);
            }
        }
        return null;
    }

}
