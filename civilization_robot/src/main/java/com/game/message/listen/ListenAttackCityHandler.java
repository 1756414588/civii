package com.game.message.listen;

import com.game.domain.Entity;
import com.game.domain.Robot;
import com.game.domain.World;
import com.game.domain.WorldPos;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Pos;
import com.game.pb.WorldPb.AttackCityRq;
import com.game.util.BasePbHelper;
import com.game.util.MapUtil;
import com.google.common.collect.HashBasedTable;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 监听到玩家攻打城池
 */
public class ListenAttackCityHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {

		AttackCityRq msg = req.getExtension(AttackCityRq.ext);

		RobotManager robotManager = getBean(RobotManager.class);
		List<Robot> robotList = robotManager.getRobotList();

		// 远征战斗,直接全部去打
		if (msg.getType() == 3) {
			robotList.forEach(e -> {
				e.sendPacket(PacketCreator.create(req));
			});
			return;
		}

		HashBasedTable<Integer, Integer, Integer> selected = HashBasedTable.create();
		selected.put(msg.getPos().getX(), msg.getPos().getY(), 1);

		robotList.forEach(e -> {

			AttackCityRq.Builder builder = AttackCityRq.newBuilder();

			World world = e.getWorld();
			List<Entity> entityList = world.getEntityList(f -> f.getEntityType() == 3 && f.getCountry() != e.getLord().getCountry());

			int x = e.getLord().getPosX();
			int y = e.getLord().getPosY();

			if (entityList == null || entityList.isEmpty()) {
				return;
			}

			int distance = -1;
			int targetX = 0;
			int targetY = 0;
			for (Entity entity : entityList) {
				WorldPos pos = entity.getPos();

				if (selected.contains(pos.getX(), pos.getY())) {
					continue;
				}
				int t = MapUtil.distance(x, y, targetX, targetY);
				if (distance == -1 || distance > t) {
					distance = t;
					targetX = pos.getX();
					targetY = pos.getY();
				}
			}

			if (targetX == 0) {
				return;
			}

			// 攻打的坐标
			Pos.Builder pos = Pos.newBuilder();
			pos.setX(targetX);
			pos.setY(targetY);

			builder.setPos(pos);

			// 参战的英雄
			int heroId = e.getCache().getHeroCache().getEmptyEmbattle();
			builder.addHeroId(heroId);

			// 攻打类型
			builder.setType(msg.getType());

			Base base = BasePbHelper.createRqBase(AttackCityRq.EXT_FIELD_NUMBER, AttackCityRq.ext, builder.build()).build();
			e.sendPacket(PacketCreator.create(base));
		});
	}

	private void yuanzhen(List<Robot> robotList, Base req) {

	}
}
