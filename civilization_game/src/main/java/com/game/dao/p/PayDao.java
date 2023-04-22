package com.game.dao.p;

import org.apache.ibatis.annotations.Param;

import com.game.domain.p.Pay;
import com.game.domain.p.PayBack;

public interface PayDao {

	public Pay selectPay(@Param("platNo") int platNo, @Param("orderId") String orderId);

	public Pay selectRolePay(@Param("serverId") int serverId, @Param("roleId") long roleId);

	public void createPay(Pay pay);

	public PayBack selectPayBack(@Param("platNo") int platNo, @Param("platId") String platId);

	public void updatePayBack(PayBack payBack);
}
