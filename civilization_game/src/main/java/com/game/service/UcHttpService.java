package com.game.service;

import java.util.HashMap;
import java.util.Map;

import com.game.util.LogHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
//import com.game.common.ServerSetting;
import com.game.manager.ServerManager;
import com.game.pay.channel.ChannelConsts;
import com.game.uc.Message;
import com.game.uc.PayOrder;
import com.game.util.HttpUtil;
import com.game.util.Md5Util;
import com.game.util.SortUtils;

/**
 *
 * @date 2020/4/10 10:57
 * @description
 */
@Service
public class UcHttpService {

	@Autowired
	private ServerManager serverManager;

	/**
	 * 校验账号
	 *
	 * @param keyId
	 * @param token
	 * @param serverId
	 * @return
	 */
	public Message verifyToUc(int keyId, String token, int serverId, int channel) {
		Map<String, String> parm = new HashMap<>();
		parm.put("keyId", String.valueOf(keyId));
		parm.put("token", token);
		parm.put("serverId", String.valueOf(serverId));
		parm.put("channel", String.valueOf(channel));
		String url = serverManager.getAccountServerUrl() + "/account/verifyAccount.do";
		String msg = HttpUtil.sendPost(url, parm);
		return JSONObject.parseObject(msg, Message.class);
	}

	public Message getServer(int serverId) {
		Map<String, String> parm = new HashMap<>();
		parm.put("serverId", String.valueOf(serverId));
		String url = serverManager.getAccountServerUrl() + "/server/findServer.do";
		String msg = HttpUtil.sendPost(url, parm);
		return JSONObject.parseObject(msg, Message.class);
	}

	/**
	 * @return
	 */
	public Message defaultPayBack(PayOrder payOrder) {
		Map<String, String> param = new HashMap<>();

		int platNo = payOrder.getPlatNo();
		int payAmount = payOrder.getPayAmount();
		int productId = payOrder.getProductId();
		int channelId = payOrder.getChannelId();
		int productType = payOrder.getProductType();
		int serverId = payOrder.getServerId();
		String cpOrderId = payOrder.getCpOrderId();
		String platId = payOrder.getPlatId();
		int accountKey = payOrder.getAccountKey();
		long roleId = payOrder.getRoleId();

		param.put("platNo", String.valueOf(platNo));
		param.put("payAmount", String.valueOf(payAmount));
		param.put("productId", String.valueOf(productId));
		param.put("channelId", String.valueOf(channelId));
		param.put("productType", String.valueOf(productType));
		param.put("serverId", String.valueOf(serverId));
		param.put("platId", platId);
		param.put("accountKey", String.valueOf(accountKey));
		param.put("orderNum", cpOrderId);
		param.put("roleId", String.valueOf(roleId));
		param.put("lv", payOrder.getLv() + "");
		param.put("realServer", payOrder.getRealServer() + "");

		String formatUrlParam = SortUtils.formatUrlParam(param, "utf-8", false) + ChannelConsts.DEFAULT_CHANNEL_KEY;
		String sign = Md5Util.string2MD5(formatUrlParam);
		param.put("sign", String.valueOf(sign));

		String url = serverManager.getPayServerUrl() + "/pay/" + ChannelConsts.DEFAULT_CHANNEL_NAME + "/notify";
		String msg = HttpUtil.sendPost(url, param);
		return JSONObject.parseObject(msg, Message.class);
	}


	public Message getCdkAward(long lorldId, int channel, int serverId, String cdk, int level) {
		Map<String, String> parm = new HashMap<>();
		parm.put("cdk", cdk);
		parm.put("roleId", String.valueOf(lorldId));
		parm.put("serverId", String.valueOf(serverId));
		parm.put("channel", String.valueOf(channel));
		parm.put("level", String.valueOf(level));
		String url = serverManager.getAccountServerUrl() + "/cdk/getCdkAward.do";
		String msg = HttpUtil.sendPost(url, parm);
		return JSONObject.parseObject(msg, Message.class);
	}

	/**
	 * 校验账号
	 *
	 * @param keyId
	 * @param token
	 * @param serverId
	 * @return
	 */
	public Message createSuccessToUc(int keyId, String token, int serverId) {
		try {
			Map<String, String> parm = new HashMap<>();
			parm.put("keyId", String.valueOf(keyId));
			parm.put("token", token);
			parm.put("serverId", String.valueOf(serverId));
			String url = serverManager.getAccountServerUrl() + "/account/updateServerInfos.do";
			String msg = HttpUtil.sendPost(url, parm);
			return JSONObject.parseObject(msg, Message.class);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("通知uc角色创建成功失败...:{}", e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 通知支付创建订单
	 *
	 * @param payOrder
	 * @return
	 */
	public Message sendOrderNum(PayOrder payOrder) {
		String paramStr = JSONObject.toJSONString(payOrder);
		Map<String, String> param = new HashMap<>();
		param.put("param", paramStr);
		String url = serverManager.getPayServerUrl() + "/pay/getOrderNum";
		String msg = HttpUtil.sendPost(url, param);
		return JSONObject.parseObject(msg, Message.class);
	}
}
