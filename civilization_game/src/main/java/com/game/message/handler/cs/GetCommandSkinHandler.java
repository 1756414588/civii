package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.SkinPb;
import com.game.service.CommandSkinService;

/**
 * @author CaoBing
 * @date 2021/2/2 15:27
 */
public class GetCommandSkinHandler extends ClientHandler {
    @Override
    public void action() {
        CommandSkinService service = getService(CommandSkinService.class);
        SkinPb.GetCommandSkinRq req = msg.getExtension(SkinPb.GetCommandSkinRq.ext);
        service.GetCommandSkinRq(req, this);
    }
}
