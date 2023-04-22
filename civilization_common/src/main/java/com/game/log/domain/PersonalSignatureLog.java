package com.game.log.domain;
/**
 * @Description 个性签名数据埋点
 * @Date 2021/1/27 15:06
 **/
public class PersonalSignatureLog {
    //id
    private Integer accountKeyId;
    //服务器id
    private Integer serverId;
    //角色id
    private Long roleId;
    //角色等级
    private Integer lv;
    //签名修改内容
    private String personalSignature;
    //角色等级
    private Integer countryId;
    //vip等级
    private Integer vip;
    //渠道
    private Integer channel;

    public PersonalSignatureLog() {
    }

    public PersonalSignatureLog(Integer accountKeyId, Integer serverId, Long roleId, Integer lv, String personalSignature, Integer countryId, Integer vip, Integer channel) {
        this.accountKeyId = accountKeyId;
        this.serverId = serverId;
        this.roleId = roleId;
        this.lv = lv;
        this.personalSignature = personalSignature;
        this.countryId = countryId;
        this.vip = vip;
        this.channel = channel;
    }

    public Integer getAccountKeyId() {
        return accountKeyId;
    }

    public void setAccountKeyId(Integer accountKeyId) {
        this.accountKeyId = accountKeyId;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Integer getLv() {
        return lv;
    }

    public void setLv(Integer lv) {
        this.lv = lv;
    }

    public String getPersonalSignature() {
        return personalSignature;
    }

    public void setPersonalSignature(String personalSignature) {
        this.personalSignature = personalSignature;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Integer getVip() {
        return vip;
    }

    public void setVip(Integer vip) {
        this.vip = vip;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(accountKeyId).append(",");
        builder.append(serverId).append(",");
        builder.append(roleId).append(",");
        builder.append(lv).append(",");
        builder.append(personalSignature).append(",");
        builder.append(countryId).append(",");
        builder.append(vip).append(",");
        builder.append(channel);
        return builder.toString();
    }

}
