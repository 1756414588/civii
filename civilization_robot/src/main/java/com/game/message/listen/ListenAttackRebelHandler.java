package com.game.message.listen;

import com.game.domain.Entity;
import com.game.domain.Robot;
import com.game.domain.World;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Pos;
import com.game.pb.WorldPb.AttackRebelRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import com.google.common.collect.HashBasedTable;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 监听到玩家获取坐标 则所有的玩家攻打自身周围的叛军
 */
public class ListenAttackRebelHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {

		RobotManager robotManager = getBean(RobotManager.class);
		List<Robot> robotList = robotManager.getRobotList();

		AttackRebelRq msg = req.getExtension(AttackRebelRq.ext);
		Pos targetPos = msg.getPos();

		HashBasedTable<Integer, Integer, Integer> selected = HashBasedTable.create();
		selected.put(targetPos.getX(), targetPos.getY(), 1);

		robotList.forEach(e -> {
			AttackRebelRq.Builder builder = AttackRebelRq.newBuilder();

			World world = e.getWorld();
			int monsterLv = world.getMaxMonsterLv() + 1;
			int mlv = monsterLv > world.getMonsterLv() ? world.getMonsterLv() : monsterLv;
//			LogHelper.CHANNEL_LOGGER.info("攻打叛军 playerId:{} 可打等级:{},叛军等级:{} 最终等级:{}", e.getId(), monsterLv, world.getMonsterLv(), mlv);
			List<Entity> entityList = world.getEntityList(f -> f.getEntityType() == 1 && f.getLevel() == mlv);
			if (entityList.isEmpty()) {
				return;
			}

			int lordX = e.getLord().getPosX();
			int lordY = e.getLord().getPosY();

			int targetX = 0;
			int targetY = 0;
			int targetDistance = -1;

			for (Entity entity : entityList) {
				int x = entity.getPos().getX();
				int y = entity.getPos().getY();

				if (x == -1 || y == -1) {
					continue;
				}

				// 已经被选
				if (selected.contains(x, y)) {
					continue;
				}

				int distance = distance(lordX, lordY, x, y);
				if (targetDistance == -1 || distance < targetDistance) {
					targetDistance = distance;
					targetX = x;
					targetY = y;
				}
			}

			if (targetX == 0) {
				return;
			}

			selected.put(targetX, targetY, 1);

			// 攻打的坐标
			Pos.Builder pos = Pos.newBuilder();
			pos.setX(targetX);
			pos.setY(targetY);

			builder.setPos(pos);

			// 参战的英雄
			int heroId = e.getCache().getHeroCache().getEmptyEmbattle();
			builder.addHeroId(heroId);

			Base base = BasePbHelper.createRqBase(AttackRebelRq.EXT_FIELD_NUMBER, AttackRebelRq.ext, builder.build()).build();
			e.sendPacket(PacketCreator.create(base));

//			LogHelper.CHANNEL_LOGGER.info("AttackRebelRq :{}", builder.build());
		});
	}

	public int distance(int x, int y, int endX, int endY) {
		return Math.abs(endX - x) + Math.abs(endY - y);
	}
}
