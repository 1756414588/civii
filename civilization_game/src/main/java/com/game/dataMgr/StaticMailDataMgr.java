package com.game.dataMgr;

import com.game.domain.Award;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticMail;
import com.game.domain.s.StaticMailPlat;

@Component
public class StaticMailDataMgr extends BaseDataMgr {
	@Autowired
	private StaticDataDao staticDataDao;

	private Map<Integer, StaticMail> mailMap = new HashMap<Integer, StaticMail>();

	private Map<Integer, List<StaticMailPlat>> mailPlatMap = new HashMap<Integer, List<StaticMailPlat>>();

	/**
	 * Overriding: init
	 * 
	 * @see com.game.dataMgr.BaseDataMgr#init()
	 */
	@Override
	public void init() throws Exception{
		mailMap = staticDataDao.selectMail();
		Iterator<StaticMail> it = mailMap.values().iterator();
		while (it.hasNext()) {
			StaticMail next = it.next();
			if (next.getTitleIndex() != null && next.getTitleIndex().size() > 0) {
				int[] title = new int[next.getTitleIndex().size()];
				for (int i = 0; i < next.getTitleIndex().size(); i++) {
					title[i] = next.getTitleIndex().get(i);
				}
				next.setTitleIndexArr(title);
			}
		}
		mailPlatMap.clear();
		List<StaticMailPlat> mailPlatList = staticDataDao.selectMailPlat();
		for (StaticMailPlat e : mailPlatList) {
			List<StaticMailPlat> elist = mailPlatMap.get(e.getPlatNo());
			if (elist == null) {
				elist = new ArrayList<StaticMailPlat>();
				mailPlatMap.put(e.getPlatNo(), elist);
			}
			elist.add(e);
		}
	}

	public StaticMail getStaticMail(int mailId) {
		return mailMap.get(mailId);
	}

	public List<StaticMailPlat> getPlatMail(int platNo) {
		return mailPlatMap.get(platNo);
	}

	public int[] copyTitleParam(StaticMail staticMail) {
		if (staticMail.getTitleIndexArr() == null) {
			return null;
		}
		int[] title = new int[staticMail.getTitleIndexArr().length];
		System.arraycopy(staticMail.getTitleIndexArr(), 0, title, 0, title.length);
		return title;
	}

	/**
	 * 获取邮件的奖励列表
	 **/
	public List<Award> getMailAward(int mailId) {
		List<Award> list = Lists.newArrayList();
		StaticMail staticMail = getStaticMail(mailId);
		if (staticMail.getAward() == null || staticMail.getAward().isEmpty()) {
			return list;
		}
		for (List<Integer> integers : staticMail.getAward()) {
			if (integers.size() != 3) {
				continue;
			}
			list.add(new Award(integers.get(0), integers.get(1), integers.get(2)));
		}
		return list;
	}

}
