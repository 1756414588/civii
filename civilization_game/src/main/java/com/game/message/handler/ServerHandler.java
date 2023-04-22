package com.game.message.handler;


abstract public class ServerHandler extends Handler {
	@Override
	public DealType dealType() {
		return DealType.PUBLIC;
	}
}
