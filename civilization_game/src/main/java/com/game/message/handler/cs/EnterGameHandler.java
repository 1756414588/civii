package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.AccountService;

public class EnterGameHandler extends ClientHandler {
    @Override
    public void action() {
        AccountService accountService = getService(AccountService.class);
        accountService.enterGame(this);
    }
}
