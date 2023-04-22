package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--查看分享的钓鱼记录
 */
public class GetFishRecordHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.GetFishRecordRq req = msg.getExtension(FishingPb.GetFishRecordRq.ext);
        service.getFishRecordRq(req, this);
    }
}
