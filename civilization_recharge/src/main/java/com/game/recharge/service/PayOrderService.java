package com.game.recharge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.recharge.manager.PayOrderManager;
import com.game.uc.PayOrder;

/**
 * 2020年5月12日
 * 
 *    halo_uc PayOrderService.java
 **/
@Service
public class PayOrderService {
	@Autowired
	private PayOrderManager orderManager;

	public int createOrder(PayOrder payOrder) {
		return orderManager.addPayOrder(payOrder);
	}

	public PayOrder findPayOrder(String orderNum) {
		return orderManager.selectPayOrder(orderNum);
	}

	public void updatePayOrder(PayOrder payOrder) {
		orderManager.updatePayOrder(payOrder);
	}
}
