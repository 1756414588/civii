package com.game.cache;

import com.game.domain.ChatShare;
import com.google.common.collect.HashBasedTable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 *
 * @Description聊天分享管理
 * @Date 2022/10/27 15:00
 **/

@Component
public class ChatCacheManager {

	@Getter
	private Map<Long, ChatShare> shares = new ConcurrentHashMap<>();

	public boolean contain(long id) {
		return shares.containsKey(id);
	}

	public void put(ChatShare chatShare) {
		shares.put(chatShare.getId(), chatShare);
	}


}
