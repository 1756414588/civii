package com.game.message.handler.ss;

import com.game.message.handler.ServerHandler;

public class PayBackRqHandler extends ServerHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
//		PayBackRq req = msg.getExtension(PayBackRq.ext);
//		PayService payService = SpringUtil.getBean(PayService.class);
//		PayDao payDao = SpringUtil.getBean(PayDao.class);
//		Pay pay = payDao.selectPay(req.getPlatNo(), req.getOrderId());
//		if (pay != null) {
//			return;
//		}
//
////		System.out.println("PayBackRqHandler action");
//
//		pay = new Pay();
//		pay.setPlatNo(req.getPlatNo());
//		pay.setPlatId(req.getPlatId());
//		pay.setOrderId(req.getOrderId());
//		pay.setSerialId(req.getSerialId());
//		pay.setServerId(req.getServerId());
//		pay.setRoleId(req.getRoleId());
//		pay.setAmount(req.getAmount());
//		pay.setPayTime(new Date());
//		if (req.hasChannelId()) {
//		    pay.setChannelId(req.getChannelId());
//        }
//		payDao.createPay(pay);

//		System.out.println("PayBackRqHandler pay logic");
		//payService.payBackRq(req, this);
	}

}
