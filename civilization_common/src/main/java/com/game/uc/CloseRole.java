package com.game.uc;

import java.util.Date;

public class CloseRole extends CloseRoleKey {
    private Integer accountKey;

    private Date endtime;

    public Integer getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(Integer accountKey) {
        this.accountKey = accountKey;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }


}