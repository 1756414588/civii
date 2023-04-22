package com.game.util;


public class PrintHelper {

	public static void printByte(byte[] msg) {
		StringBuffer m = new StringBuffer();
		m.append("[");
		for (byte b : msg) {
			m.append(b).append(" ");
		}
		m.append("]");
		LogHelper.MESSAGE_LOGGER.info(m.toString());
	}

}
