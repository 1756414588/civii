package com.game.uc;

import java.util.Date;

public class PayOrder {
    // 订单状态 完成
    public static final int ORDER_SUCCESS = 1;
    // 订单状态 未完成
    public static final int ORDER_FALI = 0;
    // 订单状态 已完成未同步
    public static final int ORDER_SYN = 2;

    private Long keyId;

    private String cpOrderId;

    private String spOrderId;

    private Integer channelId;

    private Integer accountKey;

    private Integer platNo;

    private String platId;

    private Long roleId;

    private Integer serverId;

    private Integer status;

    private Integer productType;

    private Integer productId;

    private Integer payAmount;

    private Integer realAmount;

    private int pathway;

    private Date createTime;

    private Date finishTime;

    private int lv;    //付费等级

    private int realServer;

    private String nick;

    public PayOrder() {
        super();
    }

    /**
     * cp创建订单
     *
     * @param channelId
     * @param platNo
     * @param roleId
     * @param serverId
     * @param productType
     * @param productId
     * @param payAmount
     * @param platId
     * @param accountKey
     */
    public PayOrder(Integer channelId, Integer platNo, Long roleId, Integer serverId, Integer productType,
                    Integer productId, Integer payAmount, String platId, Integer accountKey, int lv) {
        super();
        this.channelId = channelId;
        this.platNo = platNo;
        this.roleId = roleId;
        this.serverId = serverId;
        this.productType = productType;
        this.productId = productId;
        this.payAmount = payAmount;
        this.accountKey = accountKey;
        this.platId = platId;
        this.createTime = new Date();
        this.lv = lv;
    }


    /**
     * 回调插入订单
     *
     * @param cpOrderId
     * @param spOrderId
     */
    public PayOrder(String cpOrderId, String spOrderId, Integer realAmount) {
        super();
        this.cpOrderId = cpOrderId;
        this.spOrderId = spOrderId;
        this.realAmount = realAmount;
        this.createTime = new Date();
    }

    /**
     * 重新赋值
     *
     * @param cpOrderId
     * @param spOrderId
     * @param realAmount
     */
    public void resetData(String cpOrderId, String spOrderId, Integer realAmount) {
        this.cpOrderId = cpOrderId;
        this.spOrderId = spOrderId;
        this.realAmount = realAmount;
        this.createTime = new Date();
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public String getCpOrderId() {
        return cpOrderId;
    }

    public void setCpOrderId(String cpOrderId) {
        this.cpOrderId = cpOrderId;
    }

    public String getSpOrderId() {
        return spOrderId;
    }

    public void setSpOrderId(String spOrderId) {
        this.spOrderId = spOrderId;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Integer getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(Integer accountKey) {
        this.accountKey = accountKey;
    }

    public Integer getPlatNo() {
        return platNo;
    }

    public void setPlatNo(Integer platNo) {
        this.platNo = platNo;
    }

    public String getPlatId() {
        return platId;
    }

    public void setPlatId(String platId) {
        this.platId = platId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getProductType() {
        return productType;
    }

    public void setProductType(Integer productType) {
        this.productType = productType;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(Integer payAmount) {
        this.payAmount = payAmount;
    }

    public Integer getRealAmount() {
        return realAmount;
    }

    public void setRealAmount(Integer realAmount) {
        this.realAmount = realAmount;
    }

    public Integer getPathway() {
        return pathway;
    }

    public void setPathway(Integer pathway) {
        this.pathway = pathway;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getRealServer() {
        return realServer;
    }

    public void setRealServer(int realServer) {
        this.realServer = realServer;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    @Override
    public String toString() {
        return "PayOrder [keyId=" + keyId + ", cpOrderId=" + cpOrderId + ", spOrderId=" + spOrderId + ", channelId="
                + channelId + ", accountKey=" + accountKey + ", platNo=" + platNo + ", platId=" + platId + ", roleId="
                + roleId + ", serverId=" + serverId + ", status=" + status + ", productType=" + productType
                + ", productId=" + productId + ", payAmount=" + payAmount + ", realAmount=" + realAmount + ", pathway="
                + pathway + ", createTime=" + createTime + ", finishTime=" + finishTime + ", lv=" + lv + ", realServer=" + realServer + "]";
    }
}