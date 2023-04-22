package com.game.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.UUID;

/**
 *
 * @Description 连接属性操作类
 * @Date 2022/9/9 11:30
 **/

public class ChannelUtil {

	public static Long getChannelId(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		return channel.attr(ChannelAttr.ID).get();
	}

	public static void setChannelId(ChannelHandlerContext ctx, Long id) {
		Channel channel = ctx.channel();
		channel.attr(ChannelAttr.ID).set(id);
	}

	public static Integer getAccountKey(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		return channel.attr(ChannelAttr.ACCOUNT_KEY).get();
	}

	public static void setAccountKey(ChannelHandlerContext ctx, Integer id) {
		Channel channel = ctx.channel();
		channel.attr(ChannelAttr.ACCOUNT_KEY).set(id);
	}

	public static Long createChannelId(ChannelHandlerContext ctx) {
		InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
		String ip = address.getAddress().getHostAddress();
		int port = address.getPort();
		Long id = ip2long(ip) * 100000L + port;
		return id;
	}

	public static void setRoleId(ChannelHandlerContext ctx, Long roleId) {
		Channel channel = ctx.channel();
		channel.attr(ChannelAttr.roleId).set(roleId);
	}

	public static void setHeartTime(ChannelHandlerContext ctx, Long nowTime) {
		Channel channel = ctx.channel();
		channel.attr(ChannelAttr.heartTime).set(nowTime);
	}

	public static Long getRoleId(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		return channel.attr(ChannelAttr.roleId).get();
	}

	public static Long getHeartTime(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		return channel.attr(ChannelAttr.heartTime).get();
	}

	public static void setNetId(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		String id = UUID.randomUUID().toString();
		channel.attr(ChannelAttr.NET_ID).set(id);
	}

	public static String getNetId(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		return channel.attr(ChannelAttr.NET_ID).get();
	}

	public static <T> void setAttribute(ChannelHandlerContext ctx, String key, T value) {
		AttributeKey<T> attrKey = AttributeKey.valueOf(key);
		ctx.channel().attr(attrKey).set(value);
	}

	public static <T> T getAttribute(ChannelHandlerContext ctx, String key) {
		AttributeKey<T> attrKey = AttributeKey.valueOf(key);
		return ctx.channel().attr(attrKey).get();
	}

	public static <T> T getAttribute(ChannelFuture future, String key) {
		AttributeKey<T> attrKey = AttributeKey.valueOf(key);
		return future.channel().attr(attrKey).get();
	}

	/**
	 * IP转成整型
	 *
	 * @param ip
	 * @return
	 */
	private static Long ip2long(String ip) {
		Long num = 0L;
		if (ip == null) {
			return num;
		}

		try {
			ip = ip.replaceAll("[^0-9\\.]", ""); // 去除字符串前的空字符
			String[] ips = ip.split("\\.");
			if (ips.length == 4) {
				num = Long.parseLong(ips[0], 10) * 256L * 256L * 256L + Long.parseLong(ips[1], 10) * 256L * 256L + Long.parseLong(ips[2], 10) * 256L
					+ Long.parseLong(ips[3], 10);
				num = num >>> 0;
			}
		} catch (NullPointerException ex) {
			//System.out.println(ip);
		}

		return num;
	}
}
