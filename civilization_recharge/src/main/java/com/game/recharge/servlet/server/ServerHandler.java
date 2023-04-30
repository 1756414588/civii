package com.game.recharge.servlet.server;

import com.game.constant.UcCodeEnum;
import com.game.recharge.manager.ChannelConfigManager;
import com.game.recharge.manager.ServerManager;
import com.game.uc.Message;
import com.game.util.Md5Util;
import com.game.spring.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @date 2020/7/1 17:48
 * @description
 */
@Controller
public class ServerHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/closeGame")
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String validateCode = request.getParameter("validateCode");
        boolean success = validateCode != null
                && validateCode.equals(Md5Util.string2MD5(Md5Util.KEY));
        if (success) {
            response.getOutputStream().write("OK".getBytes());
            response.flushBuffer();

            logger.info("close game server start ....");
            logger.info("close game server end ");
            Thread.sleep(1000);
            //SpringContextLoader.getContext().destroy();
            System.exit(0);
            return;
        } else {
            logger.warn("invalidate code");
            response.getOutputStream().write("invalidate code".getBytes());
            return;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/initChannelConfig.do")
    public Message doInitChannelConfig(HttpServletRequest request, HttpServletResponse response) {
        try {
            ChannelConfigManager manager = SpringUtil.getBean(ChannelConfigManager.class);
            manager.init();
            return new Message(UcCodeEnum.SUCCESS);
        }catch (Exception e){
            return new Message(UcCodeEnum.SYS_ERROR);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/initServerConfig.do")
    public Message doInitServerConfig(HttpServletRequest request, HttpServletResponse response) {
        try {
            ServerManager manager = SpringUtil.getBean(ServerManager.class);
            manager.init();
            return new Message(UcCodeEnum.SUCCESS);
        }catch (Exception e){
            return new Message(UcCodeEnum.SYS_ERROR);
        }
    }
}
