package com;

import com.game.server.GameServer;
import com.game.spring.SpringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class ServerStart {


	public static ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

	/**
	 * Method: main
	 *
	 * @param args
	 * @return void
	 * @throws
	 * @Description: TODO
	 */
	public static void main(String[] args) {
		SpringUtil.setApplicationContext(ac);
		new Thread(GameServer.getInstance()).start();
		terminateForWindows();
	}


	public static void terminateForWindows() {
		if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
			//System.out.println("press ENTER to call System.exit() and run the shutdown routine.");
			try {
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}

}