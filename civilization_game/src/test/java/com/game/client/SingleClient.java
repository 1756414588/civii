package com.game.client;

import java.util.concurrent.TimeUnit;

import com.game.pb.BasePb;
import com.game.pb.BasePb.Base;
import com.google.protobuf.ExtensionRegistry;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class SingleClient {
	private static ManualResetEvent mre;
	private static Boolean isConnected;
	private static Channel channel;
	public static Boolean connectServer(String ip, Integer port) {
		// 1.定义服务类
		Bootstrap clientBootstap = new Bootstrap();

		// 2.定义执行线程组
		EventLoopGroup worker = new NioEventLoopGroup();

		// 3.设置线程池
		clientBootstap.group(worker);

		// 4.设置通道
		clientBootstap.channel(NioSocketChannel.class);

		// 5.添加Handler
		clientBootstap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				System.out.println("client channel init!");
				ChannelPipeline pipeline = channel.pipeline();
				pipeline.addLast(new IdleStateHandler(360, 0, 0, TimeUnit.SECONDS));
				pipeline.addLast("frameEncoder", new LengthFieldPrepender(2));
				pipeline.addLast("protobufEncoder", new ProtobufEncoder());

				pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 2, 0, 2));
				pipeline.addLast("protobufDecoder", new ProtobufDecoder(BasePb.Base.getDefaultInstance(), ExtensionRegistry.newInstance()));
				pipeline.addLast("protobufHandler", new MessageHandler());
			}
		});

		// 6.建立连接
		ChannelFuture channelFuture = clientBootstap.connect(ip, port);
		mre = new ManualResetEvent(false);
		channelFuture.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				isConnected = future.isSuccess();
				channel = isConnected ? future.channel() : null;
				mre.set();
			}
		});
		mre.waitOne();
		return isConnected;
	}
	
	public static boolean isConnected() {
		return isConnected;
	}
	
	public static void sendMsg(Base.Builder baseBuilder) {
		ChannelFuture cf = channel.writeAndFlush(baseBuilder);
		cf.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					System.out.println("发送成功");
				} else {
					System.out.println("发送失败");
				}
				
			}
		});
	}
}
