package com.game.recharge.manager;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.recharge.dao.ifs.p.PayOrderDao;
import com.game.uc.PayOrder;

/**
 * 2020年5月12日
 * 
 * @CaoBing halo_uc PayOrderManager.java
 **/
@Component
public class PayOrderManager {
	@Autowired
	private PayOrderDao payOrderDao;
	
	public int addPayOrder(PayOrder payOrder) {
		payOrder.setCreateTime(new Date());
		return payOrderDao.insertSelective(payOrder);
	}
	
	
	public PayOrder selectPayOrder(String orderNum) {
		return payOrderDao.selectByOrderNum(orderNum);
	}
	
	public void updatePayOrder(PayOrder payOrder) {
		payOrderDao.updateByPrimaryKeySelective(payOrder);
	}
}
