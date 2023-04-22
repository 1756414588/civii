package com.game.dao.uc;

/**
 * 2020年5月19日
 *
 * @CaoBing halo_uc
 * StaticChanelConfigDao.java
 **/

import com.game.pay.channel.PlayerExist;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface UServerInfoDao {
	void insertPlayerExist(PlayerExist playerExist);

	@MapKey("accountKey")
	Map<Integer, PlayerExist> findPlayerExist(int serverId);

	List<PlayerExist> findAllPlayerExist();

	void updatePlayerExist(PlayerExist playerExist);

	List<PlayerExist> load(@Param(value = "parMap") Map<Integer, List<Integer>> parMap);

	List<PlayerExist> loadOne(@Param("accountKey") long accountKey, @Param("serverId") int serverId);
}
