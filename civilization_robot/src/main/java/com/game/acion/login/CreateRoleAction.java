package com.game.acion.login;

import com.game.acion.MessageEvent;
import com.game.cache.ConfigCache;
import com.game.domain.Robot;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.CreateRoleRq;
import com.game.spring.SpringUtil;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 *
 * @Description 创建角色
 * @Date 2022/9/19 10:50
 **/

@Component
public class CreateRoleAction extends EnterGameAction {

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		long eventId = messageEvent.getEventId();

		ConfigCache robotConfigData = SpringUtil.getBean(ConfigCache.class);
		String robotCounty = robotConfigData.getValueByKey("robot_country");
		int country = 1;
		if (robotCounty == null || robotCounty.equals("0")) {
			country = new Random().nextInt(3) + 1;
		} else {
			country = Integer.valueOf(robotCounty.trim());
		}

		int serverId = robot.getData().getServerId();
		CreateRoleRq.Builder builder = CreateRoleRq.newBuilder();
		builder.setCountry(country);
		builder.setNick("robot_" + serverId + "_" + robot.getId());
		builder.setPortrait(1);
		builder.setSex(1);

		Base.Builder base = BasePbHelper.createRqBase(CreateRoleRq.EXT_FIELD_NUMBER, eventId, CreateRoleRq.ext, builder.build());
		robot.sendPacket(PacketCreator.create(base.build()));

		LogHelper.CHANNEL_LOGGER.info("[游戏.创建角色] accountKey:{} eventId:{} cmd:{}", robot.getId(), eventId, CreateRoleRq.EXT_FIELD_NUMBER);
	}
}
