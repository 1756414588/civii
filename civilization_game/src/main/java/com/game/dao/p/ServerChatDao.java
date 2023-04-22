package com.game.dao.p;

import com.game.domain.p.Chat;

import java.util.List;

public interface ServerChatDao {
	public void cleanChat();
	
	public int insertSelective(Chat chat);

	public List<Chat> selectAllServerChats();
}
