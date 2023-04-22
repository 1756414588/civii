package com.game.timer;


import com.game.cache.StaticWorldMapCache;
import com.game.define.AppTimer;
import com.game.domain.Robot;
import com.game.manager.RobotManager;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.MapInfoPb.GetMapNpcRq;
import com.game.server.datafacede.SaveRecordServer;
import com.game.spring.SpringUtil;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import com.game.util.StringUtil;
import java.util.ArrayList;
import java.util.List;

@AppTimer(desc = "地图怪物")
public class MapMonsterTimer extends TimerEvent {

	List<Packet> list = new ArrayList<>();


	public MapMonsterTimer() {
		super(-1, 10000);
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
		StaticWorldMapCache staticWorldMapCache = SpringUtil.getBean(StaticWorldMapCache.class);
		staticWorldMapCache.getWorldMaps().forEach((e, f) -> {
			// 平原+高原
			if (f.getAreaType() != 1 && f.getAreaType() != 2) {
				return;
			}
			GetMapNpcRq.Builder builder = GetMapNpcRq.newBuilder();
			builder.setMapId(f.getMapId());
			Packet packet = PacketCreator.create(BasePbHelper.createRqBase(GetMapNpcRq.EXT_FIELD_NUMBER, GetMapNpcRq.ext, builder.build()).build());
			list.add(packet);
		});
	}
}
