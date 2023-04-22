package com.game.servlet.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendTargetMail {

	private int mailId;
	private String title;
	private String titleContent;
	private String content;
	private String playerList;
	private String awards;

}