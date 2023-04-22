package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticPersonality;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author cpz
 * @date 2021/1/26 14:18
 * @description
 */
@Component
public class StaticPersonalityMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao dataDao;

    @Getter
    private Map<Integer, StaticPersonality> dataMap = new ConcurrentHashMap<>();

    @Override
    public void init() throws Exception {
        dataMap = dataDao.loadStaticPersonality();
    }

    public List<StaticPersonality> getByType(int type) {
        List<StaticPersonality> list = dataMap.values().stream().filter(e -> e.getType() == type).collect(Collectors.toList());
        return list;
    }

    public StaticPersonality get(int id) {
        return dataMap.get(id);
    }
}
