package com.game.recharge.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.game.constant.UcCodeEnum;
import com.game.pay.BaseOrderConfig;
import com.game.pay.channel.ChannelConsts;
import com.game.pay.domain.KuaiYouConfig;
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
public class KuaiYouRedBagHandler extends BasePayHandler {
	@Override
	public Message validate(HttpServletRequest requetst, PayOrderService payOrderService, ChannelConfigManager channelConfigManager,String payName) {
		try {
			if (requetst.getInputStream() == null) {
				log.error("PayUtils : params {}, desc{} ", "数据流为空", UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}

			String pars2318 = IOUtils.toString(requetst.getInputStream());
			if (null == pars2318 || pars2318.equals("")) {
				log.error("PayUtils : params {}, desc{} ", pars2318, UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			log.error("PayUtils : params {}", pars2318);
			Map<String, String> param = JsonToMap(pars2318);

			BaseOrderConfig kuaiYou2318Order = JSON.parseObject(pars2318, BaseOrderConfig.class);
			String ky2318Sign = kuaiYou2318Order.getSign();

			if (null == ky2318Sign || ky2318Sign.equals("") || null == kuaiYou2318Order.getPrice() || null == kuaiYou2318Order.getTrade_sn() || null == kuaiYou2318Order.getOrder_id() || null == kuaiYou2318Order.getExtradata() || null == kuaiYou2318Order.getAppId()) {
				log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}

			if (null == ky2318Sign || ky2318Sign.equals("")) {
				log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PAY_SIGN_ERROR.getDesc());
				return new Message(UcCodeEnum.PAY_SIGN_ERROR);
			}

			param.remove("sign");
			int ky2318AppId = Integer.parseInt(param.get("appId"));
			KuaiYouConfig chanelConfigByAppId = channelConfigManager.getChanelConfigByAppId(ky2318AppId);
			if (null == chanelConfigByAppId) {
				log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.CHANNEL_CONFIG_ERROR.getDesc());
				return new Message(UcCodeEnum.CHANNEL_CONFIG_ERROR);
			}
			String formatKy2318Param = SortUtils.formatUrlParam(param, "utf-8", false) + chanelConfigByAppId.getPay_config();
			String newKy2318Sign = Md5Util.string2MD5(formatKy2318Param);
			if (!ky2318Sign.equals(newKy2318Sign)) {
				log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PAY_SIGN_ERROR.getDesc());
				return new Message(UcCodeEnum.PAY_SIGN_ERROR);
			}
			PayOrder payOrder = payOrderService.findPayOrder(kuaiYou2318Order.getOrder_id());
			int fee2318 = kuaiYou2318Order.getPrice();
			String trade_sn2318 = kuaiYou2318Order.getTrade_sn();
			String orderId2318 = kuaiYou2318Order.getOrder_id();
			Integer userid2318 = kuaiYou2318Order.getUserid();
			if (null == payOrder) {
				payOrder = new PayOrder(orderId2318, trade_sn2318, fee2318);
				payOrder.setPlatId(String.valueOf(userid2318));
				payOrderService.createOrder(payOrder);
			} else {
				payOrder.resetData(orderId2318, trade_sn2318, fee2318);
			}
			return afterValidate(payOrderService, payOrder);
		} catch (Exception e) {
			log.error("PayUtils : desc{} ", UcCodeEnum.PAY_SIGN_ERROR.getDesc());
			return new Message(UcCodeEnum.PARAM_ERROR);
		}
	}

	@Override
	public void register() {
//		HandlerManager.getInst().addHandler(ChannelConsts.KUAI_YOU__2318_RED_BAG_NAME, this);

		addHandler(ChannelConsts.KUAI_YOU__2318_RED_BAG_NAME, this);
	}
}
