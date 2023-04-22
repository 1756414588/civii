package com.game.domain.s;

/**
 * @author cpz
 * @date 2020/9/24 10:31
 * @description 虫族入侵 积分商店
 */
public class StaticActRoitScoreShop {
    private int keyId;//	INT	索引号
    private String icon;//	VARCHAR	道具图标
    private int score;//INT	售卖价格，需要积分的数量
    private int propId;//INT	道具索引号

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getPropId() {
        return propId;
    }

    public void setPropId(int propId) {
        this.propId = propId;
    }
}
