package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.message.handler.ServerHandler;
import com.game.pb.TDPb;
import com.game.service.TDService;

/**
 *
 * @date 2020/8/19 16:39
 * @description
 */
public class TDMapInitHandler extends ClientHandler {
  @Override
  public void action() {
    TDPb.TDMapInitRq req = msg.getExtension(TDPb.TDMapInitRq.ext);
    getService(TDService.class).mapInit(req, this);
  }
}
