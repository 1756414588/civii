package com.game.acion.login;

import com.game.cache.CacheManager;
import com.game.domain.Lord;
import com.game.domain.Record;
import com.game.domain.Robot;
import com.game.domain.World;
import com.game.manager.RecordManager;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.network.ChannelUtil;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.RoleLoginRs;
import com.game.server.AppPropertes;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

/**
 * 角色登录请求
 */
public class RoleLoginHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		RoleLoginRs msg = req.getExtension(RoleLoginRs.ext);
		RobotManager robotManager = getBean(RobotManager.class);
		Robot robot = robotManager.getRobotByKey(accountKey);

		Lord lord = new Lord();
		robot.setLord(lord);

		lord.setLordId(msg.getLordId());
		lord.setNick(msg.getNick());
		lord.setPortrait(msg.getPortrait());
		lord.setExp(msg.getExp());
		lord.setVip(msg.getVip());
		lord.setVipExp(msg.getVipDot());
		lord.setPosX(msg.getPosX());
		lord.setPosY(msg.getPosY());
		lord.setCountry(msg.getCountry());

		// 可击杀野怪等级
		World world = robot.getWorld();
		world.setMaxMonsterLv(msg.getMaxMonsterLv());

		// 新手引导步骤
		robot.setGuideKey(msg.getGuideKey());

		ChannelUtil.setRoleId(ctx, msg.getLordId());

		// 绑定记录
		RecordManager recordManager = getBean(RecordManager.class);
		Record record = recordManager.getRecord(robot.getId());
		if (record == null) {
			record = new Record();
			record.setAccountKey(robot.getId());
			record.setRecordId(1);
			record.setState(0);
			record.setCreateTime(System.currentTimeMillis());
			recordManager.insert(record);
			recordManager.getRecordMap().put(robot.getId(), record);
		}

		robot.setRecord(record);

		// 登录初始化
		AppPropertes appPropertes = getBean(AppPropertes.class);
		if (appPropertes.isRobotAuto()) {
			getBean(CacheManager.class).doLoginRequest(robot);
		}

	}
}
