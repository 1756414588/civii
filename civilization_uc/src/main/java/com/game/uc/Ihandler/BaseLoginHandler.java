package com.game.uc.Ihandler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

public abstract class BaseLoginHandler implements LoginHandler {

	public static final String prefix = "http://";

	@PostConstruct
	public abstract void register();

	public static final Map<Integer, LoginHandler> map = new HashMap<>();

	public void addHandler(int name, LoginHandler hanlder) {
		map.put(name, hanlder);
	}


}
