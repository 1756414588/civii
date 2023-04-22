package com.game.define;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @Description 战斗处理接口
 * @Date 2022/9/9 11:30
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Fight {

	/**
	 * 战斗类型
	 *
	 * @return
	 */
	int[] warType();


	String warName();

	/**
	 * 行军类型
	 *
	 * @return
	 */
	int[] marthes();
}
