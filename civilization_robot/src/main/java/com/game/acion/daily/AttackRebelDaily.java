package com.game.acion.daily;

import com.game.acion.MessageEvent;
import com.game.cache.StaticWorldMapCache;
import com.game.cache.UserHeroCache;
import com.game.cache.UserMapCache;
import com.game.domain.Robot;
import com.game.domain.WorldPos;
import com.game.domain.p.DailyMessage;
import com.game.domain.s.StaticWorldMap;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Pos;
import com.game.pb.WorldPb.AttackRebelRq;
import com.game.util.BasePbHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author 陈奎
 * @Description攻打野怪
 * @Date 2022/10/21 10:53
 **/

public class AttackRebelDaily extends AutoDaily {


	public AttackRebelDaily(DailyMessage dailyMessage) {
		super(dailyMessage);
	}

	@Override
	public void doAction(MessageEvent event, Robot robot) {
		int x = robot.getLord().getPosX();
		int y = robot.getLord().getPosY();

		WorldPos target = getNearestPos(robot, x, y);

		AttackRebelRq.Builder builder = AttackRebelRq.newBuilder();
		builder.setPos(Pos.newBuilder().setX(target.getX()).setY(target.getY()));

		// 玩家身上武将信息
		List<Integer> heroList = new ArrayList<>();
		UserHeroCache userHeroCache = robot.getCache().getHeroCache();
		heroList.addAll(userHeroCache.getEmbattles());
		builder.addAllHeroId(heroList);

		Base.Builder base = BasePbHelper.createRqBase(AttackRebelRq.EXT_FIELD_NUMBER, event.getEventId(), AttackRebelRq.ext, builder.build());
		robot.sendPacket(PacketCreator.create(base.build()));
	}

	private WorldPos getNearestPos(Robot robot, int x, int y) {

		StaticWorldMapCache staticWorldMapCache = getBean(StaticWorldMapCache.class);
		StaticWorldMap staticWorldMap = staticWorldMapCache.getStaticWorldMap(x, y);
		int lordMapId = staticWorldMap.getMapId();

		int rebelLv = 6;
		if (staticWorldMap.getAreaType() == 2) {
			rebelLv = 8;
		}

		UserMapCache mapCache = robot.getCache().getMapCache();
		List<WorldPos> entityList = mapCache.getPosList(lordMapId, rebelLv);
		if (entityList.isEmpty()) {
			return null;
		}

		WorldPos target = null;
		int targetDistance = 0;

		for (WorldPos worldPos : entityList) {
			if (worldPos.getX() == -1 || worldPos.getY() == -1) {
				continue;
			}

			// 选择距离最短的
			int distance = distance(x, y, worldPos.getX(), worldPos.getY());
			if (target == null || distance < targetDistance) {
				targetDistance = distance;
				target = worldPos;
			}
		}

		if (target != null) {
			target.setAttack(true);
		}

		return target;
	}

	public int distance(int x, int y, int endX, int endY) {
		return Math.abs(endX - x) + Math.abs(endY - y);
	}

}
