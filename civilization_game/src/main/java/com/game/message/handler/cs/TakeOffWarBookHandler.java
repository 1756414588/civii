package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.OmamentPb;
import com.game.pb.WarBookPb;
import com.game.service.OmamentService;
import com.game.service.WarBookService;

/**
 * @author CaoBing
 * @date 2020/12/14 16:37
 */
public class TakeOffWarBookHandler extends ClientHandler {
    @Override
    public void action() {
        WarBookService service = getService(WarBookService.class);
        WarBookPb.TakeOffWarBookRq req = msg.getExtension(WarBookPb.TakeOffWarBookRq.ext);
        service.takeOffWarBookRq(req, this);
    }
}
