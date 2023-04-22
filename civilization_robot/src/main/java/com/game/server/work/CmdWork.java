package com.game.server.work;

import com.game.dao.p.RobotMessageDao;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.InnerPb.ListenEventRq;
import com.game.pb.InnerPb.ListenEventRs;
import com.game.server.ITask;
import com.game.spring.SpringUtil;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

public class CmdWork implements ITask {

	private Packet packet;

	public CmdWork(Packet packet) {
		this.packet = packet;
	}

	@Override
	public void run() {
		try {
			Base base = BasePbHelper.createBase(packet.getBytes());
			if (base.getCommand() == ListenEventRq.EXT_FIELD_NUMBER || base.getCommand() == ListenEventRs.EXT_FIELD_NUMBER) {
				return;
			}
			RobotMessage robotMessage = new RobotMessage();
			robotMessage.setRequestCode(base.getCommand());
			robotMessage.setContent(packet.getBytes());
			robotMessage.setCreateTime(System.currentTimeMillis());

			// 存储r
			RobotMessageDao robotMessageDao = SpringUtil.getBean(RobotMessageDao.class);
			robotMessageDao.insert(robotMessage);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}
}
