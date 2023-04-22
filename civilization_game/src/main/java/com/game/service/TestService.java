package com.game.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.constant.AwardType;
import com.game.constant.LordPropertyType;
import com.game.constant.Reason;
import com.game.domain.Player;
import com.game.manager.LordManager;
import com.game.manager.PlayerManager;
import com.game.util.LogHelper;

@Service
public class TestService {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private LordManager lordManager;

	public void addItemTo(Long roleId, int type, int id, int count) {
		Player target = playerManager.getPlayer(roleId);
		if (target == null) {
			LogHelper.CONFIG_LOGGER.info("玩家对象为空");
			return;
		}
		if (type == AwardType.LORD_PROPERTY && id == LordPropertyType.VIP_LEVEL) {
			lordManager.setVipLevel(target.getLord(), count, Reason.GM_TOOL);
		} else {
			playerManager.addAward(target, type, id, count, Reason.GM_TOOL);
		}
	}
}
