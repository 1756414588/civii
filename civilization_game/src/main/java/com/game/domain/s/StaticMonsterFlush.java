package com.game.domain.s;

import java.util.List;

// 世界野怪刷新配置
public class StaticMonsterFlush {
	private int commandLv; // 司令部等级
	private List<Integer> lowRange; // 初级区域的怪物刷新权重
	private List<Integer> middleRange; // 中级区域的怪物刷新权重
	private List<Integer> highRange; // 高级区域的怪物刷新权重

	public int getCommandLv() {
		return commandLv;
	}

	public void setCommandLv(int commandLv) {
		this.commandLv = commandLv;
	}


    public List<Integer> getMiddleRange () {
        return middleRange;
    }

    public void setMiddleRange (List<Integer> middleRange) {
        this.middleRange = middleRange;
    }

    public List<Integer> getHighRange () {
        return highRange;
    }

    public void setHighRange (List<Integer> highRange) {
        this.highRange = highRange;
    }

    public List<Integer> getLowRange () {
        return lowRange;
    }

    public void setLowRange (List<Integer> lowRange) {
        this.lowRange = lowRange;
    }
}
