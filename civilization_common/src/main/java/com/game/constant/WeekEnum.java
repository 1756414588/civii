package com.game.constant;

import java.util.Calendar;

/**
 *
 * @date 2020/3/31 11:22
 * @description
 */
public enum WeekEnum {

    MONDAY(1, Calendar.MONDAY),

    TUESDAY(2, Calendar.TUESDAY),


    WEDNESDAY(3, Calendar.WEDNESDAY),


    THURSDAY(4, Calendar.THURSDAY),

    FRIDAY(5, Calendar.FRIDAY),

    SATURDAY(6, Calendar.SATURDAY),

    SUNDAY(7, Calendar.SUNDAY),


    ;
    /**
     * 中国的星期几
     */
    private int chinaWeek;

    /***
     * 美国的星期几
     */
    private int week;

    WeekEnum(int chinaWeek, int week) {
        this.chinaWeek = chinaWeek;
        this.week = week;
    }

    public static int getUsaWeek(int chinaWeek) {
        for (WeekEnum week : WeekEnum.values()) {
            if (week.chinaWeek == chinaWeek) {
                return week.week;
            }
        }
        return 1;
    }
}
