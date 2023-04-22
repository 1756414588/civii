package com.game.dao.p;

import com.game.domain.p.Zerg;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;

public interface ZergDao {

	@MapKey("openTime")
	public Map<Long, Zerg> selectZerg();

	public void updateZerg(Zerg zerg);

	public void insertZerg(Zerg zerg);

}
