package com.game.dao.p;

import java.util.List;

import com.game.domain.p.Activity;

public interface ActivityDao {

	public List<Activity> selectActivityList();
	
	public int updateActivity(Activity activity);

	public void insertActivity(Activity activity);
}
