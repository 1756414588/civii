//package com.game.uc.Ihandler;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import com.game.spring.BeanFactory;
//import com.game.uc.Ihandler.LoginHandler;
//
//public class HandlerManager {
//
//	private static HandlerManager inst = new HandlerManager();
//
//	public static HandlerManager getInst() {
//		return inst;
//	}
//
//	private Map<Integer, LoginHandler> map = new HashMap<>();
//
//	public void addHandler(int name, LoginHandler hanlder) {
//		map.put(name, hanlder);
//	}
//
//	public void listen() {
//		Map<String, LoginHandler> beansOfType = BeanFactory.getContext().getBeansOfType(LoginHandler.class);
//		beansOfType.values().forEach(x -> x.register());
//	}
//
//	public LoginHandler getPayHandler(int handName) {
//		return map.get(handName);
//	}
//}
