package com.game.dataMgr;

import com.game.define.LoadData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.DepotType;
import com.game.dao.s.StaticDataDao;
import com.game.domain.p.Depot;
import com.game.domain.s.StaticDepot;
import com.game.domain.s.StaticExchange;
import com.game.domain.s.StaticRate;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.google.common.collect.HashBasedTable;

@Component
@LoadData(name = "仓库")
public class StaticDepotMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;
    private List<StaticDepot> depotList = new ArrayList<StaticDepot>();

    private List<StaticExchange> staticExchanges = new ArrayList<StaticExchange>();
    private HashBasedTable<Integer, Integer, Double> exChangeRate = HashBasedTable.create();

    @Override
    public void load() throws Exception {
        depotList.clear();
        exChangeRate.clear();
        List<StaticDepot> list = staticDataDao.selectStaticDepot();
        for (StaticDepot e : list) {

            // 金币
            List<List<Integer>> goldList = e.getGoldDepot();
            for (List<Integer> g : goldList) {
                e.setGoldRate(g.get(4) + e.getGoldRate());

                StaticRate staticRate = new StaticRate();
                staticRate.setParam(g.get(0));// 价格
                staticRate.setType(g.get(1));
                staticRate.setId(g.get(2));
                staticRate.setCount(g.get(3));
                staticRate.setRate(e.getGoldRate());

                e.getGoldRates().add(staticRate);
            }

            // 外围8个格子
            List<List<Integer>> tempList = e.getDepotList();
            for (List<Integer> g : tempList) {

                StaticRate staticRate = new StaticRate();
                staticRate.setParam(g.get(0));// 价格
                staticRate.setType(g.get(1));
                staticRate.setId(g.get(2));
                staticRate.setCount(g.get(3));
                staticRate.setRate(g.get(4));
                staticRate.setPos(g.get(5));

                if (staticRate.getPos() > 0) {
                    e.getRowRates().add(staticRate);
                } else {
                    e.getIronRates().add(staticRate);
                }
            }
            depotList.add(e);
        }
        staticExchanges = staticDataDao.selectStaticExchange();
        makeExchange();
    }

    @Override
    public void init() throws Exception{
    }

    public StaticDepot getDepotByLv(int level) {
        for (StaticDepot staticDepot : depotList) {
            if (level <= staticDepot.getLevel()) {
                return staticDepot;
            }
        }
        return null;
    }

    /**
     * 随机补给
     *
     * @param level
     * @return
     */
    public List<Depot> getRandomDeport(int level) {
        StaticDepot staticDepot = getDepotByLv(level);
        List<Depot> depots = new ArrayList<Depot>();

        // 非中间的固定列项
        List<Integer> rowpos = new ArrayList<Integer>();
        List<StaticRate> rowRates = staticDepot.getRowRates();
        for (StaticRate e : rowRates) {
            int grid = e.getPos() * 3 - 2 + RandomHelper.randomInSize(3);
            if (grid == 5) {// 容错处理
                if (!rowpos.contains(4)) {
                    grid = 4;
                } else if (!rowpos.contains(6)) {
                    grid = 6;
                } else {
                    continue;
                }
            }
            rowpos.add(grid);
            Depot depot = new Depot(grid, e.getParam(), 0, e.getType(), e.getId(), e.getCount());
            depot.setState(DepotType.OPEN_NO);
            depots.add(depot);
        }

        // 非中间的随机项
        List<StaticRate> ironRates = staticDepot.getIronRates();
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < ironRates.size(); i++) {
            list.add(i);
        }
        Collections.shuffle(list);

        int grid = 0;
        int index = 0;
        int count = 8 - rowpos.size();
        do {
            grid++;
            if (grid == 5 || rowpos.contains(grid)) {
                continue;
            }
            if (grid > 9) {
                break;
            }
            int ironId = list.get(index++);
            StaticRate staticRate = ironRates.get(ironId);
            Depot depot = new Depot(grid, staticRate.getParam(), 0, staticRate.getType(), staticRate.getId(), staticRate.getCount());
            depot.setState(DepotType.OPEN_NO);
            depots.add(depot);
        } while (count >= index);

        // 中间元宝购买格子
        int radom = RandomHelper.randomInSize(staticDepot.getGoldRate());
        for (StaticRate rate : staticDepot.getGoldRates()) {
            if (radom <= rate.getRate()) {
                Depot depot = new Depot(5, 0, rate.getParam(), rate.getType(), rate.getId(), rate.getCount());
                depot.setState(DepotType.OPEN_YES);
                depots.add(depot);
                break;
            }
        }
        return depots;
    }

    public void makeExchange() {
        for (StaticExchange staticExchange : staticExchanges) {
            exChangeRate.put(staticExchange.getResOutType(), staticExchange.getResInType(), (double) staticExchange.getPercent() / 100.0);
        }
    }

    public double getExchange(int typeOut, int typeIn) {
        Double value = exChangeRate.get(typeOut, typeIn);
        if (value == null) {
            LogHelper.CONFIG_LOGGER.error("exChange percent is 0 typeOunt:{} typeIn:{}", typeOut, typeIn);
            return 1.0;
        }

        return value;
    }

}
