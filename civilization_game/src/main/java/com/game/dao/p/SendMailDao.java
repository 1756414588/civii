package com.game.dao.p;

import java.util.List;

import com.game.servlet.domain.SendMail;

public interface SendMailDao {
    int deleteByPrimaryKey(Long keyid);

    int insert(SendMail record);

    int insertSelective(SendMail record);

    SendMail selectByPrimaryKey(Long keyid);

    int updateByPrimaryKeySelective(SendMail record);

    int updateByPrimaryKey(SendMail record);

	List<SendMail> selectAllServerMail();
}