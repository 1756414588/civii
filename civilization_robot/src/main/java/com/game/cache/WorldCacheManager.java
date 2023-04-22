package com.game.cache;

import com.game.domain.WarCache;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/12/11 17:05
 **/

@Component
public class WorldCacheManager {

	private Map<Long, WarCache> warCacheMap = new ConcurrentHashMap<>();


	public WarCache putWar(WarCache warCache) {
		return warCacheMap.putIfAbsent(warCache.getWarId(), warCache);
	}

	public WarCache getWar(long warId) {
		return warCacheMap.get(warId);
	}

	public Map<Long, WarCache> getWarCache() {
		return warCacheMap;
	}

}
