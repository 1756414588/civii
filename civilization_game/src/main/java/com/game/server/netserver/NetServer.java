package com.game.server.netserver;

import com.game.define.App;
import com.game.domain.Player;
import com.game.manager.PlayerManager;
import com.game.manager.ServerManager;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.Handler;
import com.game.network.ChannelUtil;
import com.game.pb.BasePb;
import com.game.register.PBFile;
import com.game.server.AbsServer;
import com.game.server.GameServer;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @Description 内部服务器器
 * @Date 2022/9/9 11:30
 **/

public class NetServer extends AbsServer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private GlobalTrafficShapingHandler trafficShapingHandler;
    private ServerBootstrap bootstrap;

    public NetServer() {
        super(App.GAME.getName());
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void run() {
        // 定义两个工作线程 bossGroup workerGroup 用于管理channel连接
        // 负责tcp客户端的连接请求
        bossGroup = new NioEventLoopGroup(1);
        // 真正负责IO读写的现场组
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
            ServerManager serverManager = SpringUtil.getBean(ServerManager.class);
            LogHelper.MESSAGE_LOGGER.info("success port:{}", serverManager.getNetPort());
            f = bootstrap.bind(serverManager.getNetPort()).sync();
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LogHelper.ERROR_LOGGER.error("ConnectServer", e);
        }
    }

    @Override
    public String getGameType() {
        return "connect";
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
            pipeLine.addLast("protobufDecoder", new ProtobufDecoder(BasePb.Base.getDefaultInstance(), PBFile.registry));
            pipeLine.addLast("protobufHandler", new MessageHandler(NetServer.this));
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

    @ChannelHandler.Sharable
    class MessageHandler extends SimpleChannelInboundHandler<BasePb.Base> {

        private NetServer server;

        public MessageHandler(NetServer server) {
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
        protected void channelRead0(ChannelHandlerContext ctx, BasePb.Base msg) throws Exception {
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
            ChannelUtil.setRoleId(ctx, 0L);
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

    public void doCommand(ChannelHandlerContext ctx, BasePb.Base msg) {

        try {
//            GameServer gameServer = GameServer.getInstance();
//            int cmd = msg.getCommand();
//            ClientHandler handler = gameServer.messagePool.getClientHandler(cmd);
//            if (handler == null) {
//                return;
//            }
            Handler handler = ClientHandler.map.get(msg.getCommand());
            if (handler == null) {
                return;
            }

            Long roleId = ChannelUtil.getRoleId(ctx);
            Player player = null;
            if (roleId > 0) {
                PlayerManager bean = SpringUtil.getBean(PlayerManager.class);
                player = bean.getPlayer(roleId);
                if (player == null) {
                    return;
                }
            }

            handler.setCtx(ctx);
            handler.setMsg(msg);
            handler.action();
            handler.event(msg, ctx, player);
        } catch (Exception e) {
        }
    }
}


