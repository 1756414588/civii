package com.game.domain.p;

import com.game.util.LogHelper;

public class ConfigException extends Exception   {
	private static final long serialVersionUID = 1L;

	public ConfigException(String msg) {
        super(msg);
        LogHelper.CONFIG_LOGGER.error(msg);
    }
}
