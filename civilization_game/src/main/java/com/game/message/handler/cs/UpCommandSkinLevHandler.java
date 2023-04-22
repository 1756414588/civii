package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb;
import com.game.pb.SkinPb;
import com.game.service.BeautyService;
import com.game.service.CommandSkinService;

/**
 * @author CaoBing
 * @date 2021/3/18 15:12
 */
public class UpCommandSkinLevHandler extends ClientHandler {
    @Override
    public void action() {
        CommandSkinService service = getService(CommandSkinService.class);
        SkinPb.UpCommandSkinLevRq req = msg.getExtension(SkinPb.UpCommandSkinLevRq.ext);
        service.upCommandSkinLevRq(req, this);
    }
}
