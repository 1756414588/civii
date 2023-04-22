package com.game.domain.s;

import java.util.List;

// 简单配置表
public class StaticSimpleConfig {
	private int id;
	private String name;
	private int configNum;
	private List<Integer> addition;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getConfigNum() {
		return configNum;
	}

	public void setConfigNum(int configNum) {
		this.configNum = configNum;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getAddition() {
        return addition;
    }

    public void setAddition(List<Integer> addition) {
        this.addition = addition;
    }
}
