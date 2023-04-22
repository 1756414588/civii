package com.game.domain;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @Description
 * @Date 2022/10/27 15:00
 **/

@Setter
@Getter
public class ChatShare {


	private long id;
	private long shareId;
	private int chatId;
	private String pos;
	private int posX;
	private int posY;
	private int param;
	private int type;
	private int country;
	private long shareTime;
	private long delayTime;
	private int ran;
	private int attend;

}
