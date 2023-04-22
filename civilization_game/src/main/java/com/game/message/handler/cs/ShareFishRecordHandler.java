package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--分享钓鱼记录
 */
public class ShareFishRecordHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.ShareFishRecordRq req = msg.getExtension(FishingPb.ShareFishRecordRq.ext);
        service.shareFishRecordRq(req, this);
    }
}
