package com.game.message.handler;

import com.game.constant.GameError;
import com.game.domain.Player;
import com.game.network.ChannelUtil;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.server.netserver.MessageFilter;
import com.game.util.LogHelper;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;


abstract public class ClientHandler extends Handler {


    //private Long roleId;
    //private Packet packet;
    public static Map<Integer, Handler> map = new HashMap<>();


    public Long getRoleId() {
        return ChannelUtil.getRoleId(ctx);
    }

    public void add(int cmd, int rdId, Handler handler) {
        map.put(cmd, handler);
        handler.setRsMsgCmd(rdId);
    }
    //    public Packet getPacket() {
//        return packet;
//    }

//    public void setPacket(Packet packet) {
//        this.packet = packet;
//        this.channelId = packet.getChannelId();
//        try {
//            if (packet.getBytes() != null && packet.getBytes().length > 0) {
//                this.msg = Base.parseFrom(packet.getBytes(), PBFile.registry);
//            }
//        } catch (InvalidProtocolBufferException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public DealType dealType() {
        return DealType.MAIN;
    }

    public void sendErrorMsgToPlayer(GameError gameError) {
        Base.Builder baseBuilder = createRsBase(gameError.getCode());
        sendMsgToPlayer(baseBuilder);
    }

    public <T> void sendMsgToPlayer(GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = createRsBase(GameError.OK, ext, msg);
        sendMsgToPlayer(baseBuilder);
    }

    public <T> void sendMsgToPlayer(ChannelHandlerContext context, GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = createRsBase(GameError.OK, ext, msg);
        context.writeAndFlush(baseBuilder);
    }

    public <T> void sendMsgToPlayer(GameError gameError, GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = createRsBase(gameError, ext, msg);
        sendMsgToPlayer(baseBuilder);
    }

    public void sendMsgToPlayer(Base.Builder builder) {
        getCtx().writeAndFlush(builder);
    }

}
