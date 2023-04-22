
package com.game.network;

import com.game.packet.PacketDecoder;
import com.game.packet.PacketEncoder;
import com.game.util.LogHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author 陈奎
 * @Description 远程连接
 * @Date 2022/9/9 11:30
 **/

public class RemoteNet extends Net {

	public static Logger logger = LoggerFactory.getLogger(RemoteNet.class);
	// 连接
	public INetContext context;

	protected Bootstrap bootstrap;
	protected EventLoopGroup eventLoopGroup;

	public RemoteNet() {
	}

	public RemoteNet(INetContext context, IPacketHandler packetHandler) {
		this.context = context;
		this.app = context.app();
		this.packetHandler = packetHandler;
	}

	public void startConnect() {
		bootstrap = new Bootstrap();
		eventLoopGroup = new NioEventLoopGroup();
		bootstrap.group(eventLoopGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new PacketDecoder());
				ch.pipeline().addLast(new PacketEncoder());
				ch.pipeline().addLast("handler", new ChannelHandler(RemoteNet.this, context));
			}
		});
		LogHelper.CHANNEL_LOGGER.info("net connect {}:{}", context.host(), context.port());
		connectToServer();
	}

	/**
	 * 连接目标服务器
	 */
	public void connectToServer() {
		final ChannelFuture future = bootstrap.connect(context.host(), context.port());
		future.addListener(new GenericFutureListener<Future<? super Void>>() {
			@Override
			public void operationComplete(Future<? super Void> e) throws Exception {
				LogHelper.CHANNEL_LOGGER.info("connectToServer {}:{}", context.host(), context.port());
				if (!e.isSuccess()) {
					future.channel().eventLoop().schedule(new Runnable() {
						@Override
						public void run() {
							connectToServer();
						}
					}, 5, TimeUnit.SECONDS);
				} else {
				}
			}
		});
	}


	@Override
	public void close() {
		if (eventLoopGroup != null) {
			eventLoopGroup.shutdownGracefully();
		}
	}
}


