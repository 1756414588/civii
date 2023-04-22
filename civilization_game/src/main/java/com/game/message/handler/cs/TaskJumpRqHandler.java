package com.game.message.handler.cs;

import com.game.message.handler.ServerHandler;
import com.game.pb.GmToolPb;
import com.game.service.GmToolService;
import com.game.spring.SpringUtil;

/**
 * @filename
 * @author lyz
 * @version 1.0
 * @time 2017-3-29 下午15:02
 * @describe
 */
public class TaskJumpRqHandler extends ServerHandler {

    @Override
    public void action() {
        GmToolPb.TaskJumpRq req = msg.getExtension(GmToolPb.TaskJumpRq.ext);
        GmToolService toolService = SpringUtil.getBean(GmToolService.class);
        toolService.jumpTask(req, this);
    }

}