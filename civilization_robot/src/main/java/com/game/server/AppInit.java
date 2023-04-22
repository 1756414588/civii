package com.game.server;

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
 * 初始化管理
 */
public class AppInit {

	/**
	 * @param pack
	 */
	public static void load(Package pack) {
		iniLoad(pack);
	}

	public static void iniLoad(Package pack) {
		try {
			List<Pair<LoadData, ILoadData>> loadList = new ArrayList<>();
			Set<Class<?>> allClasses = ClassUtil.getClasses(pack);

			for (Class<?> clazz : allClasses) {
				LoadData clazzAnnotation = clazz.getAnnotation(LoadData.class);
				if (clazzAnnotation != null) {
					ILoadData loadData = (ILoadData) SpringUtil.getBean(clazz);
					loadList.add(new Pair<>(clazzAnnotation, loadData));
				}
			}

			// 数据加载
			loadList.forEach(e -> {
				try{
					e.getRight().load();
				}catch (Exception ex){
					ex.printStackTrace();
				}
			});

			// 按顺序初始化
			List<Pair<LoadData, ILoadData>> sortList = loadList.stream().sorted(Comparator.comparing(e -> e.getLeft().initSeq())).collect(Collectors.toList());
			for (Pair<LoadData, ILoadData> pair : sortList) {
				pair.getRight().init();
				LoadData loadData = pair.getLeft();
				LogHelper.CHANNEL_LOGGER.info("【启动加载】 顺序:{} 描述:{}", loadData.initSeq(), loadData.name());
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
			System.exit(0);
		}
	}


}
