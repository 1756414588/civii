package com.game.define;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author 陈奎
 * @Description 世界活动
 * @Date 2022/9/9 11:30
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WorldActCmd {

    /**
     * 活动ID
     *
     * @return
     */
    int actId();

    /**
     * 活动名称
     *
     * @return
     */
    String actName();

}
