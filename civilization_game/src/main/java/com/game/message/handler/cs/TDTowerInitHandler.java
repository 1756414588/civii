package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TDPb;
import com.game.service.TDService;

/**
 * @author cpz
 * @date 2020/8/19 16:39
 * @description
 */
public class TDTowerInitHandler extends ClientHandler {
  @Override
  public void action() {
    TDPb.TDTowerInitRq req = msg.getExtension(TDPb.TDTowerInitRq.ext);
    getService(TDService.class).towerInit(req, this);
  }
}
