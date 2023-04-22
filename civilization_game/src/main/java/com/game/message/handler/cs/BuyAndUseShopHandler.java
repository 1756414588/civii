package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ShopPb;
import com.game.service.ShopService;


public class BuyAndUseShopHandler  extends ClientHandler {
    @Override
    public void action() {
        ShopService service = getService(ShopService.class);
        ShopPb.BuyAndUseShopRq req = msg.getExtension(ShopPb.BuyAndUseShopRq.ext);
        service.buyAndUseShopRq(req, this);
    }
}