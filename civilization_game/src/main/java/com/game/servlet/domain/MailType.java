package com.game.servlet.domain;
/**
*2020年4月27日
*
*halo_game
*MailType.java
**/

public class MailType {
	/**
	 * 普通邮件
	 */
	public static final int NOMAL= 1;
	
	/**
	 * 含物品邮件
	 */
	public static final int ITEMS= 2;
	
	/**
	 * 本服邮件
	 */
	public static final int NATIVE_SERVER= 3;
	
	/**
	 * 全服邮件
	 */
	public static final int ALL_SERVER= 4;

	/**
	 * 本服指定玩家邮件
	 */
	public static final int NATIVE_SERVER_PART= 5;
}
