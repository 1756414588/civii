package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

/**
 * @author cpz
 * @date 2020/9/4 9:31
 * @description
 */
public class DeliveryHandler extends ClientHandler {
  @Override
  public void action() {
    WorldPb.DeliveryRq rq = msg.getExtension(WorldPb.DeliveryRq.ext);
    getService(WorldService.class).deliveryMove(rq, this);
  }
}
