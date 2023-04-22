package com.game.network;

import io.netty.util.AttributeKey;

/**
 *
 * @Description 连接属性
 * @Date 2022/9/9 11:30
 **/

public class ChannelAttr {

	public static AttributeKey<Long> heartTime = AttributeKey.valueOf("heart");
	public static AttributeKey<Long> roleId = AttributeKey.valueOf("roleId");
	public static AttributeKey<Long> ID = AttributeKey.valueOf("ID");
	public static AttributeKey<Integer> ACCOUNT_KEY = AttributeKey.valueOf("ACCOUNT_KEY");
	public static AttributeKey<String> NET_ID = AttributeKey.valueOf("INNER_NET_ID");

	// 内部连接ID
	public static String NET = "NET";
	public static String NET_SERVER_ID = "NET_SERVER_ID";
}
