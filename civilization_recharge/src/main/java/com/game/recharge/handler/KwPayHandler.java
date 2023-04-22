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

@Slf4j
@Component
public class KwPayHandler extends BasePayHandler {
	@Override
	public Message validate(HttpServletRequest requetst, PayOrderService payOrderService, ChannelConfigManager channelConfigManager, String payName) {
		try {
			if (requetst.getInputStream() == null) {
				log.error("PayUtils : params {}, desc{} ", "数据流为空", UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			String pars = IOUtils.toString(requetst.getInputStream());
			if (null == pars || pars.equals("")) {
				log.error("PayUtils : params {}, desc{} ", pars, UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			log.error("PayUtils : params {}", pars);

			KuaiYouConfig chanelConfigByPayName = channelConfigManager.getChanelConfigByPayName(payName);
			if (chanelConfigByPayName == null) {
				log.error("PayUtils : chanelConfigByPayName {}", chanelConfigByPayName);
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			Map<String, String> param = JsonToMap(pars);
			String sign = param.remove("sign");
			param.remove("ext");
			String formatUrlParam = SortUtils.formatUrlParam(param, "utf-8", false);
			String newKySign = Md5Util.string2MD5(Md5Util.string2MD5(formatUrlParam) + "|" + chanelConfigByPayName.getPay_config());
			if (!sign.equals(newKySign)) {
				log.error("PayUtils : sign {}, newKySign{} ", sign, newKySign);
				return new Message(UcCodeEnum.PAY_SIGN_ERROR);
			}
			String cp_order_id = param.get("cp_order_id");
			PayOrder payOrder = payOrderService.findPayOrder(cp_order_id);
			String money = param.get("money");
			String order_id = param.get("order_id");
			String user_id = param.get("user_id");
			if (null == payOrder) {
				payOrder = new PayOrder(cp_order_id, order_id, Integer.parseInt(money) * 100);
				payOrder.setPlatId(String.valueOf(user_id));
				payOrderService.createOrder(payOrder);
			} else {
				if (Integer.parseInt(money) * 100 != payOrder.getPayAmount()) {
					log.error("KwPayHandler : 订单金额不对 last {} cru {}", payOrder.getPayAmount(), Integer.parseInt(money) * 100);
					return new Message(UcCodeEnum.PARAM_ERROR);
				}
				payOrder.resetData(cp_order_id, order_id, Integer.parseInt(money) * 100);
			}
			return afterValidate(payOrderService, payOrder);
		} catch (Exception e) {
			log.error("PayUtils : desc{} ", UcCodeEnum.PAY_SIGN_ERROR.getDesc());
			return new Message(UcCodeEnum.PARAM_ERROR);
		}
	}

	@Override
	public void register() {
		addHandler(TypeIndef.KW.getPayName(), this);
		addHandler(TypeIndef.KW_IOS.getPayName(), this);
		addHandler(TypeIndef.KW_IOS_RELEASE.getPayName(), this);
	}

}
