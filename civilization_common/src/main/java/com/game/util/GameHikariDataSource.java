package com.game.util;

import com.zaxxer.hikari.HikariDataSource;

/**
 *
 * @date 2021/8/11 15:56
 */
public class GameHikariDataSource extends HikariDataSource {

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public void setUsername(String username) {
        String realUserName = DESUtil.decrypt(username);
        super.setUsername(realUserName);
    }

    @Override
    public void setPassword(String password) {
        String realPwd = DESUtil.decrypt(password);
        super.setPassword(realPwd);
    }
}
