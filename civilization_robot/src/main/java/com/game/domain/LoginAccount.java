package com.game.domain;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginAccount {

	private String account;
	private int keyId;
	private String token;
	private int serverId;
}
