package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--查看分享的钓鱼记录
 */
public class LookFishRecordHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.LookFishRecordRq req = msg.getExtension(FishingPb.LookFishRecordRq.ext);
        service.lookFishRecordRq(req, this);
    }
}
