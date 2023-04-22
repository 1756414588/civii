package com.game.dataMgr;

import com.game.define.LoadData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.domain.p.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticOmType;
import com.game.domain.s.StaticOmament;

/**
 * 2020年8月5日
 *
 * @CaoBing halo_game StaticOmTypeMgr.java
 **/
@Component
@LoadData(name = "饰品配置表")
public class StaticOmamentMgr extends BaseDataMgr {

	private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StaticDataDao staticDataDao;

    // 配饰的基础信息
    List<StaticOmament> allOmamentList = new ArrayList<StaticOmament>();
    private Map<Integer, StaticOmament> omamentMap = new HashMap<Integer, StaticOmament>();

    // 配饰的类型信息
    List<StaticOmType> allOmTypeList = new ArrayList<StaticOmType>();
    private Map<Integer, StaticOmType> omTypeMap = new HashMap<Integer, StaticOmType>();

	// 配饰类型:列表
	private Map<Integer, List<StaticOmament>> typeOmamentMap = new HashMap<>();

	@Override
	public void load() throws Exception {
		initOmament();
		initOmamentType();
	}

	@Override
    public void init() throws Exception {

    }

	// 初始化美女的基础配置信息
	private void initOmament() throws ConfigException {
		omamentMap.clear();
		typeOmamentMap.clear();
		allOmamentList = staticDataDao.selectStaticOmament();
		if (allOmamentList.isEmpty()) {
			return;
		}
		for (StaticOmament staticOmament : allOmamentList) {
			if (staticOmament == null) {
				throw new ConfigException("StaticOmament is null");
			}
			omamentMap.put(staticOmament.getId(), staticOmament);

			List<StaticOmament> typeList = typeOmamentMap.get(staticOmament.getType());
			if (typeList == null) {
				typeList = new ArrayList<>();
				typeOmamentMap.put(staticOmament.getType(), typeList);
			}
			typeList.add(staticOmament);
		}
    }

    // 初始化美女的服装信息
    private void initOmamentType() {
        omTypeMap.clear();
        allOmTypeList = staticDataDao.selectStaticOmType();
        if (allOmTypeList.size() > 0) {
            for (StaticOmType staticOmType : allOmTypeList) {
                omTypeMap.put(staticOmType.getId(), staticOmType);
            }
        }
    }

    // 获取所有配饰列表
    public List<StaticOmament> getAllStaticOmament() {
        return this.allOmamentList;
    }

    // 获取单个配饰
    public StaticOmament getStaticOmament(int id) {
		if (this.omamentMap.containsKey(id)) {
			return this.omamentMap.get(id);
		}
		return null;
	}

    // 获取所有配饰类型列表
    public List<StaticOmType> getAllStaticOmType() {
        return this.allOmTypeList;
    }

    // 获取单个配饰类型
    public StaticOmType getStaticOmTypes(int id) {
		if (omTypeMap.containsKey(id)) {
        	return this.omTypeMap.get(id);
    }
		return null;
	}

    // 获取某类配饰
    public List<StaticOmament> getStaticOmamentByType(int type) {
		if (typeOmamentMap.containsKey(type)) {
			return typeOmamentMap.get(type);
		}
		return null;
    }
}
