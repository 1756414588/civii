package com.game.manager;

import com.game.domain.Robot;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/9/27 14:23
 **/

@Component
public class GuildeManager {


	public boolean isComplate(Robot robot) {
		if (robot.getGuideKey() >= 37700) {
			return true;
		}
		return false;
	}

}
