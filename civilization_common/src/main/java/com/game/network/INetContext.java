package com.game.network;

import com.game.define.App;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @Description 内部连接属性
 * @Date 2022/9/9 11:30
 **/

public interface INetContext {

	/**
	 * 连接应用
	 *
	 * @return
	 */
	App app();

	/**
	 * 连接地址
	 *
	 * @return
	 */
	String host();

	/**
	 * 端口号
	 *
	 * @return
	 */
	int port();

	/**
	 * 连接成功
	 */
	void onSucess(ChannelHandlerContext ctx);

	/**
	 * 断开连接
	 */
	void onDisconnect(ChannelHandlerContext ctx);

}
