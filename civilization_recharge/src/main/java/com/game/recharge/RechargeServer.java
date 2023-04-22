package com.game.recharge;

import com.game.spring.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author jyb
 * @date 2020/5/18 11:30
 * @description
 */
public class RechargeServer {
	private static Logger logger = LoggerFactory.getLogger(RechargeServer.class);
	
	public static void main(String[] args) {
		long now = System.currentTimeMillis();
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("applicationContext.xml");
		SpringUtil.setApplicationContext(appContext);
		logger.info("RechargeServer START COMPLETE %% STARTTIME=" + (System.currentTimeMillis() - now));
	}
}
