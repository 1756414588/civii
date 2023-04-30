package com.game.servlet.log;

import com.game.spring.SpringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.game.constant.UcCodeEnum;
import com.game.domain.Player;
import com.game.log.p.PlayerLog;
import com.game.manager.PlayerManager;
import com.game.uc.Message;
import com.game.util.PageUtils;

/**
 *
 * @date 2020/1/15 13:52
 * @description
 */
@Controller
public class QueryPlayerHandler {

  private Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * 查询服务器的玩家
   *
   * @param lordId 玩家id
   * @return
   */
  @ResponseBody
  @RequestMapping("log/queryPlayer")
  public Message queryPlayer(String lordId) {
    PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
    Long playerId = 0L;
    try {
      playerId = Long.parseLong(lordId);
    } catch (Exception e) {
    }
    // 先根据名称
    Player player = playerManager.getPlayer(lordId);
    if (player == null) {
      // 再根据id
      player = playerManager.getPlayer(playerId);
    }
    if (player == null) {
      logger.error("QueryPlayerHandler queryPlayer error player {} is not exist ", lordId);
      return new Message(UcCodeEnum.PLAYER_IS_NOT_EXIST);
    }
    logger.info("query player info:{}", JSON.toJSONString(new PlayerLog(player)));
    return new Message(JSON.toJSONString(new PlayerLog(player)));
  }

  /**
   * 分页查询服务器的玩家
   *
   * @param currPageNo 页码
   * @param pageSize 每页显示条数
   * @return
   */
  @ResponseBody
  @RequestMapping(value = "log/queryPlayerPage.do", method = RequestMethod.POST)
  public Message queryPlayerPage(Integer currPageNo, Integer pageSize) {
    Map<String, Object> pagingResultMap = new HashMap<>();

    try {
      logger.info(
          "QueryPlayerHandler queryPlayerPage : parames {}",
          "currPageNo=" + currPageNo + "," + "pageSize=" + pageSize);

      if (null == currPageNo
          || pageSize == null
          || currPageNo.intValue() == 0
          || pageSize.intValue() == 0) {
        logger.error(
            "QueryPlayerHandler queryPlayerPage : parames {},desc{}",
            "currPageNo=" + currPageNo + "," + "pageSize=" + pageSize,
            UcCodeEnum.PARAM_ERROR.getDesc());
        return new Message(UcCodeEnum.PARAM_ERROR);
      }

      PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
      Map<Long, Player> allPlayer = playerManager.getAllPlayer();

      List<PlayerLog> allPlayers = new ArrayList<>();

      Set<Entry<Long, Player>> entrySet = allPlayer.entrySet();
      for (Entry<Long, Player> entry : entrySet) {
        allPlayers.add(new PlayerLog(entry.getValue()));
      }

      pagingResultMap = PageUtils.getPagingResultMap(allPlayers, currPageNo, pageSize);
    } catch (Exception e) {
      logger.error(
          "GmCommServlet roleGmComm : parames {},desc{} currPageNo= {} ,pageSize={}",
          UcCodeEnum.SYS_ERROR.getDesc(),
          currPageNo,
          pageSize);
      return new Message(UcCodeEnum.SYS_ERROR);
    }
    return new Message(UcCodeEnum.SUCCESS, JSON.toJSONString(pagingResultMap));
  }
}
