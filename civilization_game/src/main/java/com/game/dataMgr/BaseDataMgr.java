package com.game.dataMgr;

import javax.annotation.PostConstruct;

public abstract class BaseDataMgr {
	@PostConstruct
	abstract public void init() throws Exception;
}
