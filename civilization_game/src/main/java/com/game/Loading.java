package com.game;

import com.game.define.LoadData;
import com.game.load.ILoadData;
import com.game.spring.SpringUtil;
import com.game.util.ClassUtil;
import com.game.util.LogHelper;
import com.game.util.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author 陈奎
 * @Description 加载数据类(注意该类只能放到game或更上层的包目录下, 否则会读不到加载数据)
 * @Date 2023/4/6 17:39
 **/

public class Loading {

	public static Loading inst = new Loading();

	public static final int LOAD_CONFIG_DB = 1;
	public static final int LOAD_USER_DB = 2;

	public static Loading getInst() {
		return inst;
	}

	/**
	 * 加载数据
	 */
	public void load() throws Exception {
		loadData(LOAD_CONFIG_DB, Loading.class.getPackage());
		loadData(LOAD_USER_DB, Loading.class.getPackage());
	}


	/**
	 * 加载
	 *
	 * @param type
	 * @param pack
	 */
	private void loadData(int type, Package pack) throws Exception {
		List<Pair<LoadData, ILoadData>> loadList = new ArrayList<>();

		// 根目录下所有的class
		Set<Class<?>> allClasses = ClassUtil.getClasses(pack);

		for (Class<?> clazz : allClasses) {
			LoadData clazzAnnotation = clazz.getAnnotation(LoadData.class);
			if (clazzAnnotation != null) {
				if (clazzAnnotation.type() == type) {
					ILoadData loadData = (ILoadData) SpringUtil.getBean(clazz);
					loadList.add(new Pair<>(clazzAnnotation, loadData));
				}
			}
		}

		// 数据加载
		for (Pair<LoadData, ILoadData> e : loadList) {
			LoadData loadData = e.getLeft();
			LogHelper.GAME_LOGGER.info("【load{}表】 {}", type == 1 ? "配置" : "用户", loadData.name());
			e.getRight().load();
		}

		LogHelper.GAME_LOGGER.info("====={}表数据加载完毕======", type == 1 ? "配置" : "用户");

		// 按顺序初始化
		List<Pair<LoadData, ILoadData>> sortList = loadList.stream().sorted(Comparator.comparing(e -> e.getLeft().initSeq())).collect(Collectors.toList());
		for (Pair<LoadData, ILoadData> pair : sortList) {
			LoadData loadData = pair.getLeft();
			LogHelper.GAME_LOGGER.info("【init 初始化 {}】 {}  -->{}", type == 1 ? "配置" : "用户模块", loadData.name(), loadData.initSeq());
			pair.getRight().init();
		}

		LogHelper.GAME_LOGGER.info("====={} init complete ======", type == 1 ? "配置" : "用户模块");
	}

}
