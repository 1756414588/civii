package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RobotMessage {

	private long keyId;
	private int type;
	private String name;
	private int requestCode;
	private int respondCode;
	private String param;
	private byte[] content;
	private int parentId;
	private int remainTime;
	private long createTime;

}
