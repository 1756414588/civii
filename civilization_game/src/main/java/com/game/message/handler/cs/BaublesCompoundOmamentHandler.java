package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.OmamentPb;
import com.game.service.OmamentService;

public class BaublesCompoundOmamentHandler extends ClientHandler {
    @Override
    public void action() {
        OmamentService service = getService(OmamentService.class);
        OmamentPb.BaublesCompoundOmamentRq req = msg.getExtension(OmamentPb.BaublesCompoundOmamentRq.ext);
        service.baublesCompoundOmamentRq(req, this);
    }
}
