package com.game.load;

/**
 * @Author 陈奎
 * @Description 启动服务器时初始化的接口
 * @Date 2022/9/9 11:30
 **/

public interface ILoadData {

	/**
	 * 将数据由数据库加载到内存,所有模块的load方法都是无序调用的，且所有模块load执行完成之后,再按顺序执行模块的ini方法
	 */
	void load() throws Exception;

	/**
	 * 按注解LoadData的initSeq值,进行升序调用该初始化方法
	 */
	void init() throws Exception;

}
