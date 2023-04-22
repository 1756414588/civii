package com.game.recharge.handler;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.game.constant.UcCodeEnum;
import com.game.pay.channel.ChannelConsts;
import com.game.recharge.Ihandler.BasePayHandler;
import com.game.recharge.manager.ChannelConfigManager;
import com.game.recharge.service.PayOrderService;
import com.game.uc.Message;
import com.game.uc.PayOrder;
import com.game.util.Md5Util;
import com.game.util.SortUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DefaultValidateHandler extends BasePayHandler {

	@Override
	public Message validate(HttpServletRequest requetst, PayOrderService payOrderService, ChannelConfigManager channelConfigManager,String payName) {
		PayOrder payOrder = null;
		String defaultSign = requetst.getParameter("sign");
		Enumeration<String> parameterNames = requetst.getParameterNames();
		Map<String, String> param = new HashMap<>();
		while (parameterNames.hasMoreElements()) {
			String nextElement = parameterNames.nextElement();
			if (null == requetst.getParameter(nextElement)) {
				log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			param.put(nextElement, requetst.getParameter(nextElement));
		}
		if (null == defaultSign || defaultSign.equals("") || null == requetst.getParameter("payAmount") || null == requetst.getParameter("serverId") || null == requetst.getParameter("orderNum")) {
			log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PARAM_ERROR.getDesc());
			return new Message(UcCodeEnum.PARAM_ERROR);
		}

		if (null == defaultSign || defaultSign.equals("")) {
			log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PAY_SIGN_ERROR.getDesc());
			return new Message(UcCodeEnum.PAY_SIGN_ERROR);
		}
		log.error("PayUtils : params {}, desc{} ", param);

		param.remove("sign");
		String formatDefaultParam = SortUtils.formatUrlParam(param, "utf-8", false) + ChannelConsts.DEFAULT_CHANNEL_KEY;
		String newDefaultSign = Md5Util.string2MD5(formatDefaultParam);
		if (!defaultSign.equals(newDefaultSign)) {
			log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PAY_SIGN_ERROR.getDesc());
			return new Message(UcCodeEnum.PAY_SIGN_ERROR);
		}
		payOrder = payOrderService.findPayOrder(requetst.getParameter("orderNum"));
		int payAmount = Integer.parseInt(requetst.getParameter("payAmount"));
		String cporderNUm = requetst.getParameter("orderNum");
		if (null == payOrder) {
			payOrder = new PayOrder(cporderNUm, cporderNUm, payAmount);
			payOrderService.createOrder(payOrder);
		} else {
			payOrder.resetData(cporderNUm, cporderNUm, payAmount);
		}
		return afterValidate(payOrderService, payOrder);
	}

	@Override
	public void register() {
		addHandler(ChannelConsts.DEFAULT_CHANNEL_NAME, this);
	}
}
