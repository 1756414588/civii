package com.game.acion.message;

import com.game.acion.EventAction;
import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.acion.events.AddSoldierEvent;
import com.game.cache.MapMonsterCache;
import com.game.cache.StaticWorldMapCache;
import com.game.constant.GameError;
import com.game.domain.Record;
import com.game.domain.Robot;
import com.game.domain.WorldPos;
import com.game.domain.p.RobotMessage;
import com.game.domain.World;
import com.game.domain.s.StaticWorldMap;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Pos;
import com.game.pb.WorldPb.AttackRebelRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @Description 攻打野外叛军
 * @Date 2022/9/14 18:04
 **/

public class AttackRebelAction extends MessageAction {

	private AttackRebelRq attackRebelRq;

	public AttackRebelAction(RobotMessage robotMessage) {
		super(robotMessage);
		attackRebelRq = BasePbHelper.createPb(AttackRebelRq.ext, robotMessage.getContent());
	}

	final static String SELECT_PRE = "SELECT_PRE";

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		long eventId = messageEvent.getEventId();

		World world = robot.getWorld();
		int monsterLv = world.getMaxMonsterLv() + 1;
		int lordX = robot.getLord().getPosX();
		int lordY = robot.getLord().getPosY();

		// 野怪缓存数据
		MapMonsterCache mapMonsterCache = getBean(MapMonsterCache.class);
		StaticWorldMapCache staticWorldMapCache = getBean(StaticWorldMapCache.class);

		StaticWorldMap staticWorldMap = staticWorldMapCache.getStaticWorldMap(lordX, lordY);
		if (staticWorldMap.getAreaType() == 1) {// 平原
			monsterLv = monsterLv > 8 ? 8 : monsterLv;
		} else if (staticWorldMap.getAreaType() == 2) {// 高原
			monsterLv = monsterLv > 15 ? 15 : monsterLv;
		}

		List<WorldPos> entityList = mapMonsterCache.getMonsterByLv(staticWorldMap.getMapId(), monsterLv);
		if (entityList.isEmpty()) {
			return;
		}

		List<WorldPos> worldPosList = getPreSelectList(messageEvent);

		WorldPos target = null;
		int targetDistance = 0;

		for (WorldPos worldPos : entityList) {
			if (worldPos.getX() == -1 || worldPos.getY() == -1) {
				continue;
			}

			if (worldPosList.contains(worldPos)) {
				continue;
			}

			// 选择距离最短的
			int distance = distance(lordX, lordY, worldPos.getX(), worldPos.getY());
			if (target == null || distance < targetDistance) {
				targetDistance = distance;
				target = worldPos;
			}
		}

		// 未选中目标
		if (target == null) {
			tryEvent(messageEvent, 60000);//1分钟之后重新选择,等待地图刷怪
			return;
		}

		// 记录已攻打的目标
		worldPosList.add(target);

		// 攻击目标
		AttackRebelRq.Builder builder = AttackRebelRq.newBuilder();
		builder.setPos(Pos.newBuilder().setX(target.getX()).setY(target.getY()));
		builder.addAllHeroId(attackRebelRq.getHeroIdList());

		Base.Builder base = BasePbHelper.createRqBase(AttackRebelRq.EXT_FIELD_NUMBER, eventId, AttackRebelRq.ext, builder.build());
		robot.sendPacket(PacketCreator.create(base.build()));

		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{} pos:{} level:{} heroList:{}", robot.getId(), requestCode, eventId, id, getName(), target, monsterLv, attackRebelRq.getHeroIdList());
	}


	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		LogHelper.CHANNEL_LOGGER.info("[消息.返回] accountKey:{} cmd:{} eventId:{} id:{} code:{}", robot.getId(), base.getCommand(), base.getParam(), id, base.getCode());

		if (base.getCode() == GameError.WORLD_MONSTER_NOT_FOUND.getCode()) {// 坐标上的怪物消失或者其他
			tryEvent(messageEvent, 3000);
			return;
		}

		if (base.getCode() == GameError.NO_SOLDIER_COUNT.getCode()) {// 带兵量不足,补个兵
			attackRebelRq.getHeroIdList().forEach(heroId -> {

				AddSoldierEvent addSoldierEvent = new AddSoldierEvent(robot, heroId, 10);

				tryEvent(addSoldierEvent);
			});

			// 重新进攻
			tryEvent(messageEvent, 5000);
			return;
		}

		if (base.getCode() != 200) {// 攻打野怪失败,则重新选择野怪攻击
			tryEvent(messageEvent, 5000);
			return;
		}

		Record record = robot.getRecord();
		record.setState(record.getState() + 1);
	}

	private List<WorldPos> getPreSelectList(MessageEvent messageEvent) {
		List<WorldPos> list = null;
		if (!messageEvent.getParam().containsKey(SELECT_PRE)) {
			list = new ArrayList<>();
			messageEvent.getParam().put(SELECT_PRE, list);
			return list;
		}
		return (List<WorldPos>) messageEvent.getParam().get(SELECT_PRE);
	}


	public int distance(int x, int y, int endX, int endY) {
		return Math.abs(endX - x) + Math.abs(endY - y);
	}
}
