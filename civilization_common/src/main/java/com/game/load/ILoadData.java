package com.game.load;

/**
 *
 * @Description 数据加载以及初始化接口
 * @Date 2022/9/9 11:30
 **/

public interface ILoadData {

	/**
	 * 将数据由数据库加载到内存,不参与关联运算
	 */
	void load();

	/**
	 * 将内存的数据进行关联运算
	 */
	void init();

}
