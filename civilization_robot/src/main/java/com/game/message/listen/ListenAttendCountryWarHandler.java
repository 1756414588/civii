package com.game.message.listen;

import com.game.domain.Robot;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.AttendCountryWarRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 监听参加国战
 */
public class ListenAttendCountryWarHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {

		RobotManager robotManager = getBean(RobotManager.class);
		List<Robot> robotList = robotManager.getRobotList();

		AttendCountryWarRq msg = req.getExtension(AttendCountryWarRq.ext);

		robotList.forEach(e -> {

			Base base = BasePbHelper.createRqBase(AttendCountryWarRq.EXT_FIELD_NUMBER, AttendCountryWarRq.ext, msg).build();
			e.sendPacket(PacketCreator.create(base));

//			LogHelper.CHANNEL_LOGGER.info("AttendCountryWarRq roldId:{} pos:{}", e.getLord().getLordId(), e.getPos());
		});
	}

}
