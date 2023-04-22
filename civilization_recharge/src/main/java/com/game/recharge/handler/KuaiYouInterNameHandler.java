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

@Slf4j
@Component
public class KuaiYouInterNameHandler extends BasePayHandler {
	@Override
	public Message validate(HttpServletRequest requetst, PayOrderService payOrderService, ChannelConfigManager channelConfigManager,String payName) {
		try {
			// 判断数据流是否为空
			if (requetst.getInputStream() == null) {
				log.error("PayUtils : params {}, desc{} ", "数据流为空", UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}

			// 流转字符串
			String pars_en = IOUtils.toString(requetst.getInputStream());

			// 判断pars_en是否为空
			if (null == pars_en || pars_en.equals("")) {
				log.error("PayUtils : params {}, desc{} ", pars_en, UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			// 打印日志
			log.error("PayUtils : params {}", pars_en);

			// 将pars_en数据存到map
			Map<String, String> param = JsonToMap(pars_en);
			// 将数据存放到KuaiYouInternationOrder实体类
			BaseOrderConfig kuaiyou_en_user = JSON.parseObject(pars_en, BaseOrderConfig.class);

			// 获取新快加密
			String kySign_en = kuaiyou_en_user.getSign();

			// 判断数据是否为空
			if (null == kySign_en || kySign_en.equals("") || null == kuaiyou_en_user.getFee() || null == kuaiyou_en_user.getTrade_sn() || null == kuaiyou_en_user.getOrder_id() || null == kuaiyou_en_user.getExtradata() || null == kuaiyou_en_user.getAppid()) {
				log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			if (null == kySign_en || kySign_en.equals("")) {
				log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PAY_SIGN_ERROR.getDesc());
				return new Message(UcCodeEnum.PAY_SIGN_ERROR);
			}

			// 移除新快加密键值对
			param.remove("sign");

			// 获取项目id
			int kyAppId_en = Integer.parseInt(param.get("appid"));

			// 根据项目id获取渠道信息
			KuaiYouConfig kuaiYouConfig_en =channelConfigManager.getChanelConfigByAppId(kyAppId_en);
			// 判断渠道信息是否为空
			if (null == kuaiYouConfig_en) {
				log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.CHANNEL_CONFIG_ERROR.getDesc());
				return new Message(UcCodeEnum.CHANNEL_CONFIG_ERROR);
			}

			// 字典排序用户信息
			String formatKyParam_en = SortUtils.formatUrlParam(param, "utf-8", false) + kuaiYouConfig_en.getPay_config();
			// MD5处理用户信息
			String newKySign_en = Md5Util.string2MD5(formatKyParam_en);

			// 判断客户端与用户端新快加密是否相等
			if (!kySign_en.equals(newKySign_en)) {
				log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PAY_SIGN_ERROR.getDesc());
				return new Message(UcCodeEnum.PAY_SIGN_ERROR);
			}

			// 查询订单状态信息
			PayOrder payOrder = payOrderService.findPayOrder(kuaiyou_en_user.getOrderid());

			// 获取商品价格
			int fee_en = (int) (kuaiyou_en_user.getFee() * 100);
			// 新快账单号
			String trade_sn_en = kuaiyou_en_user.getTrade_sn();
			// 游戏方账单号
			String orderId_en = kuaiyou_en_user.getOrderid();
			// 用户唯一id
			Integer userid_en = kuaiyou_en_user.getUserid();
			// 判断订单状态信息是否为空
			if (null == payOrder) {
				payOrder = new PayOrder(orderId_en, trade_sn_en, fee_en);
				payOrder.setPlatId(String.valueOf(userid_en));
				payOrderService.createOrder(payOrder);
			} else {
				payOrder.resetData(orderId_en, trade_sn_en, fee_en);
			}
			return afterValidate(payOrderService, payOrder);
		} catch (Exception e) {
			log.error("PayUtils :desc{} ", UcCodeEnum.PAY_SIGN_ERROR.getDesc());
			return new Message(UcCodeEnum.PARAM_ERROR);
		}
	}

	@Override
	public void register() {
		addHandler(ChannelConsts.KUAI_INTERNATION_NAME, this);
	}
}
