package com.game.dao.p;

import com.game.domain.p.Manoeuvre;
import java.util.List;

public interface ManoeuvreDao {

	public List<Manoeuvre> selectTopList();

	public void updateManoeuvre(Manoeuvre manoeuvre);

	public void insertManoeuvre(Manoeuvre manoeuvre);
}
