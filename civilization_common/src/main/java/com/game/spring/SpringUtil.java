package com.game.spring;

import org.springframework.context.ApplicationContext;

/**
 *
 * @Description
 * @Date 2022/9/9 11:30
 **/

public class SpringUtil {

	private static ApplicationContext applicationContext;

	public static void setApplicationContext(ApplicationContext ac) {
		applicationContext = ac;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static <T> T getBean(Class<T> requiredType) {
		return applicationContext.getBean(requiredType);
	}

	public static <T> T getBean(String name, Class<T> requiredType) {
		return applicationContext.getBean(name, requiredType);
	}


}
