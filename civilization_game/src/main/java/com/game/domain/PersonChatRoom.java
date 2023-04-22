package com.game.domain;

import com.game.pb.DataPb;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author CaoBing
 * @date 2021/2/22 17:19
 */
public class PersonChatRoom {
    private long roomId;
    private long replayRoomId;
    private long lordId;
    private long replayLordId;
    private List<PersonChat> chats = new LinkedList<>();

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public long getReplayLordId() {
        return replayLordId;
    }

    public void setReplayLordId(long replayLordId) {
        this.replayLordId = replayLordId;
    }

    public List<PersonChat> getChats() {
        return chats;
    }

    public void setChats(List<PersonChat> chats) {
        this.chats = chats;
    }

    public long getReplayRoomId() {
        return replayRoomId;
    }

    public void setReplayRoomId(long replayRoomId) {
        this.replayRoomId = replayRoomId;
    }

    public DataPb.PersonChatRoomData.Builder writeData() {
        DataPb.PersonChatRoomData.Builder builder = DataPb.PersonChatRoomData.newBuilder();
        builder.setRoomId(roomId);
        builder.setReplayRoomId(replayRoomId);
        builder.setLordId(lordId);
        builder.setReplayLordId(replayLordId);

        for (PersonChat chat : chats) {
            if (null != chat) {
                DataPb.PersonChatData.Builder chatData = DataPb.PersonChatData.newBuilder();
                chatData.setLordId(chat.getLordId());
                chatData.setState(chat.getState());
                chatData.setCreateTime(chat.getCreateTime());
                chatData.setRoomId(chat.getRoomId());
                chatData.setMsg(chat.getMsg());
                builder.addChats(chatData);
            }
        }
        return builder;
    }

    public void readData(DataPb.PersonChatRoomData build) {
        roomId = build.getRoomId();
        replayRoomId = build.getReplayRoomId();
        lordId = build.getLordId();
        replayLordId = build.getReplayLordId();

        chats.clear();
        for (DataPb.PersonChatData personChatData : build.getChatsList()) {
            PersonChat chat = new PersonChat();
            chat.setLordId(personChatData.getLordId());
            chat.setState(personChatData.getState());
            chat.setCreateTime(personChatData.getCreateTime());
            chat.setRoomId(personChatData.getRoomId());
            chat.setMsg(personChatData.getMsg());
            chats.add(chat);
        }
    }

    @Override
    public String toString() {
        return "PersonChatRoom{" +
               "roomId=" + roomId +
               ", replayRoomId=" + replayRoomId +
               ", lordId=" + lordId +
               ", replayLordId=" + replayLordId +
               ", chats=" + chats +
               '}';
    }
}
