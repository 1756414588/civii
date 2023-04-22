package com.game.dao.p;

import com.game.domain.p.LoginMessage;
import com.game.domain.p.RobotMessage;
import java.util.List;

public interface LoginMessageDao {

	public List<LoginMessage> load();

	public void insert(LoginMessage loginMessage);

}
