package com.game.recharge.Ihandler;

import com.game.spring.SpringUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.game.constant.UcCodeEnum;
import com.game.recharge.manager.ServerManager;
import com.game.recharge.service.PayOrderService;
import com.game.uc.Message;
import com.game.uc.PayOrder;
import com.game.uc.Server;
import com.game.util.HttpUtil;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

@Slf4j
public abstract class BasePayHandler implements PayHandler {

	public static final String prefix = "http://";

	public static final Map<String, PayHandler> map = new HashMap<>();

	@PostConstruct
	public abstract void register();

	public void addHandler(String name, PayHandler hanlder) {
		map.put(name, hanlder);
	}

	/**
	 * 最后验证
	 *
	 * @param payOrderService
	 * @param payOrder
	 * @return
	 */
	public Message afterValidate(PayOrderService payOrderService, PayOrder payOrder) {
		boolean b = sendToServer(payOrderService, payOrder);
		if (b) {
			return new Message(UcCodeEnum.SUCCESS, JSON.toJSONString(payOrder));
		}
		return new Message(UcCodeEnum.PAY_ORDER_CONSUME_ERROR, JSON.toJSONString(payOrder));
	}

	public Message shouMafterValidate(PayOrderService payOrderService, PayOrder payOrder) {
		boolean b = sendToServer(payOrderService, payOrder);
		if (b) {
			return new Message(UcCodeEnum.SUCCESS1, JSON.toJSONString(payOrder));
		}
		return new Message(UcCodeEnum.PAY_ORDER_CONSUME_ERROR, JSON.toJSONString(payOrder));
	}

	public boolean sendToServer(PayOrderService payOrderService, PayOrder payOrder) {
		boolean flag = false;
		Map<String, String> urlPar = new HashMap<String, String>();
		urlPar.put("params", JSON.toJSONString(payOrder));
		Server realServer = SpringUtil.getBean(ServerManager.class).getServerById(payOrder.getRealServer());
		if (realServer == null) {
			log.error("PayUtils : params {}, desc{} ", JSON.toJSONString(payOrder), UcCodeEnum.SERVER_NOT_EXIST.getDesc());
			return flag;
		}
		String message = HttpUtil.sendPost(prefix + realServer.getIp() + ":" + realServer.getHttpPort() + "/pay/payBack", urlPar);
		Message mes = JSON.parseObject(message, Message.class);
		int code = mes.getCode();
		PayOrder newPayOrder = JSON.parseObject(mes.getData(), PayOrder.class);
		if (code == UcCodeEnum.SUCCESS.getCode()) {
			log.error("PayUtils : params {}, desc{}", JSON.toJSONString(payOrder), mes.getDesc());
			payOrder.setStatus(PayOrder.ORDER_SUCCESS);
			flag = true;
		} else if (code == UcCodeEnum.PAY_ORDER_CONSUME_ERROR.getCode()) {
			log.error("PayUtils : params {}, desc{} ", JSON.toJSONString(payOrder), mes.getDesc());
			payOrder.setStatus(PayOrder.ORDER_SYN);
		} else {
			log.error("PayUtils : params {},desc {} ", JSON.toJSONString(payOrder), mes.getDesc());

		}
		newPayOrder.setKeyId(payOrder.getKeyId());
		newPayOrder.setFinishTime(new Date());
		payOrderService.updatePayOrder(newPayOrder);
		return flag;

	}
}
