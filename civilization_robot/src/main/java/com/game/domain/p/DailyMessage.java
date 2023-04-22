package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @Description
 * @Date 2022/10/21 10:22
 **/

@Getter
@Setter
public class DailyMessage {

	private long keyId;
	private int type;
	private String name;
	private int requestCode;
	private int respondCode;
	private String param;
	private byte[] content;
	private long parentId;
	private int remainTime;
	private long createTime;
	private int diffHour;

}
