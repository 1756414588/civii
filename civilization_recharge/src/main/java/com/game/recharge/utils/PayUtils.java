//package com.game.recharge.utils;
//
//import java.util.*;
//
//import javax.servlet.http.HttpServletRequest;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.game.constant.UcCodeEnum;
//import com.game.recharge.manager.ServerManager;
//import com.game.recharge.service.PayOrderService;
//import com.game.spring.BeanFactory;
//import com.game.uc.Message;
//import com.game.uc.PayOrder;
//import com.game.uc.Server;
//import com.game.util.HttpUtil;
//
//import lombok.extern.slf4j.Slf4j;
//
///**
// * 2020年7月1日
// *
// *    halo_common PayUtils.java
// **/
//@Slf4j
//public class PayUtils {
//	public static final String prefix = "http://";
//
//	/**
//	 * 最后验证
//	 *
//	 * @param payOrderService
//	 * @param payOrder
//	 * @return
//	 */
//	public static Message afterValidate(PayOrderService payOrderService, PayOrder payOrder) {
//		boolean b = sendToServer(payOrderService, payOrder);
//		if (b) {
//			return new Message(UcCodeEnum.SUCCESS, JSON.toJSONString(payOrder));
//		}
//		return new Message(UcCodeEnum.PAY_ORDER_CONSUME_ERROR, JSON.toJSONString(payOrder));
//	}
//
//    public static Message shouMafterValidate(PayOrderService payOrderService, PayOrder payOrder) {
//		boolean b = sendToServer(payOrderService, payOrder);
//		if (b) {
//			return new Message(UcCodeEnum.SUCCESS1, JSON.toJSONString(payOrder));
//		}
//		return new Message(UcCodeEnum.PAY_ORDER_CONSUME_ERROR, JSON.toJSONString(payOrder));
//	}
//
//	public static boolean sendToServer(PayOrderService payOrderService, PayOrder payOrder) {
//		boolean flag = false;
//		Map<String, String> urlPar = new HashMap<String, String>();
//		urlPar.put("params", JSON.toJSONString(payOrder));
//		Server realServer = BeanFactory.getBean(ServerManager.class).getServerById(payOrder.getRealServer());
//		if (realServer == null) {
//			log.error("PayUtils : params {}, desc{} ", JSON.toJSONString(payOrder), UcCodeEnum.SERVER_NOT_EXIST.getDesc());
//			return flag;
//		}
//		String message = HttpUtil.sendPost(PayUtils.prefix + realServer.getIp() + ":" + realServer.getHttpPort() + "/pay/payBack", urlPar);
//		Message mes = JSON.parseObject(message, Message.class);
//		int code = mes.getCode();
//		PayOrder newPayOrder = JSON.parseObject(mes.getData(), PayOrder.class);
//		if (code == UcCodeEnum.SUCCESS.getCode()) {
//			log.error("PayUtils : params {}, desc{}", JSON.toJSONString(payOrder), mes.getDesc());
//			payOrder.setStatus(PayOrder.ORDER_SUCCESS);
//			flag = true;
//		} else if (code == UcCodeEnum.PAY_ORDER_CONSUME_ERROR.getCode()) {
//			log.error("PayUtils : params {}, desc{} ", JSON.toJSONString(payOrder), mes.getDesc());
//			payOrder.setStatus(PayOrder.ORDER_SYN);
//		} else {
//			log.error("PayUtils : params {},desc {} ", JSON.toJSONString(payOrder), mes.getDesc());
//
//		}
//		newPayOrder.setKeyId(payOrder.getKeyId());
//		newPayOrder.setFinishTime(new Date());
//		payOrderService.updatePayOrder(newPayOrder);
//		return flag;
//
//	}
//
//	private static Map<String, String> JsonToMap(String stObj) throws Exception {
//		Map<String, String> resultMap = new HashMap<>();
//		if (stObj == null || stObj.equals("")) {
//			return resultMap;
//		}
//		JSONObject parseObject = JSON.parseObject(stObj);
//		Set<String> keySet = parseObject.keySet();
//		for (String string : keySet) {
//			if (null != parseObject.get(string) && !parseObject.get(string).equals("")) {
//				resultMap.put(string, String.valueOf(parseObject.get(string)));
//			}
//		}
//		log.error("PayUtils : method {}, params {}", Thread.currentThread().getStackTrace()[1].getMethodName(), resultMap);
//		return resultMap;
//	}
//
//	private void ParaToMap(HttpServletRequest requetst, boolean isDecode) {
//		Enumeration<String> parameterNames = requetst.getParameterNames();
//		Map<String, String> param = new HashMap<>();
//		while (parameterNames.hasMoreElements()) {
//			String nextElement = parameterNames.nextElement();
//			if (null == requetst.getParameter(nextElement)) {
//				continue;
//			}
//			param.put(nextElement, requetst.getParameter(nextElement));
//		}
//	}
//}
