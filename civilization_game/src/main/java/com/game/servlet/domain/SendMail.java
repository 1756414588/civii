package com.game.servlet.domain;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.game.domain.Award;

/**
 * 2020年4月27日
 * 
 * @CaoBing halo_common SendMail.java
 **/
public class SendMail {
	public static final int UN_SEND = 0;

	public static final int HAVE_SEND = 1;

	public static final int UN_REMOVE = 0;

	public static final int HAVE_REMOVE = 1;

	// 物品
	private List<Award> awards;

	private Long keyId;

	private Long roleId;

	private int mailId;

	// 邮件类型 1.邮件 2.含物品邮件 3.本服邮件 4.全服邮件
	private Integer type;

	private String title;

	private String titleContent;

	private String content;

	private String remark;

	private String awardList;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startTime;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date endTime;

	private Integer vipType;

	/**
	 * 1.最低等级 (表示VIP需要大于多少)
	 * 
	 * 2.区间 (VIP请用-隔开 如2-3表示只有)
	 * 
	 * 3.间隔(用逗号隔开)
	 **/
	private String vip;

	// 0.未推送 1.已推送
	private int status;

	private Date createTime;

	// 修改时间
	private Date updateTime;

	// 是否删除(是否删除(0.正常 1.已删除))
	private int remove;

	private Date deleteTime;

	private List<Integer> channel;

	private String channelList;

	public List<Award> getAwards() {
		return awards;
	}

	public void setAwards(List<Award> awards) {
		this.awards = awards;
	}

	public Long getKeyId() {
		return keyId;
	}

	public void setKeyId(Long keyId) {
		this.keyId = keyId;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Integer getMailId() {
		return mailId;
	}

	public void setMailId(Integer mailId) {
		this.mailId = mailId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAwardList() {
		return awardList;
	}

	public void setAwardList(String awardList) {
		this.awardList = awardList;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Integer getVipType() {
		return vipType;
	}

	public void setVipType(Integer vipType) {
		this.vipType = vipType;
	}

	public String getVip() {
		return vip;
	}

	public void setVip(String vip) {
		this.vip = vip;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getRemove() {
		return remove;
	}

	public void setRemove(Integer remove) {
		this.remove = remove;
	}

	public Date getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}

	public String getTitleContent() {
		return titleContent;
	}

	public void setTitleContent(String titleContent) {
		this.titleContent = titleContent;
	}

	public List<Integer> getChannel() {
		return channel;
	}

	public void setChannel(List<Integer> channel) {
		this.channel = channel;
	}

	public String getChannelList() {
		return channelList;
	}

	public void setChannelList(String channelList) {
		this.channelList = channelList;
	}

	@Override
	public String toString() {
		return "SendMail [awards=" + awards + ", keyId=" + keyId + ", roleId=" + roleId + ", mailId=" + mailId + ", type=" + type + ", title=" + title
				+ ", content=" + content + ", remark=" + remark + ", awardList=" + awardList + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", vipType=" + vipType + ", vip=" + vip + ", status=" + status + ", createTime=" + createTime + ", updateTime=" + updateTime + ", remove="
				+ remove + ", deleteTime=" + deleteTime + "]";
	}
}