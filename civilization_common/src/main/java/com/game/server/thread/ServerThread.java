package com.game.server.thread;

import com.game.timer.ITimerEvent;

import java.util.concurrent.LinkedBlockingQueue;

import com.game.server.ICommand;
import com.game.util.LogHelper;

public class ServerThread extends Thread {

    // 命令执行队列
    private LinkedBlockingQueue<ICommand> command_queue = new LinkedBlockingQueue<ICommand>();
    // 计时线程
    private TimerThread timer;
    // 线程名称
    protected String threadName;
    // 心跳间隔
    protected int heart;

    // 运行标志
    private boolean stop;

    public boolean stopped = false;

    private boolean processingCompleted = false;

    public ServerThread(ThreadGroup group, String threadName, int heart) {
        super(group, threadName);
        this.threadName = threadName;
        this.heart = heart;
        if (this.heart > 0) {
            timer = new TimerThread(this);
        }
        this.setUncaughtExceptionHandler((e, f) -> {
            LogHelper.ERROR_LOGGER.error("ServerThread", e);
            if (timer != null) {
                timer.stop(true);
            }
            command_queue.clear();
        });
    }

    @Override
    public void run() {
        if (this.heart > 0 && timer != null) {
            // 启动计时线程
            timer.start();
        }

        stop = false;
        int loop = 0;
        long start = 0;
        long end = 0;
        long timeCost = 0;
        String className = "";
        String mainName = this.getName();
        while (!stop) {
            ICommand command = command_queue.poll();
            if (command == null) {
                try {
                    synchronized (this) {
                        loop = 0;
                        processingCompleted = true;
                        wait();
                    }
                } catch (Exception e) {
                    LogHelper.ERROR_LOGGER.error("ServerThread", e);
                }
            } else {
                try {
                    loop++;
                    processingCompleted = false;
                    start = System.currentTimeMillis();
                    command.action();
                    end = System.currentTimeMillis();

                    timeCost = end - start;
                    if (timeCost > 100) {
                        className = command.getClass().getSimpleName();
                        LogHelper.SAVE_LOGGER.error(mainName + "-->" + className + " haust:" + timeCost);
                    }

                    if (loop > 1000) {
                        loop = 0;
                        try {
                            Thread.sleep(1);
                        } catch (Exception e) {
							LogHelper.ERROR_LOGGER.error("ServerThread", e);
						}
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogHelper.ERROR_LOGGER.error(command.getClass().getSimpleName() + " exception -->" + e.getMessage(), e);
                }
            }
        }

        stopped = true;
    }

    public void stop(boolean flag) {
        stop = flag;
        if (timer != null) {
            this.timer.stop(flag);
        }
        this.command_queue.clear();
        try {
            synchronized (this) {
                if (processingCompleted) {
                    processingCompleted = false;
                    notify();
                }
            }
        } catch (Exception e) {
            LogHelper.ERROR_LOGGER.error("Server Thread " + threadName + " Notify Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 添加命令
     *
     * @param command 命令
     */
    public void addCommand(ICommand command) {
        try {
            this.command_queue.add(command);
            synchronized (this) {
                notify();
            }
        } catch (Exception e) {
            LogHelper.ERROR_LOGGER.error("Server Thread " + threadName + " Notify Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 添加定时事件
     *
     * @param event 定时事件
     */
    public void addTimerEvent(ITimerEvent event) {
        if (timer != null) {
            this.timer.addTimerEvent(event);
        }
    }

    /**
     * 移除定时事件
     *
     * @param event 定时事件
     */
    public void removeTimerEvent(ITimerEvent event) {
        if (timer != null) {
            this.timer.removeTimerEvent(event);
        }
    }

    public String getThreadName() {
        return threadName;
    }

    public int getHeart() {
        return heart;
    }
}
