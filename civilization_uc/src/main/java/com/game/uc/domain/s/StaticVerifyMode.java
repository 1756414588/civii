package com.game.uc.domain.s;

public class StaticVerifyMode {

    private int verifyMode;

    public int getVerifyMode() {
        return verifyMode;
    }

    public void setVerifyMode(int verifyMode) {
        this.verifyMode = verifyMode;
    }

    @Override
    public String toString() {
        return "verifyMode:"+verifyMode;
    }
}
