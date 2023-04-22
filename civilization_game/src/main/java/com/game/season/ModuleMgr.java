package com.game.season;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.game.util.ClassUtil;
import com.game.worldmap.fight.process.FightProcess;

public class ModuleMgr {

	public static List<Class<?>> modules = new ArrayList<>();

	// public static List<Class<?>> acts = new ArrayList<>();

	public static boolean init() {
		Package pack = BaseModule.class.getPackage();
		Set<Class<?>> allClass = ClassUtil.getClasses(pack);
		for (Class<?> clazz : allClass) {
			if (BaseModule.class.isAssignableFrom(clazz) && !clazz.getName().equals(BaseModule.class.getName())) {
				modules.add(clazz);
			}
			// if (SeasonActivity.class.isAssignableFrom(clazz) && clazz.getName().equals(BaseModule.class.getName())) {
			// acts.add(clazz);
			// }

		}
		return true;
	}

	public static List<Class<?>> getAllModulesClass() {
		return modules;
	}

	// public static List<Class<?>> getAllActsClass() {
	// return acts;
	// }
}
