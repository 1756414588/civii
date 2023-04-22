package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.PayPb.GetOrderNumRq;
import com.game.service.PayService;
import com.game.spring.SpringUtil;

/**
 * 2020年5月15日
 *
 * @CaoBing halo_game
 * GetOrderNumHandler.java
 **/
public class GetOrderNumHandler extends ClientHandler {

    @Override
    public void action() {
        GetOrderNumRq req = msg.getExtension(GetOrderNumRq.ext);
        PayService payService = SpringUtil.getBean(PayService.class);
        payService.createOrder(req, this);
    }

}
