package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 * @author cpz
 * @date 2020/10/11 16:46
 */
public class ActLuxuryGiftHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        ActivityPb.ActLuxuryGiftRq rq = msg.getExtension(ActivityPb.ActLuxuryGiftRq.ext);
        service.actLuxuryGiftRq(rq, this);
    }
}
