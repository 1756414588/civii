package com.game.timer;

public abstract class TimerEvent implements ITimerEvent {
	// 定时结束时间
	private long end;
	// 定时剩余时间
	private long remain;
	// 执行次数
	private int loop;
	// 间隔时间
	private long interval;

	/**
	 * 计时事件
	 * 
	 * @param end
	 *            执行事件
	 */
	protected TimerEvent(long end) {
		this.end = end;
		this.loop = 1;
	}

	/**
	 * 循环事件
	 * 
	 * @param loop
	 *            循环次数
	 * @param interval
	 *            间隔时间
	 */
	//loop = -1 表示无限loop
    //interval 表示loop的时间间隔
	protected TimerEvent(int loop, long interval) {
		this.loop = loop;
		this.interval = interval;
		this.end = System.currentTimeMillis() + interval;
	}

	@Override
	public long remain() {
		return this.end - System.currentTimeMillis();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getRemain() {
		return remain;
	}

	public void setRemain(long remain) {
		this.remain = remain;
	}

	public int getLoop() {
		return loop;
	}

	public void setLoop(int loop) {
		this.loop = loop;
		this.end = System.currentTimeMillis() + interval;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

}
