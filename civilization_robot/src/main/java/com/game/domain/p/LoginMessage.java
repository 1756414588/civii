package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginMessage {

	private long keyId;
	private String name;
	private int requestCode;
	private int respondCode;
	private String desc;

}
