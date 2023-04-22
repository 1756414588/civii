package com.game.worldmap;

import com.game.domain.Player;
import com.game.flame.NodeType;
import com.game.pb.CommonPb;

import java.util.LinkedList;

public class PlayerCity extends Entity {
    // 这些信息最好从玩家身上取
    private long lordId; // 玩家Id
    private int commandLv; // 司令部等级, 需要定时更新
    private int country; // 国家, 需要定时更新
    private String name; // 姓名
    private int assitNum; // 可驻防武将
    private int maxMonsterLv; // 最高击杀的怪物等级
    private int callCount; // 可召唤总人数
    private int callReply; // 召唤应答人数
    private long callEndTime; // 召唤结束时间
    private LinkedList<FriendArmy> friendArmies = new LinkedList<FriendArmy>(); // 友军城防
    private Player player; // 玩家,TODO

    public int getCommandLv() {
        return commandLv;
    }

    public void setCommandLv(int commandLv) {
        this.commandLv = commandLv;
    }

    @Override
    public NodeType getNodeType() {
        return null;
    }

    @Override
    public int getNodeState() {
        return 0;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAssitNum() {
        return assitNum;
    }

    public void setAssitNum(int assitNum) {
        this.assitNum = assitNum;
    }

    public LinkedList<FriendArmy> getFriendArmies() {
        return friendArmies;
    }

    public void setFriendArmies(LinkedList<FriendArmy> friendArmies) {
        this.friendArmies = friendArmies;
    }

    public int getMaxMonsterLv() {
        return maxMonsterLv;
    }

    public void setMaxMonsterLv(int maxMonsterLv) {
        this.maxMonsterLv = maxMonsterLv;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }

    public int getCallReply() {
        return callReply;
    }

    public void setCallReply(int callReply) {
        this.callReply = callReply;
    }

    public long getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(long callEndTime) {
        this.callEndTime = callEndTime;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public CommonPb.WorldEntity.Builder wrapPb() {
        CommonPb.WorldEntity.Builder builder = super.wrapPb();
        builder.setCountry(country);
        if (player != null) {
            if (player.getNick() == null) {
                builder.setName("");
            } else {
                builder.setName(player.getNick());
            }

            builder.setProtectedTime(player.getProectedTime());
            builder.setCallCount(callCount);
            builder.setCallReply(callReply);
            builder.setCallEndTime(callEndTime);
            builder.setLevel(player.getCommandLv());
            boolean isBreak = player.getSimpleData().isWaveContinue();
            builder.setIsBreak(isBreak);
            builder.setIsAttack(!player.getSimpleData().getRiotWar().isEmpty());
            builder.setSkin(player.getLord().getSkin());
        }
        return builder;
    }
}
