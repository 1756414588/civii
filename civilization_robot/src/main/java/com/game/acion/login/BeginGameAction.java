package com.game.acion.login;

import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.RolePb.UserLoginRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description 开始游戏请求
 * @Date 2022/9/19 10:50
 **/

@Component
public class BeginGameAction extends EnterGameAction {

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		long eventId = messageEvent.getEventId();
		UserLoginRq.Builder userLoginRq = UserLoginRq.newBuilder();
		userLoginRq.setServerId(robot.getData().getServerId());
		userLoginRq.setKeyId(robot.getData().getAccountKey());
		userLoginRq.setToken(robot.getData().getToken());
		userLoginRq.setDeviceNo(robot.getData().getAccount());
		userLoginRq.setClientVer("2.0.001");
		userLoginRq.setChannel(1);
		userLoginRq.setMinCt(1);

		// 登录游戏
		Packet packet = PacketCreator.create(BasePbHelper.createRqBase(UserLoginRq.EXT_FIELD_NUMBER, eventId, UserLoginRq.ext, userLoginRq.build()).build());
		robot.sendPacket(packet);

		LogHelper.CHANNEL_LOGGER.info("[游戏.开始] accountKey:{} eventId:{} cmd:{}", robot.getId(), eventId, packet.getCmd());
	}
}
