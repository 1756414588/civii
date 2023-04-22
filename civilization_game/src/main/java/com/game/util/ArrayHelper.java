package com.game.util;


import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ArrayHelper {
    // copy array from index = 1 to index = oldArray.size()-1
    public static List<Long> copyArray(List<Long> oldArray) {
        List<Long> param = new ArrayList<Long>();
        int length = oldArray.size();
        if (length > 1) {
            for (int i = 0; i < length -1; ++i) {
                param.add(oldArray.get(i+1));
            }
        }

        return param;
    }

    @Test
    public void testCopyArray() {
        List<Long> old = new ArrayList<Long>();
        for (Long i = 0L; i < 0; i++) {
            old.add(i+1);
        }

        List<Long> param = copyArray(old);
//        System.out.println("param length = " + param.size());
        for (int i =0; i < param.size(); i++) {
//            System.out.println("i = " + param.get(i));
        }
    }

}
