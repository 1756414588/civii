package com.game.domain.s;

// 人口上限
public class StaticPeople {
    private int commandLv;
    private int base;
    private int limit;


    public int getCommandLv () {
        return commandLv;
    }

    public void setCommandLv (int commandLv) {
        this.commandLv = commandLv;
    }

    public int getBase () {
        return base;
    }

    public void setBase (int base) {
        this.base = base;
    }

    public int getLimit () {
        return limit;
    }

    public void setLimit (int limit) {
        this.limit = limit;
    }
}
