package com.game;

import com.game.server.RobotServer;
import com.game.spring.SpringUtil;
import java.io.IOException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StartRobotApp {

	public static ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

	public static void main(String[] msg) {
		SpringUtil.setApplicationContext(ac);
		new Thread(RobotServer.getInst()).start();
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
