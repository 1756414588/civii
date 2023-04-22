package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TDPb.UseEndlessTDProRq;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/10 9:50
 **/
public class UseEndlessTDProHandler extends ClientHandler {

    @Override
    public void action() {
        getService(TDService.class).useEndlessTDProRq(this,msg.getExtension(UseEndlessTDProRq.ext));
    }
}
