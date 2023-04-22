package com.game.define;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @Description 初始化接口
 * @Date 2022/9/9 11:30
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LoadData {

	String name();

	/**
	 * 初始化数据方法加载顺序
	 *
	 * @return
	 */
	int initSeq();
}
