package com.game.recharge.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
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
import com.game.util.SortUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class A1567PayHandler extends BasePayHandler {
	@Override
	public void register() {
		addHandler(TypeIndef.A1576_ANDROID.getPayName(), this);
		addHandler(TypeIndef.A1576_IOS.getPayName(), this);
	}

	@Override
	public Message validate(HttpServletRequest requetst, PayOrderService payOrderService, ChannelConfigManager channelConfigManager, String payName) {
		try {
			if (requetst.getInputStream() == null) {
				log.error("AKuaiYouPayHandler : params {}, desc{} ", "数据流为空", UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			String pars = IOUtils.toString(requetst.getInputStream());
			if (null == pars || pars.equals("")) {
				log.error("AKuaiYouPayHandler : params {}, desc{} ", pars, UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			log.error("AKuaiYouPayHandler : params {}", pars);
			KuaiYouConfig chanelConfigByPayName = channelConfigManager.getChanelConfigByPayName(payName);
			if (chanelConfigByPayName == null) {
				log.error("AKuaiYouPayHandler : chanelConfigByPayName {}", chanelConfigByPayName);
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			Map<String, String> param = JsonToMap(pars);
			String sign = param.remove("sign");
			String formatUrlParam = SortUtils.formatUrlParam(param, "utf-8", false) + chanelConfigByPayName.getPay_config();
			String newKySign = Md5Util.string2MD5(formatUrlParam);
			if (!sign.equals(newKySign)) {
				log.error("AKuaiYouPayHandler : sign {}, newKySign{} ", sign, newKySign);
				return new Message(UcCodeEnum.PAY_SIGN_ERROR);
			}
			String cp_order_id = param.get("orderId");
			PayOrder payOrder = payOrderService.findPayOrder(cp_order_id);
			String money = param.get("fee");
			String trade_sn = param.get("trade_sn");
			String user_id = param.get("user_id");

			int realm = (int) (Float.parseFloat(money) * 100);
			if (null == payOrder) {
				payOrder = new PayOrder(cp_order_id, trade_sn, realm);
				payOrder.setPlatId(String.valueOf(user_id));
				payOrderService.createOrder(payOrder);
			} else {
				if (realm != payOrder.getPayAmount()) {
					log.error("AKuaiYouPayHandler : 订单金额不对 last {} cru {}", payOrder.getPayAmount(), realm);
					return new Message(UcCodeEnum.PARAM_ERROR);
				}
				payOrder.resetData(cp_order_id, trade_sn, realm);
			}
			return afterValidate(payOrderService, payOrder);
		} catch (Exception e) {
			log.error("PayUtils : desc{} ", UcCodeEnum.PAY_SIGN_ERROR.getDesc());
			log.error("AKuaiYouPayHandler : error{} ", e);
			return new Message(UcCodeEnum.PARAM_ERROR);
		}
	}
}
