package com.game.uc.netty;

public abstract class BaseServer {

	private class CloseByExit implements Runnable {
		@Override
		public void run() {
			BaseServer.this.stop();
		}
	}

	public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread(new CloseByExit()));
	}

	protected abstract void stop();
}
