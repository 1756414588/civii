package com.game.servlet.domain;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * 全服广播的实体类
 * 
 * @author CaoBing 2020年6月29日 ServerRadio.java
 *
 */
public class ServerRadio {
	public static final int UN_SEND = 0;

	public static final int SEND_ING = 1;
	
	public static final int HAVE_SEND = 2;

	public static final int UN_REMOVE = 0;

	public static final int HAVE_REMOVE = 1;
	
	private Long keyId;

	private Integer language;

	private String message;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startTime;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date endTime;

	private String channel;

	private int status;

	private Integer frequency;

	private int remove;

	private Date createTime;

	private Date updateTime;

	private Date deleteTime;
	
	private long lastSendTime;

	/**
	 * 临时数据
	 */
	private List<Integer> channelList;

	public List<Integer> getChannelList() {
		return channelList;
	}

	public void setChannelList(List<Integer> channelList) {
		this.channelList = channelList;
	}

	public Long getKeyId() {
		return keyId;
	}

	public void setKeyId(Long keyId) {
		this.keyId = keyId;
	}

	public Integer getLanguage() {
		return language;
	}

	public void setLanguage(Integer language) {
		this.language = language;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Integer getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	public int getRemove() {
		return remove;
	}

	public void setRemove(int remove) {
		this.remove = remove;
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

	public Date getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}
	
	public long getLastSendTime() {
		return lastSendTime;
	}

	public void setLastSendTime(long lastSendTime) {
		this.lastSendTime = lastSendTime;
	}

	@Override
	public String toString() {
		return "ServerRadio [keyId=" + keyId + ", language=" + language + ", message=" + message + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", channel=" + channel + ", status=" + status + ", frequency=" + frequency + ", remove=" + remove + ", createTime=" + createTime
				+ ", updateTime=" + updateTime + ", deleteTime=" + deleteTime + ", lastSendTime=" + lastSendTime + "]";
	}
}