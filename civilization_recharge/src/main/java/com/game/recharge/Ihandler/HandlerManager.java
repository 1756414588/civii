//package com.game.recharge.Ihandler;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import com.game.spring.BeanFactory;
//
//public class HandlerManager {
//
//	private static HandlerManager inst = new HandlerManager();
//
//	public static HandlerManager getInst() {
//		return inst;
//	}
//
//	private Map<String, PayHandler> map = new HashMap<>();
//
//	public void addHandler(String name, PayHandler hanlder) {
//		map.put(name, hanlder);
//	}
//
//    public void listen() {
////        Map<String, PayHandler> beansOfType = BeanFactory.getContext().getBeansOfType(PayHandler.class);
////        beansOfType.values().forEach(x -> x.register());
//    }
//
//    public PayHandler getPayHandler(String handName) {
//        return map.get(handName.trim());
//    }
//}
