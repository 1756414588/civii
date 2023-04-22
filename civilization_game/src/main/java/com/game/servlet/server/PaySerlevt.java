package com.game.servlet.server;

import com.game.constant.*;
import com.game.domain.s.*;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.log.consumer.domin.BaseProperties;
import com.game.manager.*;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticVipMgr;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.pay.channel.ChannelConsts;
import com.game.service.PayService;
import com.game.service.UcHttpService;
import com.game.uc.Message;
import com.game.uc.PayOrder;

import java.util.Date;
import java.util.List;

/**
 * 2020年5月12日
 *
 * @CaoBing halo_game PaySerlevt.java
 **/

@Controller
@RequestMapping("pay")
public class PaySerlevt {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @param params
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/createOrderImitate", method = RequestMethod.POST)
	public Message createOrderImitate(String params) {
		logger.info("PaySerlevt createOrderImitate : params {}", params);
		if (params == null || params.equals("")) {
			logger.error("PaySerlevt createOrderImitate : params {},desc{}}", params, UcCodeEnum.PARAM_ERROR.getDesc());
			return new Message(UcCodeEnum.PARAM_ERROR);
		}

		try {
			PayOrder payOrder = JSON.parseObject(params, PayOrder.class);

			PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
			StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
			ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
			ServerManager serverManager = SpringUtil.getBean(ServerManager.class);
			PayManager payManager = SpringUtil.getBean(PayManager.class);
			StaticVipMgr staticVipMgr = SpringUtil.getBean(StaticVipMgr.class);
			UcHttpService httpService = SpringUtil.getBean(UcHttpService.class);

			Player player = playerManager.getPlayer(payOrder.getRoleId());
			if (player == null) {
				return new Message(UcCodeEnum.PLAYER_IS_NOT_EXIST);
			}

			// 获取参数
			int platNo = 1;
			int productId = payOrder.getProductId();
			int channelId = 1;
			int productType = payOrder.getProductType();
			int serverId = serverManager.getServerId();
			String platId = null;
			if (channelId == ChannelConsts.DEFAULT_CHANNEL) {
				platId = String.valueOf(player.account.getAccountKey());
			}
			int accountKey = player.account.getAccountKey();

			int payAmount = 0;
			boolean flag = false;
			if (productType == 1) {
				StaticPay payStaticPay = staticVipMgr.getPayStaticPay(productId);
				if (null == payStaticPay) {
					flag = false;
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}
				payAmount = payStaticPay.getTopup() * 10;
				flag = true;
			} else if (productType == 2 || productType == 8) {
				StaticActPayGift payGift = staticActivityMgr.getPayGift(productId);
				if (null == payGift) {
					flag = false;
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}

				payAmount = payGift.getMoney() * 100;
				flag = true;
			} else if (productType == 3) {
				StaticActPayCard payCard = staticActivityMgr.getPayCard(productId);
				if (null == payCard) {
					flag = false;
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}

				ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_MONTH_CARD);
				if (actRecord == null) {
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}

				ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
				if (activityData == null) {
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}

				payAmount = payCard.getMoney() * 100;
				flag = true;
			} else if (productType == 4) {
				StaticActPayMoney payMoney = staticActivityMgr.getPayMoney(productId);
				if (null == payMoney) {
					flag = false;
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}

				ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_DAY_PAY);
				if (actRecord == null) {
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}

				ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
				if (activityData == null) {
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}

				payAmount = payMoney.getMoney() * 100;
				flag = true;
			} else if (productType == 5) {
				StaticPayPassPort payPassPort = staticActivityMgr.getStaticPayPassPort(productId);
				if (null == payPassPort) {
					flag = false;
				}

				ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
				if (actRecord == null) {
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}

				ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
				if (activityData == null) {
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}

				payAmount = payPassPort.getMoney() * 100;
				flag = true;
			} else if (productType == 6) {
				StaticLimitGift limitGift = staticActivityMgr.getLimitGiftByKeyId(productId);
				if (null == limitGift) {
					flag = false;
				}

				ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SURIPRISE_GIFT);
				if (actRecord == null) {
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}

				payAmount = limitGift.getMoney() * 100;
				flag = true;
			} else if (productType == 7) {
				StaticActPayMoney payMoney = staticActivityMgr.getPayMoney(productId);
				if (null == payMoney) {
					flag = false;
				}
				ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_SPECIAL_GIFT);
				if (activityBase == null) {
					return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
				}
				payAmount = payMoney.getMoney() * 100;
				flag = true;
			}

			if (!flag) {
				// 活动结束
				// 订单创建失败
				return new Message(UcCodeEnum.ACTIVITY_NOT_OPEN);
			}

			// 创建订单
			PayOrder newPayOrder = payManager.createOrderNum(
				new PayOrder(channelId, platNo, player.roleId, serverId, productType, productId, payAmount, platId, accountKey, player.getLevel()));
			newPayOrder.setRealServer(serverId);
			if (null == newPayOrder.getCpOrderId()) {
				// 创建失败
				return new Message(UcCodeEnum.SYS_ERROR);
			}
			//先创建
			Message msg = httpService.sendOrderNum(newPayOrder);
			if (msg == null || msg.getCode() != UcCodeEnum.SUCCESS.getCode()) {
				// 创建失败
				return new Message(UcCodeEnum.SYS_ERROR);
			}

			if (channelId == ChannelConsts.DEFAULT_CHANNEL) {
				Message defaultPayBack = httpService.defaultPayBack(newPayOrder);
				return defaultPayBack;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("PaySerlevt pay : params {},desc{}}", params, UcCodeEnum.PARAM_ERROR.getDesc());
			return new Message(UcCodeEnum.SYS_ERROR);
		}

		return new Message(UcCodeEnum.SYS_ERROR);
	}

	/**
	 * 支付回调接口
	 *
	 * @param params
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/payBack", method = RequestMethod.POST)
	public Message payBack(String params) {
		if (params == null || params.equals("")) {
			logger.error("PaySerlevt payBack : params {},desc{}}", params, UcCodeEnum.PARAM_ERROR.getDesc());
			return new Message(UcCodeEnum.PARAM_ERROR);
		}

		PayOrder payOrder = null;

		try {
			payOrder = JSON.parseObject(params, PayOrder.class);
			PayService payService = SpringUtil.getBean(PayService.class);
			PayManager payManager = SpringUtil.getBean(PayManager.class);

			PayOrder findPayOrder = payManager.findPayOrder(payOrder.getCpOrderId());
			if (null == findPayOrder) {
				logger.error("PaySerlevt payBack : params {},desc{}}", params, UcCodeEnum.PAY_ORDER_NOT_EXIST.getDesc());
				return new Message(UcCodeEnum.PAY_ORDER_NOT_EXIST);
			}

			if (findPayOrder.getStatus() == PayOrder.ORDER_SUCCESS) {
				logger.error("PaySerlevt payBack : params {},desc{}}", params, UcCodeEnum.PAY_ORDER_SUCCESSED_ERROR.getDesc());
				return new Message(UcCodeEnum.PAY_ORDER_SUCCESSED_ERROR);
			}

			Integer realAmount = payOrder.getRealAmount();
			Integer payAmount = findPayOrder.getPayAmount();
			if (realAmount.intValue() != payAmount.intValue()) {
				logger.error("OrderServlet pay : params {},desc{}}", params, UcCodeEnum.PAY_PRICE_ERROR.getDesc());
				return new Message(UcCodeEnum.PAY_PRICE_ERROR);
			}

			findPayOrder.setRealAmount(realAmount);
			findPayOrder.setSpOrderId(payOrder.getSpOrderId());
			String platId = payOrder.getPlatId();
			if (null != platId && !platId.equals("")) {
				findPayOrder.setPlatId(platId);
			}
			Player player = SpringUtil.getBean(PlayerManager.class).getPlayer(findPayOrder.getRoleId());
			boolean isFirst = false;
			if (player != null) {
				isFirst = player.getLord().getTopup() == 0;
			}
//            player.setLogin(true);
			if (payService.payBack(findPayOrder)) {
				LogHelper.MESSAGE_LOGGER.info("Pay add gold success");
				int diamond = findPayOrder.getPayAmount() / 10;
				SpringUtil.getBean(WorldBoxManager.class).calcuPoints(WorldBoxTask.DO_RECHARGE, player, diamond);
				player.getLord().addDayRecharge();
				if (player != null && findPayOrder.getChannelId() != ChannelConsts.DEFAULT_CHANNEL) {
					List<Object> param = Lists.newArrayList(findPayOrder.getCpOrderId(), payAmount / 100f, "test", "test", findPayOrder.getProductId(), "test", payOrder.getCpOrderId(), new Date(), false, isFirst, payService.getPayGiftName(payOrder),payOrder.getProductType(),payOrder.getProductId());
					BaseProperties baseProperties = SpringUtil.getBean(EventManager.class).order_event(player, param);
					SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.first_pay);
					if (baseProperties != null) {
						SpringUtil.getBean(XinkuaiManager.class).pushXinkuai(baseProperties, EventType.TRACK.getType());
					}
				}
				return new Message(UcCodeEnum.SUCCESS, JSON.toJSONString(findPayOrder));
			} else {
				return new Message(UcCodeEnum.PAY_ORDER_CONSUME_ERROR, JSON.toJSONString(findPayOrder));
			}
		} catch (Exception e) {
			e.printStackTrace();

			logger.error("PaySerlevt payBack : params {},desc{}}", params, UcCodeEnum.PARAM_ERROR.getDesc());
			return new Message(UcCodeEnum.SYS_ERROR);
		}
	}
}
