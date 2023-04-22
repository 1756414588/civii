package com.game.dao.p;

import com.game.domain.p.Bootstrap;
import com.game.domain.p.Pay;
import com.game.domain.p.PayBack;
import org.apache.ibatis.annotations.Param;

public interface BootstrapDao {

	public Bootstrap selectBootstrap();

	public void update(Bootstrap bootstrap);
}
