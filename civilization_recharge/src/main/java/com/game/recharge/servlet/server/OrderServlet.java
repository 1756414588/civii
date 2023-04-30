package com.game.recharge.servlet.server;

import com.game.spring.SpringUtil;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.game.constant.UcCodeEnum;
import com.game.pay.channel.ChannelConsts;
import com.game.recharge.Ihandler.BasePayHandler;
import com.game.recharge.Ihandler.PayHandler;
import com.game.recharge.manager.ChannelConfigManager;
import com.game.recharge.service.PayOrderService;
import com.game.uc.Message;
import com.game.uc.PayOrder;

/**
 * 2020年5月12日
 *
 *    halo_uc OrderServlet.java
 **/

@RequestMapping("pay")
@Controller
public class OrderServlet {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@ResponseBody
	@RequestMapping("/getOrderNum")
	private Message getOrderNum(@Param("param") String param) {
		try {
			PayOrder payOrder = JSON.parseObject(param, PayOrder.class);
			PayOrderService payOrderService = SpringUtil.getBean(PayOrderService.class);
			payOrderService.createOrder(payOrder);
			return new Message(UcCodeEnum.SUCCESS);
		} catch (Exception e) {
			logger.error("getOrderNum={}", e);
			return new Message(UcCodeEnum.ERROR);
		}
	}

	/**
	 * 支付回调
	 *
	 * @param requetst
	 * @param response
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/{channelName}/notify", method = RequestMethod.POST)
	public void payBack(@PathVariable("channelName") String channelName, HttpServletRequest requetst, HttpServletResponse response) throws Exception {
		response.setContentType("text/html;charset=UTF-8");
		Writer writer = response.getWriter();
		logger.error("OrderServlet payBack : channelName {}}", channelName);
		try {
			if (null == channelName || channelName.equals("")) {
				logger.error("OrderServlet payBack : params {},desc{}}", channelName, UcCodeEnum.PARAM_ERROR.getDesc());
				writer.write(JSON.toJSONString(new Message(UcCodeEnum.PARAM_ERROR)));
				return;
			}

			PayOrderService payOrderService = SpringUtil.getBean(PayOrderService.class);
			ChannelConfigManager channelConfigManager = SpringUtil.getBean(ChannelConfigManager.class);
			PayHandler channelName1 = BasePayHandler.map.get(channelName);
			logger.error("OrderServlet payBack : channelName1 {}}", channelName1);
			Message validateOrder = null;
			if (channelName1 != null) {
				validateOrder = channelName1.validate(requetst, payOrderService, channelConfigManager, channelName);
			}
			if (validateOrder != null) {
				if (ChannelConsts.DEFAULT_CHANNEL_NAME.equals(channelName.trim())) {
					writer.write(JSON.toJSONString(validateOrder));
				} else {
					if (validateOrder.getCode() == UcCodeEnum.SUCCESS.getCode() || validateOrder.getCode() == UcCodeEnum.SUCCESS1.getCode()) {
						writer.write(validateOrder.getDesc());
					} else {
						logger.error("OrderServlet payBack : desc {}}", validateOrder.getDesc());
						writer.write("fail");
					}
				}
			} else {
				logger.error("OrderServlet payBack : validateOrder is null");
				writer.write("fail");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("OrderServlet payBack : params {},desc{}}", channelName, UcCodeEnum.SYS_ERROR.getDesc());
			writer.write(JSON.toJSONString(new Message(UcCodeEnum.SYS_ERROR)));
		} finally {
			if (null != writer) {
				writer.close();
			}
		}
	}
}
