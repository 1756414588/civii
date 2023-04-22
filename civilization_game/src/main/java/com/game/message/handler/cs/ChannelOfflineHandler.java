package com.game.message.handler.cs;

import com.game.domain.OffLiner;
import com.game.domain.Player;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.log.domain.LoginLog;
import com.game.manager.PlayerManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.InnerPb.ChannelOfflineRq;
import com.game.server.exec.HttpExecutor;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.Date;

public class ChannelOfflineHandler extends ClientHandler {

	@Override
	public void action() {
		long channelId = getChannelId();

		ChannelOfflineRq offlineRq = msg.getExtension(ChannelOfflineRq.ext);
		long roleId = offlineRq.getUserId();

		LogHelper.GAME_LOGGER.info("接收玩家下线 channelId:{} playerId:{}", channelId, roleId);

		if (roleId == 0) {
			return;
		}

		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		Player player = playerManager.getPlayer(roleId);
		if (player.getChannelId() != channelId) {
			return;
		}

		playerManager.offLine(new OffLiner(player, channelId));

		playerExit(player, channelId);
		LogHelper.GAME_LOGGER.info("玩家下线 channelId:{} playerId:{}", channelId, roleId);

	}


	public void playerExit(Player player, long channelId) {
		if (player == null) {
			return;
		}

		// 重复登录
		if (player.getChannelId() != -1 && player.getChannelId() != channelId) {
			player.immediateSave = true;
			return;
		}

		player.logOut();
		// 更新账号服玩家信息
		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);

		SpringUtil.getBean(HttpExecutor.class).add(() -> {
			playerManager.saveUcServerInfos(player);
		});

		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.loginLog(LoginLog.builder().lordId(player.roleId).nick(player.getNick()).lv(player.getLevel()).createDate(player.account.getCreateDate()).loginDate(player.account.getLoginDate()).logoutDate(new Date(System.currentTimeMillis())).build());
		EventManager eventManager = SpringUtil.getBean(EventManager.class);
		eventManager.quit_account(player);
		eventManager.ta_app_end(player);
		eventManager.record_userInfo(player, EventName.quit_account);
		eventManager.record_userInfo_once(player, EventName.first_leave);
		eventManager.record_onlineTime(player, EventName.onlin_time);
	}
}