package com.game.servlet.server;

import com.game.spring.SpringUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.game.manager.WorldManager;
import com.game.worldmap.MapInfo;
import com.game.worldmap.Monster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.game.constant.UcCodeEnum;
import com.game.domain.Player;
import com.game.manager.PlayerManager;
import com.game.manager.ServerManager;
import com.game.service.MijiService;
import com.game.uc.Message;
import com.game.uc.Server;

/**
 * GM命令相关的接口
 *
 * @author caobing
 * @date 2019/12/21 14:39
 * @description
 */

@Controller
@RequestMapping("gm")
public class GmCommServlet {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * GM命令角色
     *
     * @param nick
     * @param gmCommand
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/roleGm.do", method = RequestMethod.POST)
    public Message roleGmComm(String nick, String gmCommand, Integer channel) {
        logger.info("GmCommServlet roleGmComm : parames {} {} {}", nick, gmCommand, channel);

        try {
            if (null == nick || nick.equals("") || null == gmCommand || gmCommand.equals("")) {
                logger.error("GmCommServlet roleGmComm : parames {},desc{}", nick + "," + gmCommand, UcCodeEnum.PARAM_ERROR.getDesc());
                return new Message(UcCodeEnum.PARAM_ERROR);
            }

            Player player = SpringUtil.getBean(PlayerManager.class).getPlayer(nick);
            if (player == null) {
                logger.error("GmCommServlet roleGmComm : parames {},desc{}", nick + "," + gmCommand, UcCodeEnum.ACCOUNT_NOT_EXIST.getDesc());
                return new Message(UcCodeEnum.ACCOUNT_NOT_EXIST);
            }
            if (channel > 1 && player.account.getChannel() != channel) {
                logger.error("GmCommServlet roleGmComm : parames {},desc{}", nick + "," + gmCommand, UcCodeEnum.ACCOUNT_NOT_EXIST.getDesc());
                return new Message(UcCodeEnum.ACCOUNT_NOT_EXIST);
            }

            Server server = SpringUtil.getBean(ServerManager.class).getServer();
            int serverType = server.getServerType();
            if (serverType != 0) {
                List<Integer> whiteList = server.getWhiteList();
                if (null == whiteList || whiteList.size() == 0 || !whiteList.contains(player.account.getAccountKey())) {
                    logger.error("GmCommServlet roleGmComm : parames {},desc{}", nick + "," + gmCommand, UcCodeEnum.SYS_ERROR.getDesc());
                    return new Message(UcCodeEnum.PLAYER_IS_NOT_WHITE);
                }
            }

            SpringUtil.getBean(MijiService.class).mijiResult(gmCommand, player);
        } catch (Exception e) {
            logger.error("GmCommServlet roleGmComm : parames {},desc{}", nick + "," + gmCommand, UcCodeEnum.SYS_ERROR.getDesc());
            return new Message(UcCodeEnum.SYS_ERROR);
        }
        return new Message(UcCodeEnum.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/calcu", method = RequestMethod.POST)
    public String calcuationMonster() {
        WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
        StringBuilder builder = new StringBuilder();
        for (int mapId : worldManager.getAllMap()) {
            MapInfo mapInfo = worldManager.getMapInfo(mapId);
            Map<Integer, List<Monster>> m = mapInfo.getMonsterMap().values().stream().collect(Collectors.groupingBy(e -> e.getLevel()));
            m.entrySet().forEach(e -> {
                builder.append("[").append(mapId).append(":").append(e.getKey()).append(",").append(e.getValue().size()).append("]");
            });
        }
        return builder.toString();
    }
}
