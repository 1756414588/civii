package com.game.servlet.server;

import com.game.constant.UcCodeEnum;
import com.game.domain.Robot;
import com.game.manager.LoginManager;
import com.game.manager.RobotManager;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.ChatPb;
import com.game.spring.SpringUtil;
import com.game.uc.Message;
import com.game.util.BasePbHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class ServerHandler {
    /**
     * 特殊接口， 仅用作前端测试跳过sdk检测
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/robot/logOut.do", method = RequestMethod.POST)
    public Message logOut(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String accountKey = request.getParameter("accountKey");
        int type = Integer.parseInt(request.getParameter("type")); //1.上线 2.下线
        RobotManager bean = SpringUtil.getBean(RobotManager.class);
        if (accountKey != null && !accountKey.equals("")) {
            String[] split = accountKey.split(",");
            for (String s : split) {
                Robot robotByKey = bean.getRobotByKey(Integer.parseInt(s));
                if (robotByKey != null) {
                    if (type == 1) {
                        robotByKey.getData().setFlag(true);
                    } else {
                        LoginManager loginManager = SpringUtil.getBean(LoginManager.class);
                        loginManager.logout(robotByKey);
                        robotByKey.getData().setFlag(false);
                    }
                }
            }
        }
        return new Message(UcCodeEnum.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/robot/chat.do", method = RequestMethod.POST)
    public Message doChat(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String accountKey = request.getParameter("accountKey");
        RobotManager bean = SpringUtil.getBean(RobotManager.class);
        Robot robotByKey = bean.getRobotByKey(Integer.parseInt(accountKey));
        if (robotByKey != null) {
            int style = Integer.valueOf(request.getParameter("style"));
            String msg = request.getParameter("message");
            boolean region = Boolean.parseBoolean(request.getParameter("region"));
            ChatPb.DoChatRq.Builder builder = ChatPb.DoChatRq.newBuilder();
            builder.setMsg(msg);
            builder.setStyle(style);
            builder.setRegion(region);
            Packet packet = PacketCreator.create(BasePbHelper.createRqBase(ChatPb.DoChatRq.EXT_FIELD_NUMBER, robotByKey.getMsgSeq(), ChatPb.DoChatRq.ext, builder.build()).build());
            robotByKey.sendPacket(packet);
        }
        return new Message(UcCodeEnum.SUCCESS);
    }
}
