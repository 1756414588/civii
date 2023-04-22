package com.game.util;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.game.pb.CommonPb;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @date 2020/12/2 17:35
 * @description
 */
@Slf4j
public class StringUtil {
    public static final String POINT = ".";
    public static final String COMMA = ",";

    public static String arrToString(Object[] arr) {
        if (arr == null || arr.length == 0) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i < arr.length - 1) {
                builder.append(POINT);
            }
        }
        builder.append("]");
        return builder.toString().replaceAll("\n", "");
    }

    public static String intArrToString(int[] arr) {
        if (arr == null || arr.length == 0) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i < arr.length - 1) {
                builder.append(POINT);
            }
        }
        builder.append("]");
        return builder.toString().replaceAll("\n", "");
    }

    public static boolean isNullOrEmpty(Object s) {
        return s == null || s.toString().isEmpty();
    }


    public static <T> List<T> removeDuplicate(List<T> list) {
        List<T> listTemp = new ArrayList<T>();
        for (int i = 0; i < list.size(); i++) {
            if (!listTemp.contains(list.get(i))) {
                listTemp.add(list.get(i));
            }
        }
        return listTemp;
    }

    public static void main(String[] args) {
        List<CommonPb.Award> list = new ArrayList<>();
        list.add(CommonPb.Award.newBuilder()
                .setId(1)
                .setType(1)
                .setCount(1).build());
        System.out.println(arrToString(list.stream().toArray()));
    }

    public static List<Integer> stringToList(String string) {
        if (StringUtil.isNullOrEmpty(string)) {
            return new ArrayList<>();
        }
        List<Integer> list = new ArrayList<Integer>();
        try {
            JSONArray array = JSONArray.parseArray(string);
            for (int i = 0; i < array.size(); i++) {
                int value = array.getIntValue(i);
                list.add(value);
            }
        } catch (Exception e) {
            log.error("string error,{}",e);
        }

        return list;
    }

    public static String urlEncode(String string) {
        return string.replaceAll(",", "%2C");
    }
}
