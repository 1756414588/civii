package com.game.util.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 2020年8月17日
 *
 *    halo_common
 * WeightRandom.java
 **/
public class WeightRandom {
    //static List<WeightCategory> categorys = new ArrayList<WeightCategory>();
    private static Random random = new Random();

    public static int initData(List<Integer> weigHtvalue) {
        List<WeightCategory> categorys = new ArrayList<WeightCategory>();
        for (int i = 0; i < weigHtvalue.size(); i++) {
            WeightCategory wc = new WeightCategory(i, weigHtvalue.get(i));
            categorys.add(wc);
        }

        int count = 0;
        Integer weightSum = 0;
        for (WeightCategory wc : categorys) {
            weightSum += wc.getWeight();
        }

        if (weightSum <= 0) {
            //System.err.println("Error: weightSum=" + weightSum.toString());
            return 0;
        }
        Integer n = random.nextInt(weightSum); // n in [0, weightSum)
        Integer m = 0;
        for (WeightCategory wc : categorys) {
            if (m <= n && n < m + wc.getWeight()) {
                count = wc.getCount();
                //System.out.println("This Random Category is " + wc.getCount());
                break;
            }
            m += wc.getWeight();
        }
        return count;
    }
}

class WeightCategory {
    private int count;
    private Integer weight;

    public WeightCategory() {
    }

    public WeightCategory(int count, Integer weight) {
        super();
        this.count = count;
        this.weight = weight;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "WeightCategory [count=" + count + ", weight=" + weight + "]";
    }
}
