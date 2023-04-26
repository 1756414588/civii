package com.game.server;

import com.game.flame.ActFlameWarTimer;
import com.game.manager.ServerManager;
import com.game.message.handler.DealType;
import com.game.server.thread.ServerThread;
import com.game.timer.TimerEvent;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.*;

@Component
public class LogicServer extends AbstractIdleService {

    private int heart;
    protected HashMap<Integer, ServerThread> threadPool = new HashMap<Integer, ServerThread>();
    private ThreadGroup threadGroup;

    ThreadFactory threadFactory = null;
    ExecutorService executor = null;

    public ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    @Autowired
    ServerManager serverManager;

    @Autowired
    ActFlameWarTimer actFlameWarTimer;

    private void createServerThread(DealType dealType) {
        ServerThread serverThread = new ServerThread(threadGroup, dealType.getName(), heart);
        threadPool.put(dealType.getCode(), serverThread);
    }

//    public void stop() {
//
//    }

    public boolean isStopped() {
        return true && executor.isTerminated();
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

//    public void shutDownGraceful() {
//        try {
//            stop();
//            while (!isStopped()) {
//                Thread.sleep(1);
//                stop();
//            }
//        } catch (Exception e) {
//            LogHelper.ERROR_LOGGER.error("LogicServer stop:{}", e.getMessage(), e);
//        }
//    }

    @Override
    protected void startUp() throws Exception {
        this.heart = 500;
        threadFactory = new ThreadFactoryBuilder().setNameFormat("thread-pool-%d").build();
        executor = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(4096), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
        //threadGroup = new ThreadGroup(serverManager.getServer().getServerName());
        createServerThread(DealType.MAIN);
        createServerThread(DealType.SAVE_DATA);

        Iterator<ServerThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            it.next().start();
        }

        //存储线程和计算线程分开
//        threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveActivityTimer());
//        threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveCityTimer());
//        threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveCountryTimer());
//        threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SavePlayerTimer());
//        threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveServerMailTimer());
//        threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveServerRaioTimer());
//        threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveWorldMapTimer());
//        threadPool.get(DealType.SAVE_DATA.getCode()).addTimerEvent(new SaveWorldTimer());
//
//        // 业务逻辑线程
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new BuildingUpTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new RestoreTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WashTimeTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new RankTimer());
//
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WorkShopTimer());
//        //threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new PrimaryCollectTimer());
//        //定时刷新虫族
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new MonsterFlushTimer(SpringUtil.getBean(StaticLimitMgr.class).getNum(7) * 1000L));
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ResouceFlushTimer());
////		threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WarTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new CityTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new AutoTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new SquareMonsterTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new FlushFortressTimer(SpringUtil.getBean(StaticLimitMgr.class).getNum(297) * 1000L));
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new SynGloryRankTimer());
//        //threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new FlushActMonsterTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ActivityTimer());
////        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WorldPvpTimer());
//        //threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new CountryHeroCheckTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new RoitTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ServerMailTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ServerRadioTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new CitySmallGameTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WorldBoxTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new FrameTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new BigMonsterTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WorldTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new CheckMailTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new OfflineTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ZergTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ActManoeuvreTimer());
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(actFlameWarTimer);
//
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ActivityCloseTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ActivityTipTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new BattleTimer());
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new CheckArmyTimer());
//
//        //渔场派遣队列定时器
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new FishingQueueTimer());
    }

    @Override
    protected void shutDown() throws Exception {
        Iterator<ServerThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            it.next().stop(true);
        }
        executor.shutdown();
    }
}
