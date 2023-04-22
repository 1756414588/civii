package com.game.acion.daily;

import com.game.acion.DailyAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.domain.p.DailyMessage;
import com.game.domain.p.RobotData;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.spring.SpringUtil;
import com.game.util.BasePbHelper;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

/**
 *
 * @Description自动日常
 * @Date 2022/10/21 10:53
 **/

public class AutoDaily extends DailyAction {

	public AutoDaily() {
	}

	public AutoDaily(DailyMessage dailyMessage) {
		this.id = dailyMessage.getKeyId();
		this.dailyMessage = dailyMessage;
		this.requestCode = dailyMessage.getRequestCode();
		this.respondCode = dailyMessage.getRespondCode();
	}

	@Override
	public void doAction(MessageEvent event, Robot robot) {
		Packet packet = event.createPacket();
		robot.sendPacket(packet);
	}

	@Override
	public void onResult(MessageEvent event, Robot robot, Base base) {
		RobotData robotDaily = robot.getData();
		robotDaily.setStatus(2);// 设置该操作完成
	}

	public <T> T getBean(Class<T> tClass) {
		return SpringUtil.getBean(tClass);
	}

	public <Type> Type getMsg(GeneratedExtension<Base, Type> ext) {
		Base resultData = BasePbHelper.createBase(dailyMessage.getContent());
		return resultData.getExtension(ext);
	}

}
