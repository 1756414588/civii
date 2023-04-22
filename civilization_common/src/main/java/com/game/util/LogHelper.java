/**
 * @Title: LogHelper.java
 * @Package com.game.util
 * @Description: TODO
 * @author ZhangJun
 * @date 2015年8月21日 下午2:08:13
 * @version V1.0
 */
package com.game.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogHelper {
    public static Logger ERROR_LOGGER = LoggerFactory.getLogger("error");
    public static Logger MESSAGE_LOGGER = LoggerFactory.getLogger("profile");
    public static Logger GAME_DEBUG = LoggerFactory.getLogger("debug");
    public static Logger GAME_LOGGER = LoggerFactory.getLogger("game");
    public static Logger CONFIG_LOGGER = LoggerFactory.getLogger("ini_configure");
    public static Logger SAVE_LOGGER = LoggerFactory.getLogger("save");
    public static Logger PAY_LOGGER = LoggerFactory.getLogger("pay");
    public static Logger CHANNEL_LOGGER = LoggerFactory.getLogger("CHANNEL");
    public static Logger PACKET_LOGGER = LoggerFactory.getLogger("PACKET");
    public static Logger START_LOGGER = LoggerFactory.getLogger("START");
}
