package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TDPb;
import com.game.service.TDService;

/**
 * @author cpz
 * @date 2020/8/19 16:39
 * @description
 */
public class TDBoundsInitHandler extends ClientHandler {
  @Override
  public void action() {
    getService(TDService.class).bounsInit(this);
  }
}
