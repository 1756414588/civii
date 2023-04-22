package com;

import com.game.spring.SpringUtil;
import java.io.IOException;

import com.game.server.GameServer;
import com.game.util.LogHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
		LogHelper.GAME_LOGGER.error("begin redalert game server!!!");
		SpringUtil.setApplicationContext(ac);
		goodLuck();
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

	private static void goodLuck() {
		System.out.println("////////////////////////////////////////////////////////////////////\n" +
			"//                          _ooOoo_                               //\n" +
			"//                         o8888888o                              //\n" +
			"//                         88\" . \"88                              //\n" +
			"//                         (| ^_^ |)                              //\n" +
			"//                         O\\  =  /O                              //\n" +
			"//                      ____/`---'\\____                           //\n" +
			"//                    .'  \\\\|     |//  `.                         //\n" +
			"//                   /  \\\\|||  :  |||//  \\                        //\n" +
			"//                  /  _||||| -:- |||||-  \\                       //\n" +
			"//                  |   | \\\\\\  -  /// |   |                       //\n" +
			"//                  | \\_|  ''\\---/''  |   |                       //\n" +
			"//                  \\  .-\\__  `-`  ___/-. /                       //\n" +
			"//                ___`. .'  /--.--\\  `. . ___                     //\n" +
			"//              .\"\" '<  `.___\\_<|>_/___.'  >'\"\".                  //\n" +
			"//            | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |                 //\n" +
			"//            \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /                 //\n" +
			"//      ========`-.____`-.___\\_____/___.-`____.-'========         //\n" +
			"//                           `=---='                              //\n" +
			"////////////////////////////////////////////////////////////////////");
	}
}