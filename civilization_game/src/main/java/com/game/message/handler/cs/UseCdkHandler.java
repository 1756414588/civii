package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.AccountService;

/**
 * @author jyb
 * @date 2020/6/2 14:05
 * @description
 */
public class UseCdkHandler extends ClientHandler {
    @Override
    public void action() {
        AccountService service = getService(AccountService.class);
        RolePb.UseCdkRq req = msg.getExtension(RolePb.UseCdkRq.ext);
        service.useCdk(this, req);
    }
}
