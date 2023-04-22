package com.game.util;

import com.game.domain.p.Account;



public class AccountHelper {
	public static boolean isForbid(Account account) {
		return account.getForbid() == 1;
	}
}
