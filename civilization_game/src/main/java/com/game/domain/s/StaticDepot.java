package com.game.domain.s;

import java.util.ArrayList;
import java.util.List;

public class StaticDepot {

	private int level;
	private int depotLength;
	private List<List<Integer>> goldDepot;
	private List<List<Integer>> depotList;

	private int goldRate;
	private List<StaticRate> goldRates = new ArrayList<StaticRate>();

	private List<StaticRate> ironRates = new ArrayList<StaticRate>();
	private List<StaticRate> rowRates = new ArrayList<StaticRate>();

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getDepotLength() {
		return depotLength;
	}

	public void setDepotLength(int depotLength) {
		this.depotLength = depotLength;
	}

	public List<List<Integer>> getDepotList() {
		return depotList;
	}

	public void setDepotList(List<List<Integer>> depotList) {
		this.depotList = depotList;
	}

	public List<List<Integer>> getGoldDepot() {
		return goldDepot;
	}

	public void setGoldDepot(List<List<Integer>> goldDepot) {
		this.goldDepot = goldDepot;
	}

	public int getGoldRate() {
		return goldRate;
	}

	public void setGoldRate(int goldRate) {
		this.goldRate = goldRate;
	}

	public List<StaticRate> getGoldRates() {
		return goldRates;
	}

	public void setGoldRates(List<StaticRate> goldRates) {
		this.goldRates = goldRates;
	}

	public List<StaticRate> getIronRates() {
		return ironRates;
	}

	public void setIronRates(List<StaticRate> ironRates) {
		this.ironRates = ironRates;
	}

	public List<StaticRate> getRowRates() {
		return rowRates;
	}

	public void setRowRates(List<StaticRate> rowRates) {
		this.rowRates = rowRates;
	}

}
