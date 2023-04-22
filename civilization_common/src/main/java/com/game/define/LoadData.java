package com.game.define;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author 陈奎
 * @Description 初始化接口
 * @Date 2022/9/9 11:30
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LoadData {

	String name();

	/**
	 * ini方法加载顺序.顺时针加载
	 *
	 * @return
	 */
	int initSeq() default 1000;

	/**
	 * 1.配置 2.用户数据
	 *
	 * @return
	 */
	int type() default 1;
}
