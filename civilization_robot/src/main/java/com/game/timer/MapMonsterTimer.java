package com.game.timer;


import com.game.define.AppTimer;
import com.game.domain.Robot;
import com.game.manager.RobotManager;
import com.game.packet.Packet;
import com.game.spring.SpringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@AppTimer(desc = "地图怪物")
public class MapMonsterTimer extends TimerEvent {

	List<Packet> list = new ArrayList<>();

	AtomicLong incr = new AtomicLong(1);

	// 10分钟同步一次地图怪物
	public MapMonsterTimer() {
		super(-1, 600000);
	}


	@Override
	public void action() {
		RobotManager robotManager = SpringUtil.getBean(RobotManager.class);
		Robot robot = robotManager.getRobotMap().values().stream().filter(e -> e.isLogin()).findFirst().orElse(null);
		if (robot == null) {
			return;
		}

		if (list.isEmpty()) {
			init();
		}

		for (Packet packet : list) {
			robot.sendPacket(packet);
		}
	}

	private void init() {
//		StaticWorldMapCache staticWorldMapCache = SpringUtil.getBean(StaticWorldMapCache.class);
//		staticWorldMapCache.getWorldMaps().forEach((e, f) -> {
//			// 平原+高原
//			if (f.getAreaType() != 1 && f.getAreaType() != 2) {
//				return;
//			}
//			GetMapNpcRq.Builder builder = GetMapNpcRq.newBuilder();
//			builder.setMapId(f.getMapId());
//			Packet packet = PacketCreator.create(BasePbHelper.createRqBase(GetMapNpcRq.EXT_FIELD_NUMBER, incr.getAndIncrement(), GetMapNpcRq.ext, builder.build()).build());
//			list.add(packet);
//		});
	}
}
