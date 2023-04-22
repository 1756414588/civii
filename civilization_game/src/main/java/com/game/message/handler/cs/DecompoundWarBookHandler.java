package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.EquipPb;
import com.game.pb.WarBookPb;
import com.game.service.WarBookService;

/**
 * @author CaoBing
 * @date 2020/12/10 16:12
 */
public class DecompoundWarBookHandler extends ClientHandler {
    @Override
    public void action() {
        WarBookService service = getService(WarBookService.class);
        WarBookPb.DecompoundWarBookRq req = msg.getExtension(WarBookPb.DecompoundWarBookRq.ext);
        service.decompoundWarBookRq(req, this);
    }
}
