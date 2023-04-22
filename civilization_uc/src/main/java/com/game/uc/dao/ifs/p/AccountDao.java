package com.game.uc.dao.ifs.p;

import com.game.uc.Account;
import org.apache.ibatis.annotations.Param;


public interface AccountDao {
    Account selectByAccount(@Param("account") String account);

    Account selectByKey(@Param("keyId") int keyId);

    void insertWithAccount(Account account);


    void  updateAccount(Account account);
}
