package com.game.flame;

public interface NodeState {

	int NOT_OPEN = 1;// 不可被攻击
	int CENTER = 2;// 中立
	int ATTACK = 3;// 攻占
	int CAPTURE = 4;// 占领
}
