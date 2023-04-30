package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CastlePb;
import com.game.service.CastleService;

/**
 *
 * @date 2019/12/13 13:59
 * @description
 */
public class OpenMeetingTaskHandler extends ClientHandler {
    @Override
    public void action() {
        CastlePb.OpenMeetingTaskRq rq = msg.getExtension(CastlePb.OpenMeetingTaskRq.ext);
        getService(CastleService.class).openMeetingTask(this,rq);
    }
}
