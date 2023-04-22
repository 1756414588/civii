package com.game.dao.p;

import com.game.domain.p.RobotMessage;
import java.util.List;

public interface RobotMessageDao {

	public List<RobotMessage> load();

	public void insert(RobotMessage robotCmd);

	public void update(RobotMessage robotCmd);

}
