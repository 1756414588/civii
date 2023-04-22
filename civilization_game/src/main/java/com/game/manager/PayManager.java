package com.game.manager;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.p.PayOrderDao;
import com.game.uc.PayOrder;

/**
*2020年5月19日
*@CaoBing
*halo_game
*PayManager.java
**/
@Component
public class PayManager {
	@Autowired
	private PayOrderDao payDao;
	
	public PayOrder createOrderNum(PayOrder payOrder) {
		if (payDao.insertSelective(payOrder) > 0) {
			//String orderNum = DateHelper.orderNumTime() + RandomHelper.getOrderIdByUUId(payOrder.getServerId());
			String orderNum =UUID.randomUUID().toString().replace("-", "");
			payOrder.setCpOrderId(orderNum);
			payDao.updateByPrimaryKeySelective(payOrder);
		}
		return payOrder;
	}
	
	public PayOrder findPayOrder(String orderNum) {
		return payDao.selectByOrderNum(orderNum);
	}
	
	public int updateOrder(PayOrder payOrder) {
		return payDao.updateByCpNumSelective(payOrder);
	}
}
