package com.game;

import com.game.server.GateServer;

/**
 *
 * @Description 网关启动类
 * @Date 2022/9/9 11:30
 **/

public class StartGate {

	public static void main(String[] msg) {
		GateServer.getInst().start();
	}

}
