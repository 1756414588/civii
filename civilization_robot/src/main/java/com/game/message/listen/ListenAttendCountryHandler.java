package com.game.message.listen;

import com.game.domain.Entity;
import com.game.domain.Robot;
import com.game.domain.World;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.AttendCountryWarRq;
import com.game.util.BasePbHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 监听玩家参加国战
 */
public class ListenAttendCountryHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {

		AttendCountryWarRq msg = req.getExtension(AttendCountryWarRq.ext);

		RobotManager robotManager = getBean(RobotManager.class);
		List<Robot> robotList = robotManager.getRobotList();

		robotList.forEach(e -> {

			AttendCountryWarRq.Builder builder = AttendCountryWarRq.newBuilder();

			World world = e.getWorld();
			Entity entity = world.getEntity(f -> f.getEntityType() == 4);
			if (entity == null) {
				return;
			}

			builder.setWarId(msg.getWarId());
			builder.setMapId(msg.getMapId());

			// 参战的英雄
			int heroId = e.getCache().getHeroCache().getEmptyEmbattle();
			builder.addHeroId(heroId);

			Base base = BasePbHelper.createRqBase(AttendCountryWarRq.EXT_FIELD_NUMBER, AttendCountryWarRq.ext, builder.build()).build();
			e.sendPacket(PacketCreator.create(base));
		});
	}
}
