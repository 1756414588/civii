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
public class TowerWarReportHandler extends ClientHandler {
  @Override
  public void action() {
    TDPb.TowerWarReportRq req = msg.getExtension(TDPb.TowerWarReportRq.ext);
    getService(TDService.class).towerWarReport(req, this);
  }
}
