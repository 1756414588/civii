package com.game.dao.p;

import com.game.domain.p.BroodWarData;
import com.game.domain.p.BroodWarHofData;
import com.game.domain.p.BroodWarPosition;
import com.game.domain.p.BroodWarReportData;
import com.game.domain.p.Item;

import java.util.List;

/**
 * 玩家数据dao
 *
 *
 * @date 2020/11/12 17:56
 * @description
 */
public interface PDataDao {

    /**
     * 加载全部道具
     *
     * @param lordId
     * @return
     */
    public List<Item> loadAllItem(long lordId);

    /**
     * 更新道具
     *
     * @param item
     */
    public void replaceItem(Item item);

    /**
     * 更新
     *
     * @param data
     */
    public void replaceBroodWar(BroodWarData data);

    /**
     * 加载母巢数据
     *
     * @return
     */
    public List<BroodWarData> loadBroodWar();

    /**
     * 存储名人堂数据
     *
     * @param data
     */
    public void insertBroodWarHof(BroodWarHofData data);

    /**
     * 加载全部名人堂数据
     *
     * @return
     */
    public List<BroodWarHofData> loadBroodWarHof();

    public void cleanReport();

    public void saveReport(BroodWarReportData data);

    public List<BroodWarReportData> loadReport();

    public List<BroodWarPosition> loadPostion();

    public void replacePostion(BroodWarPosition position);
}
