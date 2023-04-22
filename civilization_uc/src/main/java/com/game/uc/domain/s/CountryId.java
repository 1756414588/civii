package com.game.uc.domain.s;

import java.util.ArrayList;
import java.util.List;

public class CountryId {
    public static final int country1 = 1;  //联邦
    public static final int country2 = 2;  //帝国
    public static final int country3 = 3;  //共和


    public static List<Integer> getCountryList() {
        List arrayList = new ArrayList();
        arrayList.add(country1);
        arrayList.add(country2);
        arrayList.add(country3);
        return arrayList;
    }
}
