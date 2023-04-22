package com.game.network.listen;


import com.game.network.ChannelHandler;
import com.game.network.INetContext;
import com.game.network.RemoteNet;
import com.game.network.RobotDecoder;
import com.game.network.RobotEncoder;
import com.game.network.IPacketHandler;
import com.game.util.LogHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 监听玩家行为连接
 */
public class ListenNet extends RemoteNet {

	@Override
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
				ch.pipeline().addLast(new RobotDecoder());
				ch.pipeline().addLast(new RobotEncoder());
				ch.pipeline().addLast("handler", new ChannelHandler(ListenNet.this, context));
			}
		});
		LogHelper.CHANNEL_LOGGER.info("net connect {}:{}", context.host(), context.port());
		connectToServer();
	}

	/**
	 * 包处理器
	 *
	 * @param packetHandler
	 */
	public void setPacketHandler(IPacketHandler packetHandler) {
		this.packetHandler = packetHandler;
	}

	/**
	 * 连接句柄
	 *
	 * @param context
	 */
	public void setConnect(INetContext context) {
		this.context = context;
	}

}
