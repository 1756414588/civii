package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.CastleService;

/**
 * @author jyb
 * @date 2019/12/13 10:37
 * @description
 */
public class GetMeetingTaskHandler extends ClientHandler {
    @Override
    public void action() {
        getService(CastleService.class).getGetMeetingTask(this);
    }
}
