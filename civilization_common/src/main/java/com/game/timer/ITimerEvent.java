package com.game.timer;

import com.game.server.ICommand;

public interface ITimerEvent extends ICommand{

	long remain();
}
