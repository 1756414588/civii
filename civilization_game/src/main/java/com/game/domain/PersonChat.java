package com.game.domain;

import com.game.manager.PlayerManager;
import com.game.pb.CommonPb;
import com.game.spring.SpringUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author CaoBing
 * @date 2021/2/22 17:15
 */
@Getter
@Setter
public class PersonChat {
    private long lordId;
    private int state;
    private long createTime;
    private long roomId;
    private String msg;

    public PersonChat() {
    }

    @Builder
    public PersonChat(long lordId, long createTime, long roomId, String msg) {
        this.lordId = lordId;
        this.createTime = createTime;
        this.roomId = roomId;
        this.msg = msg;
    }

    public CommonPb.PersonChat.Builder serShow() {
        CommonPb.PersonChat.Builder builder = CommonPb.PersonChat.newBuilder();
        builder.setState(state);
        builder.setLordId(lordId);
        builder.setCreateTime(createTime);
        builder.setRoomId(roomId);
        builder.setMsg(msg);
        PlayerManager acBean = SpringUtil.getBean(PlayerManager.class);
        Player player = acBean.getPlayer(lordId);
        builder.setPortrait(player.getLord().getPortrait());
        builder.setCountry(player.getLord().getCountry());
        builder.setLevle(player.getLord().getLevel());
        builder.setNick(player.getNick());
        builder.setSelfHeadSculpture(player.getLord().getHeadIndex());

        return builder;
    }

    @Override
    public String toString() {
        return "PersonChat{" +
               "lordId=" + lordId +
               ", state=" + state +
               ", createTime=" + createTime +
               ", roomId=" + roomId +
               ", msg='" + msg + '\'' +
               '}';
    }
}
