package com.game.server.datafacede;

import com.game.define.DataFacede;
import com.game.domain.Role;
import com.game.manager.PlayerManager;
import com.game.manager.PublicDataManager;
import com.game.server.thread.SavePlayerThread;
import com.game.server.thread.SaveServer;
import com.game.server.thread.SaveThread;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @Description 玩家数据存储服务
 * @Date 2022/9/9 11:30
 **/

@DataFacede(desc = "玩家数据存储")
@Service
public class SavePlayerServer extends SaveServer<Role> {

	public SavePlayerServer() {
		super("SAVE_PLAYER_SERVER", 128);
	}

	public SaveThread createThread(String name) {
		return new SavePlayerThread(name);
	}

	@Override
	public void saveData(Role role) {
		SaveThread thread = threadPool.get((int) (role.getRoleId() % threadNum));
		thread.add(role);
	}

	@Override
	public void saveAll() {
		PlayerManager playerDataManager = SpringUtil.getBean(PlayerManager.class);
		int now = TimeHelper.getCurrentSecond();

		// 只需存储登录过的玩家
		playerDataManager.getPlayers().values().stream().filter(e -> e.getFullLoad().get() && e.isEntering()).forEach(player -> {
			try {
				player.lastSaveTime = now;
				saveData(new Role(player));
			} catch (Exception e) {
				LogHelper.ERROR_LOGGER.error("SAVE_PLAYER_SERVER playerId:{} cause:{}", player.getRoleId(), e.getMessage(), e);
			}
		});

		try {
			SpringUtil.getBean(PublicDataManager.class).update();
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("SAVE_PLAYER_SERVER PUBLIC_DATA:{}", e.getMessage(), e);
		}

	}
}
