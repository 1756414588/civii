package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.PayService;
import com.game.spring.SpringUtil;

/**
 * 充值通知
 *
 *
 */
public class GetRechargeHandler extends ClientHandler {
    @Override
    public void action() {
        SpringUtil.getBean(PayService.class).getRechargeRq(null, this);
    }
}