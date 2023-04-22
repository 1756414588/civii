package com.game.domain.s;

/**
 * @author CaoBing
 * @date 2021/1/4 16:35
 */
public class StaticPayPoint {
    private int point_id; //计费点ID
    private int platNo;//1.安卓  2.苹果
    private int channel;//渠道
    private int productType;//商品类型(1.钻石 2.礼包 3.月卡 季卡 4.道具直购 5.通行证)
    private int money;//金额
    private String sdk_point;//sdk对应的计费点

    public int getPoint_id() {
        return point_id;
    }

    public void setPoint_id(int point_id) {
        this.point_id = point_id;
    }

    public int getPlatNo() {
        return platNo;
    }

    public void setPlatNo(int platNo) {
        this.platNo = platNo;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getProductType() {
        return productType;
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getSdk_point() {
        return sdk_point;
    }

    public void setSdk_point(String sdk_point) {
        this.sdk_point = sdk_point;
    }

    @Override
    public String toString() {
        return "StaticPayPoint{" +
               "point_id=" + point_id +
               ", platNo=" + platNo +
               ", channel=" + channel +
               ", productType=" + productType +
               ", money=" + money +
               ", sdk_point='" + sdk_point + '\'' +
               '}';
    }
}
