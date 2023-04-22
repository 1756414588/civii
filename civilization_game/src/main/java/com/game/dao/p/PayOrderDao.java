package com.game.dao.p;

import com.game.uc.PayOrder;

public interface PayOrderDao {
    int deleteByPrimaryKey(Long keyId);

    int insert(PayOrder record);

    int insertSelective(PayOrder record);

    PayOrder selectByPrimaryKey(Long keyId);

    int updateByPrimaryKeySelective(PayOrder record);
    
    int updateByCpNumSelective(PayOrder record);

    int updateByPrimaryKey(PayOrder record);
    
    PayOrder selectByOrderNum(String orderNum);
}