package com.game.dataMgr;

import com.game.dao.p.PDataDao;
import com.game.dao.s.StaticDataDao;
import com.game.domain.p.BroodWarData;
import com.game.domain.p.BroodWarHofData;
import com.game.domain.p.BroodWarPosition;
import com.game.domain.p.BroodWarReport;
import com.game.domain.p.BroodWarReportData;
import com.game.domain.s.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author zcp
 * @date 2021/7/6 10:11
 */

@Getter
@Setter
@Component
public class StaticBroodWarMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao staticDataDao;
    @Autowired
    private PDataDao pDataDao;

    private Map<Integer, StaticBroodWarBuff> buffMap = null;
    private Map<Integer, StaticBroodWarCommand> commandMap = null;
    private Map<Integer, StaticBroodWarShop> shopMap = null;
    private List<StaticBroodBuffCost> buffCost = null;
    /**
     * 累杀
     */
    private List<StaticBroodWarKillScore> killScores = new LinkedList<>();
    /**
     * 连杀奖励
     */
    private List<StaticBroodWarKillScore> mulitScores = new LinkedList<>();

    @Override
    public void init() throws Exception {
        Map<Integer, StaticBroodWarBuff> tmpBuffMap = new HashMap<>();
        Map<Integer, StaticBroodWarCommand> tmpCommandMap = new HashMap<>();
        Map<Integer, StaticBroodWarShop> tmpShopMap = new HashMap<>();
        staticDataDao.loadStaticBroodWarBuff().forEach(e -> {
            tmpBuffMap.put(e.getId(), e);
        });
        staticDataDao.loadStaticBroodWarCommand().forEach(e -> {
            tmpCommandMap.put(e.getPosition(), e);
        });
        staticDataDao.loadStaticBroodWarShop().forEach(e -> {
            tmpShopMap.put(e.getId(), e);
        });
        buffMap = tmpBuffMap;
        commandMap = tmpCommandMap;
        shopMap = tmpShopMap;
        buffCost = staticDataDao.loadStaticBroodBuffCost();

        killScores.clear();
        mulitScores.clear();
        staticDataDao.loadStaticBroodWarKillScore().forEach(e -> {
            if (e.getState() == 0) {
                killScores.add(e);
            } else {
                mulitScores.add(e);
            }
        });
    }

    /**
     * @param buyCount
     * @param type     0优先 1立即 2复活
     * @return
     */
    public int getCost(int buyCount, int type) {
        for (StaticBroodBuffCost cost : buffCost) {
            if (type != cost.getType()) {
                continue;
            }
            if (cost.getStart_index() <= buyCount && cost.getEnd_index() >= buyCount) {
                return cost.getCost_gold();
            }
        }
        return -1;
    }


    /**
     * 更新
     *
     * @param data
     */
    public void replaceBroodWar(BroodWarData data) {
        try {
            pDataDao.replaceBroodWar(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载母巢数据
     *
     * @return
     */
    public Map<Integer, BroodWarData> loadBroodWar() {
        List<BroodWarData> data = pDataDao.loadBroodWar();
        Map<Integer, BroodWarData> map = new HashMap<>();
        data.forEach(e -> {
            map.put(e.getCityId(), e);
        });
        return map;
    }

    /**
     * 存储名人堂数据
     *
     * @param data
     */
    public void insertBroodWarHof(BroodWarHofData data) {
        pDataDao.insertBroodWarHof(data);
    }

    /**
     * 加载全部名人堂数据
     *
     * @return
     */
    public List<BroodWarHofData> loadBroodWarHof() {
        return pDataDao.loadBroodWarHof();
    }


    /**
     * 清理历史战况
     */
    public void clearReport() {
        pDataDao.cleanReport();
    }

    /**
     * 保存战况
     *
     * @param report
     */
    public void saveReport(BroodWarReport report) {
        BroodWarReportData data = new BroodWarReportData();
        data.wrap(report);
        pDataDao.saveReport(data);
    }

    /**
     * 加载战况
     *
     * @return
     */
    public List<BroodWarReportData> loadBroodWarReport() {
        return pDataDao.loadReport();
    }


    public void replacePostion(BroodWarPosition position) {
        pDataDao.replacePostion(position);
    }

    public List<BroodWarPosition> loadPostion() {
        return pDataDao.loadPostion();
    }
}
