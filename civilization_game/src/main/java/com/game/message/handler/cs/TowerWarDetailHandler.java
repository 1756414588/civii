package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.message.handler.ServerHandler;
import com.game.pb.TDPb;
import com.game.service.TDService;

/**
 * @author cpz
 * @date 2020/8/19 16:39
 * @description
 */
public class TowerWarDetailHandler extends ClientHandler {
  @Override
  public void action() {
    TDPb.TowerWarRq req = msg.getExtension(TDPb.TowerWarRq.ext);
    getService(TDService.class).towerDetail(req,this);
  }
}
