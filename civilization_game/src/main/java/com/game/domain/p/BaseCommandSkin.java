package com.game.domain.p;

/**
 * @author CaoBing
 * @date 2021/1/28 11:46
 * 主城皮肤
 */
public class BaseCommandSkin {
    private int key;

    private int status;

    private long endTime;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "BaseCommandSkin{" +
               "key=" + key +
               ", status=" + status +
               ", endTime=" + endTime +
               '}';
    }
}
