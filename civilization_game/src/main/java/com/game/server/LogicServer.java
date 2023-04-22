package com.game.server;

import com.game.dataMgr.StaticLimitMgr;
import com.game.flame.ActFlameWarTimer;
import com.game.message.handler.DealType;
import com.game.server.thread.ServerThread;
import com.game.timer.ActivityCloseTimer;
import com.game.timer.ActManoeuvreTimer;
import com.game.timer.ActivityTimer;
import com.game.timer.ActivityTipTimer;
import com.game.timer.AutoTimer;
import com.game.timer.BattleTimer;
import com.game.timer.BigMonsterTimer;
import com.game.timer.BuildingUpTimer;
import com.game.timer.CheckArmyTimer;
import com.game.timer.CheckMailTimer;
import com.game.timer.CitySmallGameTimer;
import com.game.timer.CityTimer;
import com.game.timer.FishingQueueTimer;
import com.game.timer.FlushFortressTimer;
import com.game.timer.FrameTimer;
import com.game.timer.MonsterFlushTimer;
import com.game.timer.OfflineTimer;
import com.game.timer.RankTimer;
import com.game.timer.ResouceFlushTimer;
import com.game.timer.RestoreTimer;
import com.game.timer.RoitTimer;
import com.game.timer.SaveActivityTimer;
import com.game.timer.SaveCityTimer;
import com.game.timer.SaveCountryTimer;
import com.game.timer.SavePlayerTimer;
import com.game.timer.SaveServerMailTimer;
import com.game.timer.SaveServerRaioTimer;
import com.game.timer.SaveWorldMapTimer;
import com.game.timer.SaveWorldTimer;
import com.game.timer.ServerMailTimer;
import com.game.timer.ServerRadioTimer;
import com.game.timer.SquareMonsterTimer;
import com.game.timer.SynGloryRankTimer;
import com.game.timer.WarTimer;
import com.game.timer.WashTimeTimer;
import com.game.timer.WorkShopTimer;
import com.game.timer.WorldBoxTimer;
import com.game.timer.WorldTimer;
import com.game.timer.ZergTimer;
import com.game.timer.TimerEvent;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author 陈奎
 * @Description 逻辑服务
 * @Date 2022/9/9 11:30
 **/

public class LogicServer implements Runnable {

	static Logger logger = LoggerFactory.getLogger(LogicServer.class);

	private int heart;
	protected HashMap<Integer, ServerThread> threadPool = new HashMap<Integer, ServerThread>();
	private ThreadGroup threadGroup;

	ThreadFactory threadFactory = null;
	ExecutorService executor = null;

	public ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

	public LogicServer(String serverName, int heart, int poolSize) {
		this.heart = heart;
		threadFactory = new ThreadFactoryBuilder().setNameFormat("thread-pool-%d").build();
		executor = new ThreadPoolExecutor(1, 1,
			1L, TimeUnit.MINUTES,
			new LinkedBlockingQueue<Runnable>(4096), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());

		threadGroup = new ThreadGroup(serverName);
		createServerThread(DealType.MAIN);
		createServerThread(DealType.SAVE_DATA);
		createServerThread(DealType.TIMER_LOGIC);
	}

	private void createServerThread(DealType dealType) {
		ServerThread serverThread = new ServerThread(threadGroup, dealType.getName(), heart);
		threadPool.put(dealType.getCode(), serverThread);
	}

	public void stop() {
		Iterator<ServerThread> it = threadPool.values().iterator();
		while (it.hasNext()) {
			it.next().stop(true);
		}
		executor.shutdown();
	}

	public boolean isStopped() {
		Iterator<ServerThread> it = threadPool.values().iterator();
		while (it.hasNext()) {
			if (!it.next().stopped) {
				return false;
			}
		}
		return true && executor.isTerminated();
	}

	@Override
	public void run() {
		Iterator<ServerThread> it = threadPool.values().iterator();
		while (it.hasNext()) {
			it.next().start();
		}

		//存储线程和计算线程分开
		threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveActivityTimer());
		threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveCityTimer());
		threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveCountryTimer());
		threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SavePlayerTimer());
		threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveServerMailTimer());
		threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveServerRaioTimer());
		threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveWorldMapTimer());
		threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveWorldTimer());

		// 业务逻辑线程
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new BuildingUpTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new RestoreTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WashTimeTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new RankTimer());

		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WorkShopTimer());
		//threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new PrimaryCollectTimer());
		//定时刷新虫族
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new MonsterFlushTimer(SpringUtil.getBean(StaticLimitMgr.class).getNum(7) * 1000L));
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ResouceFlushTimer());
//		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WarTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new CityTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new AutoTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new SquareMonsterTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new FlushFortressTimer(SpringUtil.getBean(StaticLimitMgr.class).getNum(297) * 1000L));
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new SynGloryRankTimer());
		//threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new FlushActMonsterTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ActivityTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WorldPvpTimer());
		//threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new CountryHeroCheckTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new RoitTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ServerMailTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ServerRadioTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new CitySmallGameTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WorldBoxTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new FrameTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new BigMonsterTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WorldTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new CheckMailTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new OfflineTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ZergTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ActManoeuvreTimer());
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ActFlameWarTimer());

		threadPool.get(DealType.TIMER_LOGIC.getCode()).addTimerEvent(new ActivityCloseTimer());
		threadPool.get(DealType.TIMER_LOGIC.getCode()).addTimerEvent(new ActivityTipTimer());
		threadPool.get(DealType.TIMER_LOGIC.getCode()).addTimerEvent(new BattleTimer());
		threadPool.get(DealType.TIMER_LOGIC.getCode()).addTimerEvent(new CheckArmyTimer());

		//渔场派遣队列定时器
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new FishingQueueTimer());
	}


	public void addTimer(TimerEvent event) {
		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(event);
	}

	public void removeTimer(TimerEvent event) {
		threadPool.get(DealType.MAIN.getCode()).removeTimerEvent(event);
	}

	public void addCommand(Runnable runnable) {
		executor.execute(runnable);
	}

	public void addCommand(ICommand command, DealType dealType) {
		ServerThread thread = threadPool.get(dealType.getCode());
		if (thread != null) {
			thread.addCommand(command);
		} else {
			command.action();
		}
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		LogicServer.logger = logger;
	}

	public void shutDownGraceful() {
		try {
			stop();
			while (!isStopped()) {
				Thread.sleep(1);
				stop();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("LogicServer stop:{}", e.getMessage(), e);
		}
	}
}
