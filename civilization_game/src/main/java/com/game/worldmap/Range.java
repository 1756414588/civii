package com.game.worldmap;

import java.util.List;

public class Range {
	private Integer beg;
	private Integer end;

	public Range() {

	}

	public Range(Integer beg, Integer end) {
		this.setBeg(beg);
		this.setEnd(end);
	}

	public Integer getBeg() {
		return beg;
	}

	public Integer getEnd() {
		return end;
	}

	@Override
	public int hashCode() {
		return getBeg().hashCode() ^ getEnd().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Range))
			return false;
		Range pairo = (Range) o;
		return this.getBeg().equals(pairo.getBeg()) && this.getEnd().equals(pairo.getEnd());
	}

	public void setRange(Integer beg, Integer end) {
		setBeg(beg);
		setEnd(end);
	}

	public void setBeg(Integer beg) {
		this.beg = beg;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}

	public String toString() {
		return "Begin = " + beg + ", End = " + end;
	}

	public boolean isError() {
		return beg != -1 && end != -1;
	}

	public static Range create(List<Integer> pos) {
        Range range = new Range(pos.get(0), pos.get(1));
        return range;
    }

    public boolean isOk() {
	    return beg != 0 && end != 0;
    }

}
