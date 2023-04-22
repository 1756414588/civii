package com.game.dataMgr;

import com.game.load.ILoadData;

public abstract class BaseDataMgr implements ILoadData {

	abstract public void init() throws Exception;
}
