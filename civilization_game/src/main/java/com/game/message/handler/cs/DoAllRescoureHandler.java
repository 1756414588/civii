package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.BuildingService;

/**
*2020年7月6日
*
*halo_game
*DoAllRescoureHandler.java
**/
public class DoAllRescoureHandler extends ClientHandler{
	 @Override
	    public void action() {
	        BuildingService service = getService(BuildingService.class);
	        service.doAllRescoureRq(this);
	    }
}
