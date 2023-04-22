package com.game.uc.netty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.game.pb.AccountLoginPb;
import com.game.pb.BasePb;
import com.game.pb.BasePb.Base;
import com.game.uc.netty.handler.BaseHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ExtensionRegistry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;

@Component
public class ConnectServer extends BaseServer {

    @Value("${http.server.netty.port}")
    private int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    public GlobalTrafficShapingHandler trafficShapingHandler;
    ServerBootstrap bootstrap;
    public static ExtensionRegistry registry = ExtensionRegistry.newInstance();
    private ExecutorService executor;

    static {
        AccountLoginPb.registerAllExtensions(registry);
    }

    @Override
    protected void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        executor.shutdown();
    }

    @PostConstruct
    public void start() {
        super.run();
        executor = new ThreadPoolExecutor(1, 50, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(4096), new ThreadFactoryBuilder().setNameFormat("thread-pool-%d").build(), new ThreadPoolExecutor.DiscardOldestPolicy());
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        trafficShapingHandler = new GlobalTrafficShapingHandler(workerGroup, 5000L);
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        // BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        // 通过NoDelay禁用Nagle,使消息立即发出去，不用等待到一定的数据量才发出去
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childHandler(new ConnectChannelHandler());
        // Netty4使用对象池，重用缓冲区
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        // 是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        ChannelFuture f;
        try {
            // 绑定端口，同步等待成功
            f = bootstrap.bind(port).sync();
            // 等待服务端监听端口关闭
            // f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            // TODO Auto+-generated catch block
        }
    }

    private class ConnectChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeLine = ch.pipeline();
            pipeLine.addLast(trafficShapingHandler);
            pipeLine.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
            pipeLine.addLast(new HeartbeatHandler());
            pipeLine.addLast("frameEncoder", new LengthFieldPrepender(2));
            pipeLine.addLast("protobufEncoder", new ProtobufEncoder());
            pipeLine.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 2, 0, 2));
            pipeLine.addLast("protobufDecoder", new ProtobufDecoder(BasePb.Base.getDefaultInstance(), registry));
            pipeLine.addLast("protobufHandler", new MessageHandler(ConnectServer.this));
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            ctx.close();
        }
    }

    public void doCommand(ChannelHandlerContext ctx, Base msg) {
        executor.execute(() -> {
            BaseHandler baseHandler = BaseHandler.map.get(msg.getCommand());
            if (baseHandler != null) {
                baseHandler.action(ctx, msg);
            }
        });
    }
}

class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        }
    }
}

class MessageHandler extends SimpleChannelInboundHandler<Base> {

    private ConnectServer server;

    public MessageHandler(ConnectServer server) {
        this.server = server;
    }

    /**
     * Overriding: channelRead0
     *
     * @param ctx
     * @param msg
     * @throws Exception
     * @see SimpleChannelInboundHandler#channelRead0(ChannelHandlerContext, Object)
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Base msg) throws Exception {
        // TODO Auto-generated method stub
        server.doCommand(ctx, msg);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}
