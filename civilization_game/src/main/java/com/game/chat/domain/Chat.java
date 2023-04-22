package com.game.chat.domain;

import com.game.pb.CommonPb;

public abstract class Chat {

	protected int style;

	protected int chatType;

	abstract public CommonPb.Chat ser(int style, int officerId);


}
