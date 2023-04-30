package com.game.uc;

import com.game.spring.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @date 2020/4/7 10:28
 * @description
 */
public class UcServer {

	private static Logger logger = LoggerFactory.getLogger(UcServer.class);

	public static void main(String[] args) {
		long now = System.currentTimeMillis();
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("applicationContext.xml");
		SpringUtil.setApplicationContext(appContext);

		logger.info("UcServer START COMPLETE %% STARTTIME=" + (System.currentTimeMillis() - now));
		logger.info("UcServer START SUCCESS");
	}
}
