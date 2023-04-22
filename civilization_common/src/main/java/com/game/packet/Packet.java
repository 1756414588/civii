package com.game.packet;

/**
 *
 * @Description 服务器的通信包
 * @Date 2022/9/9 11:30
 **/

public class Packet implements Cloneable {

	int req;
	int cmd;
	int seq;
	byte callBack;
	long channelId;
	long roleId;
	short error;
	byte[] bytes;
	int length;

	public int getReq() {
		return req;
	}

	public void setReq(int req) {
		this.req = req;
	}

	public void resetSeq() {
		setSeq(0);
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public int getSeq() {
		return this.seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public long getChannelId() {
		return channelId;
	}

	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}

	public int getCmd() {
		return cmd;
	}

	public void setCmd(int cmd) {
		this.cmd = cmd;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public short getError() {
		return this.error;
	}

	public void setError(Short error) {
		this.error = error;
	}

	public void setLength(int length) {
		this.length = length;
	}


	public void setError(short error) {
		this.error = error;
	}

	public byte getCallBack() {
		return callBack;
	}

	public void setCallBack(byte callBack) {
		this.callBack = callBack;
	}
}
