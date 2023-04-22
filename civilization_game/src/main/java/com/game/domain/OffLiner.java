package com.game.domain;

/**
 * 下线
 */
public class OffLiner {

    private Player player;
//    private ChannelHandlerContext ctx;
    private long channelId = 0;

    public OffLiner(Player player, long channelId) {
        this.player = player;
        this.channelId = channelId;
        // 十分钟之后移出offline和onlineMap
        player.removeOnlineTime = System.currentTimeMillis() + 600000;
    }

    public long getPlayerId() {
        return player.getRoleId();
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
}
