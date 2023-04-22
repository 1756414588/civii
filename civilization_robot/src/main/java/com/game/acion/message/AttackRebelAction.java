package com.game.acion.message;

import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.acion.events.AddSoldierEvent;
import com.game.cache.StaticWorldMapCache;
import com.game.cache.UserMapCache;
import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.domain.WorldPos;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.domain.s.StaticWorldMap;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Pos;
import com.game.pb.WorldPb.AttackRebelRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author 陈奎
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

		Map<Integer, WorldPos> worldPosMap = getSelectPre(messageEvent);
		WorldPos target = getNearestPos(robot, worldPosMap);

		// 未选中目标
		if (target == null) {
			if (robot.getCache().getMapCache().getMaxLevel() >= 7) {//新手引导打怪已经做完了,放弃打怪
				RobotData robotData = robot.getData();
				robotData.setGuildState(robotData.getGuildState() + 1);
				return;
			}

			tryEvent(messageEvent, 60000);//1分钟之后重新选择,等待地图刷怪
			return;
		}

		// 攻击目标
		AttackRebelRq.Builder builder = AttackRebelRq.newBuilder();
		builder.setPos(Pos.newBuilder().setX(target.getX()).setY(target.getY()));
		builder.addAllHeroId(attackRebelRq.getHeroIdList());

		Base.Builder base = BasePbHelper.createRqBase(AttackRebelRq.EXT_FIELD_NUMBER, messageEvent.getEventId(), AttackRebelRq.ext, builder.build());
		robot.sendPacket(PacketCreator.create(base.build()));

		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{} pos:{} level:{} heroList:{}", robot.getId(), requestCode, messageEvent.getEventId(), id, getName(), target, attackRebelRq.getLevel(), attackRebelRq.getHeroIdList());
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

		RobotData robotData = robot.getData();
		robotData.setGuildState(robotData.getGuildState() + 1);
	}

	private WorldPos getNearestPos(Robot robot, Map<Integer, WorldPos> worldPosMap) {

		int lordX = robot.getLord().getPosX();
		int lordY = robot.getLord().getPosY();

		StaticWorldMapCache staticWorldMapCache = getBean(StaticWorldMapCache.class);
		StaticWorldMap staticWorldMap = staticWorldMapCache.getStaticWorldMap(lordX, lordY);
		int lordMapId = staticWorldMap.getMapId();

		UserMapCache mapCache = robot.getCache().getMapCache();
		List<WorldPos> entityList = mapCache.getPosList(lordMapId, attackRebelRq.getLevel());
		if (entityList.isEmpty()) {
			return null;
		}

		WorldPos target = null;
		int targetDistance = 0;

		for (WorldPos worldPos : entityList) {
			if (worldPos.getX() == -1 || worldPos.getY() == -1) {
				continue;
			}

			// 已选择过
			int posValue = worldPos.getPosValue();
			if (worldPosMap.containsKey(posValue)) {
				continue;
			}

			// 选择距离最短的
			int distance = distance(lordX, lordY, worldPos.getX(), worldPos.getY());
			if (target == null || distance < targetDistance) {
				targetDistance = distance;
				target = worldPos;
			}
		}

		if (target != null) {
			target.setAttack(true);
			worldPosMap.put(target.getPosValue(), target);
		}

		return target;
	}

	private Map<Integer, WorldPos> getSelectPre(MessageEvent messageEvent) {
		Map<Integer, WorldPos> map = null;
		if (!messageEvent.getParam().containsKey(SELECT_PRE)) {
			map = new HashMap<>();
			messageEvent.getParam().put(SELECT_PRE, map);
			return map;
		}
		return (Map<Integer, WorldPos>) messageEvent.getParam().get(SELECT_PRE);
	}


	public int distance(int x, int y, int endX, int endY) {
		return Math.abs(endX - x) + Math.abs(endY - y);
	}
}
