package com.game.servlet.server;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.game.constant.UcCodeEnum;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.Player;
import com.game.manager.PlayerManager;
import com.game.server.GameServer;
import com.game.uc.Message;

/**
*2020年7月9日
*
*halo_game
*NoticeServlet.java
**/
@RequestMapping("notice")
@Controller
public class NoticeServlet {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 活动计划
	 * @param
	 * @param
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getConetnt" , method = RequestMethod.POST) 
	public Message actPlan() {
		logger.info("getConetnt");
		
		String content = "";
		try {
			content="测试公告";
		} catch (Exception e) {
			logger.error("NoticeServlet getConetnt : desc{}", UcCodeEnum.SYS_ERROR.getDesc());
			e.printStackTrace();
            return new Message(UcCodeEnum.SYS_ERROR);
		}
		return new Message(UcCodeEnum.SUCCESS,content);
	}
}
