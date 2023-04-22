package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticWorldActPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jyb
 * @date 2020/1/17 16:20
 * @description
 */
@Component
@LoadData(name = "世界活动")
public class StaticWorldActPlanMgr extends BaseDataMgr {


    private Map<Integer, StaticWorldActPlan> plans = new HashMap<>();

    @Autowired
    private StaticDataDao staticDataDao;

    @Override
    public void load() throws Exception {
        plans = staticDataDao.selectWorldActPlan();
    }

    @Override
    public void init() throws Exception{
    }

    public StaticWorldActPlan get(int id) {
        return plans.get(id);
    }

    public Map<Integer, StaticWorldActPlan> getPlans(){
    	return plans;
    }

    public StaticWorldActPlan getByTargetId(int targetId) {
        for (Map.Entry<Integer, StaticWorldActPlan> entry : plans.entrySet()) {
            if (entry.getValue().getTargetId() == targetId) {
                return entry.getValue();

            }
        }
        return  null;
    }

    public List<StaticWorldActPlan> getListByTargetId(int targetId) {
        return plans.values().stream().filter(e-> e.getTargetId() == targetId).collect(Collectors.toList());
    }
}
