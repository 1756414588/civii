package com.game.dao.s;

import com.game.domain.s.StaticEquip;
import com.game.domain.s.StaticMail;
import com.game.domain.s.StaticWorldCity;
import com.game.domain.s.StaticWorldMap;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;

public interface StaticConfigDao {

	@MapKey("mailId")
	public Map<Integer, StaticMail> selectMail();

	@MapKey("equipId")
	public Map<Integer, StaticEquip> selectEquipMap();

	@MapKey("mapId")
	public Map<Integer, StaticWorldMap> selectWorldMap();

	@MapKey("cityId")
	public Map<Integer, StaticWorldCity> selectWorldCity();
}
