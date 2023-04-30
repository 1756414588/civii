package com.game.servlet.server;

import com.game.spring.SpringUtil;
import java.util.Date;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.game.constant.UcCodeEnum;
import com.game.manager.ServerRadioManager;
import com.game.servlet.domain.ServerRadio;
import com.game.uc.Message;

/**
 * 2020年6月29日
 *
 *    halo_game
 * RadioServlet.java
 * 广播接口类
 **/

@RequestMapping("radio")
@Controller
public class RadioServlet {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @ResponseBody
    @RequestMapping(value = "/sendRadio", method = RequestMethod.POST)
    public Message addMessage(String params) {
        long keyId = 0;
        try {
            logger.info("RadioServlet sendRadio : parames {}", params);

            if (!StringUtils.isNotEmpty(params)) {
                logger.error("RadioServlet sendRadio : parames {},desc{}", params, UcCodeEnum.PARAM_ERROR.getDesc());
                return new Message(UcCodeEnum.PARAM_ERROR);
            }

            /**
             * json字符串转java对象
             */
            ServerRadio serverRadio = JSON.parseObject(params, ServerRadio.class);
            String channel = serverRadio.getChannel();
            Integer language = serverRadio.getLanguage();
            Integer frequency = serverRadio.getFrequency();
            String message = serverRadio.getMessage();
            Date startTime = serverRadio.getStartTime();
            Date endTime = serverRadio.getEndTime();
            if (serverRadio.getChannelList() != null) {
                channel = JSONArray.toJSONString(serverRadio.getChannelList());
            }

            if (null == channel || null == language || null == frequency || null == message || null == startTime || null == endTime) {
                logger.error("RadioServlet sendRadio : parames {},desc{}", params, UcCodeEnum.PARAM_ERROR.getDesc());
                return new Message(UcCodeEnum.PARAM_ERROR);
            }

            ServerRadioManager serverRadioManager = SpringUtil.getBean(ServerRadioManager.class);
            keyId = serverRadioManager.addServerRadio(serverRadio);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("RadioServlet sendRadio {},desc{}", params, UcCodeEnum.SYS_ERROR.getDesc());
            logger.error(e.toString());
            return new Message(UcCodeEnum.SYS_ERROR);
        }
        return new Message(UcCodeEnum.SUCCESS, String.valueOf(keyId));
    }
	
	/*@Test
	public void aaa() {
		ServerRadio radio = new ServerRadio();
		radio.setChannel(1);
		radio.setFrequency(3);
		//String format = DateHelper.dateFormat1.format("2020-6-28 00:00:00");
		radio.setStartTime(new Date());
		radio.setEndTime(new Date());
		radio.setLanguage(1);
		radio.setMessage("我是superMan");
		radio.setStatus(0);
		
		System.out.println(JSON.toJSONString(radio));
	}*/

    @ResponseBody
    @RequestMapping(value = "/deleteMail", method = RequestMethod.POST)
    public Message deleteMail(String keyId) {
        logger.info("RadioServlet deleteMail : parames {}", keyId);
        try {

            if (!StringUtils.isNotEmpty(keyId)) {
                logger.error("RadioServlet deleteMail : parames {},desc{}", keyId, UcCodeEnum.PARAM_ERROR.getDesc());
                return new Message(UcCodeEnum.PARAM_ERROR);
            }

            ServerRadioManager serverRadioManager = SpringUtil.getBean(ServerRadioManager.class);
            ServerRadio findServerRadio = serverRadioManager.findServerRadio(Long.parseLong(keyId));
            if (null != findServerRadio) {
                serverRadioManager.deleteServerRadio(Long.parseLong(keyId));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("RadioServlet deleteMail : parames {},desc{}", keyId, UcCodeEnum.SYS_ERROR.getDesc());
            logger.error(e.toString());
            return new Message(UcCodeEnum.SYS_ERROR);
        }
        return new Message(UcCodeEnum.SUCCESS, String.valueOf(keyId));
    }
}
