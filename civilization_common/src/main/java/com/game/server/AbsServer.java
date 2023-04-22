package com.game.server;

import com.game.network.INet;
import com.game.register.PBFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbsServer implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("game");

	protected String name;

	protected INet net;

	protected boolean ready;

	protected AbsServer(String name) {
		this.name = name;
	}

	public abstract String getGameType();

	protected abstract void stop();

	public void setNet(INet net) {
		this.net = net;
	}

	public INet getNet() {
		return this.net;
	}

	private class CloseByExit implements Runnable {

		private String serverName;

		public CloseByExit(String serverName) {
			this.serverName = serverName;
		}

		@Override
		public void run() {
			AbsServer.this.stop();
			logger.info(serverName + " closed");
		}
	}

	public void registerPbFile() {
		PBFile.getInst().register();
	}

	public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread(new CloseByExit(name)));
	}
}
