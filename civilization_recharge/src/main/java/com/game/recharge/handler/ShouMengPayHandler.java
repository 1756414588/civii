package com.game.recharge.handler;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.game.constant.UcCodeEnum;
import com.game.pay.channel.TypeIndef;
import com.game.pay.domain.KuaiYouConfig;
import com.game.recharge.Ihandler.BasePayHandler;
import com.game.recharge.manager.ChannelConfigManager;
import com.game.recharge.service.PayOrderService;
import com.game.uc.Message;
import com.game.uc.PayOrder;
import com.game.util.Md5Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ShouMengPayHandler extends BasePayHandler {
	@Override
	public Message validate(HttpServletRequest requetst, PayOrderService payOrderService, ChannelConfigManager channelConfigManager, String payName) {
		try {
			KuaiYouConfig chanelConfigByPayName = channelConfigManager.getChanelConfigByPayName(payName);
			if (chanelConfigByPayName == null) {
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			Enumeration<String> parameterNames = requetst.getParameterNames();
			Map<String, String> param = new HashMap<>();

			while (parameterNames.hasMoreElements()) {
				String nextElement = parameterNames.nextElement();
				String parameter = requetst.getParameter(nextElement);
				if (null == parameter) {
					log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PARAM_ERROR.getDesc());
					return new Message(UcCodeEnum.PARAM_ERROR);
				}
				param.put(nextElement, parameter);
			}
			String sign = param.get("sign");
			if (null == sign || sign.equals("")) {
				log.error("PayUtils : params {}, desc{} ", UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			StringBuilder builder = new StringBuilder();
			String orderId = param.get("orderId");
			String uid = param.get("uid");
			String amount1 = param.get("amount");
			String coOrderId = param.get("coOrderId");
			String success = param.get("success");
			builder.append("orderId=").append(orderId).append("&").
					append("uid=").append(uid).append("&").
					append("amount=").append(amount1).append("&").
					append("coOrderId=").append(coOrderId).append("&").
					append("success=").append(success).append("&").
					append("secret=").append(chanelConfigByPayName.getPay_config().trim());
			log.error("PayUtils : builder {}", builder.toString());
			String newKySign = Md5Util.string2MD5(builder.toString());
			if (!sign.equals(newKySign)) {
				log.error("PayUtils : sign {}, newKySign{} ", sign, newKySign);
				return new Message(UcCodeEnum.PAY_SIGN_ERROR);
			}
			int amount = Integer.parseInt(amount1);
			PayOrder payOrder = payOrderService.findPayOrder(coOrderId);
			if (null == payOrder) {
				payOrder = new PayOrder(coOrderId, orderId, amount * 100);
				payOrder.setPlatId(String.valueOf(uid));
				payOrderService.createOrder(payOrder);
			} else {
				if (amount * 100 != payOrder.getPayAmount()) {
					log.error("KwPayHandler : 订单金额不对 last {} cru {}", payOrder.getPayAmount(), amount * 100);
					return new Message(UcCodeEnum.PARAM_ERROR);
				}
				payOrder.resetData(coOrderId, orderId, amount * 100);
			}
			return shouMafterValidate(payOrderService, payOrder);
		} catch (Exception e) {
			log.error("PayUtils : , desc{} ", UcCodeEnum.PAY_SIGN_ERROR.getDesc());
			return new Message(UcCodeEnum.PARAM_ERROR);
		}
	}

	@Override
	public void register() {
//		HandlerManager.getInst().addHandler(TypeIndef.SM.name(), this);
		addHandler(TypeIndef.SM.getPayName(), this);
	}
}
