package com.game.pay;

/**
 *
 * @date 2021/6/16 18:09
 */
public class BaseOrderConfig {
    /**
     * 玩家在快游互动平台的唯一标识
     */
    private int userid;
    private String body; // string 订单说明
    private Integer price; // int 商品价格，单位（分）1元=100
    private String subject; // string 商品名称
    private Integer appId; // int 游戏id（每个游戏的appid唯一）
    private String trade_sn; // string 新快订单号(订单号唯一)
    private String order_id; // string 游戏方订单号(要求唯一)
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime; // date 订单创建时间（Y-m-d H:i:s） 例如：2020-08-12 14:30:40
    private String status; // string "succ"表示订单支付成功
    private String serverid; // string 区服ID
    private String roleid; // string 角色id
    private String extradata; // string 额外字段（自定义字段，对应发行SDK的extension字段）
    private String good_id; // string 商品id
    private String sign; // string 快游互动发行平台的加密串
    private Integer sandbox; // int 1代表是ios沙盒数据，默认0
    //商品价格，单位（美元），保留2位小数
    private Float fee;

    //项目id（每个项目的appid唯一）
    private Integer appid;


    //游戏id（每个游戏包的game_id唯一,客户端的game_id）
    private Integer game_id;

    //游戏方订单号(要求唯一)
    private String orderid;


    //游戏区服
    private String servername;


    //用户名
    private String username;


    private String orderId;

    public BaseOrderConfig(){

    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getTrade_sn() {
        return trade_sn;
    }

    public void setTrade_sn(String trade_sn) {
        this.trade_sn = trade_sn;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServerid() {
        return serverid;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public String getRoleid() {
        return roleid;
    }

    public void setRoleid(String roleid) {
        this.roleid = roleid;
    }

    public String getExtradata() {
        return extradata;
    }

    public void setExtradata(String extradata) {
        this.extradata = extradata;
    }

    public String getGood_id() {
        return good_id;
    }

    public void setGood_id(String good_id) {
        this.good_id = good_id;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Integer getSandbox() {
        return sandbox;
    }

    public void setSandbox(Integer sandbox) {
        this.sandbox = sandbox;
    }

    public Float getFee() {
        return fee;
    }

    public void setFee(Float fee) {
        this.fee = fee;
    }

    public Integer getAppid() {
        return appid;
    }

    public void setAppid(Integer appid) {
        this.appid = appid;
    }

    public Integer getGame_id() {
        return game_id;
    }

    public void setGame_id(Integer game_id) {
        this.game_id = game_id;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getServername() {
        return servername;
    }

    public void setServername(String servername) {
        this.servername = servername;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
