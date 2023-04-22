package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.SkinPb;
import com.game.service.CommandSkinService;

/**
 * @author CaoBing
 * @date 2021/2/3 14:43
 */
public class ChangeCommandSkinHandler extends ClientHandler {
    @Override
    public void action() {
        CommandSkinService service = getService(CommandSkinService.class);
        SkinPb.ChangeCommandSkinRq req = msg.getExtension(SkinPb.ChangeCommandSkinRq.ext);
        service.changeCommandSkinRq(req, this);
    }
}
