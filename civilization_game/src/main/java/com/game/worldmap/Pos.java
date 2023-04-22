package com.game.worldmap;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;

public class Pos {

	private int x;
	private int y;

	public Pos() {
		x = -1;
		y = -1;
	}

	public Pos(int x, int y) {
		this.setX(x);
		this.setY(y);
	}

	public Pos(DataPb.PosData posData) {
		if (posData == null) {
			return;
		}
		this.setX(posData.getX());
		this.setY(posData.getY());
	}

	public void init(int x, int y) {
		this.setX(x);
		this.setY(y);
	}

	public void initPos(Pos param) {
		this.setX(param.getX());
		this.setY(param.getY());
	}

	public boolean isZero() {
		return x == 0 && y == 0;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public int hashCode() {
		return getX() ^ getY();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pos)) {
			return false;
		}
		Pos pairo = (Pos) o;
		return x == pairo.getX() && y == pairo.getY();
	}

	public void setPos(int x, int y) {
		setX(x);
		setY(y);
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String toString() {
		return "[x = " + x + ", y = " + y + "]";
	}

	public boolean isError() {
		return x == -1 && y == -1;
	}

	public CommonPb.Pos wrapPb() {
		CommonPb.Pos.Builder builder = CommonPb.Pos.newBuilder();
		builder.setX(x);
		builder.setY(y);
		return builder.build();
	}

	public void unwrapPb(CommonPb.Pos builder) {
		x = builder.getX();
		y = builder.getY();
	}

	public DataPb.PosData.Builder writeData() {
		DataPb.PosData.Builder builder = DataPb.PosData.newBuilder();
		builder.setX(x);
		builder.setY(y);
		return builder;
	}

	public void readData(DataPb.PosData builder) {
		x = builder.getX();
		y = builder.getY();
	}

	public static void swapPos(Pos pos1, Pos pos2) {
		int temp = pos1.getX();
		pos1.setX(pos2.getX());
		pos2.setX(temp);
		temp = pos1.getY();
		pos1.setY(pos2.getY());
		pos2.setY(temp);

	}

	public boolean isEqual(Pos pos) {
		if (pos == null) {
			return false;
		}

		return x == pos.getX() && y == pos.getY();
	}

	public Pos clone() {
		Pos pos = new Pos();
		pos.setX(x);
		pos.setY(y);
		return pos;
	}

	public String toPosStr() {
		return x + "," + y;
	}

	private int distance;

	public int getDistance() {
		return distance;
	}

	public void setDistance(Pos pos) {
		this.distance = Math.abs(pos.getX() - this.x) + Math.abs(pos.getY() - this.y);
	}
}
