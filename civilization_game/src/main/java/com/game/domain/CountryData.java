package com.game.domain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.game.constant.CountryConst;
import com.game.constant.HeroState;
import com.game.domain.p.Country;
import com.game.domain.p.CountryHero;
import com.game.domain.p.CountryRank;
import com.game.domain.p.CtyDaily;
import com.game.domain.p.CtyGlory;
import com.game.domain.p.CtyGovern;
import com.game.pb.DataPb;
import com.game.pb.SerializePb.SerCountryDailyData;
import com.game.pb.SerializePb.SerCountryGloryData;
import com.game.pb.SerializePb.SerCountryGovernData;
import com.game.pb.SerializePb.SerCountryHero;
import com.game.pb.SerializePb.SerCountryRankData;
import com.game.server.GameServer;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 每个国家的数据结构
 */
public class CountryData {

	private int countryId; // 国家Id
	private int level; // 国家等级
	private long exp; // 国家经验
	private String announcement; // 国家公告
	private String publisher; // 公告发布者名字
	private long voteTime;// 选举时间
	private long voteHour;// 选举持续时长
	private long taskTime; // 任务刷新时间
	private long rankTime; // 国家建设,城战,国战排行清理时间
	private int appoint;// 已任命次数

	public volatile int voteState;// 0初始化 1.选举中 2.选举完毕

	// 国家荣誉
	private CtyGlory glory = new CtyGlory();

	// 1.城战排行 2.国战排行3.建设排行
	private Map<Integer, CountryRank> ranks = new HashMap<Integer, CountryRank>();

	// 国家日志
	private ConcurrentLinkedQueue<CtyDaily> dailys = new ConcurrentLinkedQueue<CtyDaily>();

	// 国家官员
	private ConcurrentHashMap<Long, CtyGovern> governs = new ConcurrentHashMap<Long, CtyGovern>();

	// 主席,总理,总司令
	private ConcurrentHashMap<Integer, CtyGovern> offeres = new ConcurrentHashMap<Integer, CtyGovern>();

	// 国家官员(轮换期间的中间数据)
	private ConcurrentHashMap<Long, CtyGovern> tempGoverns = new ConcurrentHashMap<Long, CtyGovern>();

	// 主席,总理,总司令(轮换期间的中间数据)
	private ConcurrentHashMap<Integer, CtyGovern> tempOfferes = new ConcurrentHashMap<Integer, CtyGovern>();

	private Map<Integer, CountryHero> countryHeroMap = new HashMap<Integer, CountryHero>();
	private int soldierNum; // 点兵符数量
	private int killNum;//参谋部任务整个国家杀怪的数量
	private String countryName;//阵营名称
	private int checkState;//审核状态 1 审核中 2 审核通过
	private String modifyName; //审核中的名字
	private int modifyTime;//改名的次数
	private long modifyPlayer;//上一次更改的玩家

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public CountryData(Country country) {
		this.countryId = country.getCountryId();
		this.level = country.getLevel();
		this.level = Math.max(1, this.level);
		this.exp = country.getExp();
		if (country.getAnnouncement() != null) {
			this.announcement = country.getAnnouncement();
		}

		if (country.getPublisher() != null) {
			this.publisher = country.getPublisher();
		}
		this.voteState = country.getVoteState();
		this.voteTime = country.getVoteTime();
		if (country.getVoteHour() == 0) {
			this.voteHour = 18 * 3600 * 1000;
		} else {
			this.voteHour = country.getVoteHour() * 60 * 1000L;
		}
		this.taskTime = country.getTaskTime();
		this.appoint = country.getAppoint();
		this.rankTime = country.getRankTime();
		this.dserCountryData(country);
		this.soldierNum = country.getSoldierNum();
		this.killNum = country.getKillNum();
		this.countryName = country.getCountryName();
		this.checkState = country.getCheckState();
		this.modifyTime = country.getModifyTime();
		this.modifyPlayer = country.getModifyPlayer();
		this.modifyName = country.getModifyName();
	}

	public int getKillNum() {
		try {
			lock.readLock().lock();
			return killNum;
		} finally {
			lock.readLock().unlock();
		}

	}

	public void setKillNum(int killNum) {
		try {
			lock.writeLock().lock();
			this.killNum = killNum;
		} finally {
			lock.writeLock().unlock();
		}

	}

	/**
	 * 反序列化信息
	 *
	 * @return
	 */
	private void dserCountryData(Country country) {
		this.dserDaily(country.getDaily());
		this.dserGovern(country.getGovern());
		this.dserTempGovern(country.getTempGovern());
		this.dserGlory(country.getGlory());
		this.dserRank(country.getCountryRank());
		this.dserHero(country.getCountryHero());
	}

	public void dserHero(byte[] heroInfo) {
		if (heroInfo == null) {
			return;
		}
		try {
			SerCountryHero data = SerCountryHero.parseFrom(heroInfo);
			List<DataPb.CountryHeroData> list = data.getDataList();
			for (DataPb.CountryHeroData elem : list) {
				if (elem == null) {
					continue;
				}
				CountryHero countryHero = new CountryHero();
				countryHero.readData(elem);
				if (countryHero.getLordId() == 0 && countryHero.getState() == HeroState.ACTIVATE) {
					countryHero.setState(HeroState.OPENED);
				}
				countryHeroMap.put(elem.getHeroId(), countryHero);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拷贝数据
	 */
	public Country copyCountry() {
		Country country = new Country();
		country.setCountryId(this.countryId);
		country.setLevel(this.level);
		country.setExp(this.exp);
		country.setAnnouncement(this.announcement);
		country.setPublisher(this.publisher);
		country.setVoteTime(this.voteTime);
		country.setTaskTime(this.taskTime);
		country.setVoteState(this.voteState);
		country.setAppoint(this.appoint);
		country.setRankTime(this.rankTime);
		country.setGovern(serGovern());
		country.setTempGovern(serTempGovern());
		country.setGlory(serGlory());
		country.setCountryRank(serRank());
		country.setDaily(serDailys());
		country.setCountryHero(serHero());
		country.setSoldierNum(this.soldierNum);
		country.setKillNum(this.killNum);
		country.setCountryName(this.countryName);
		country.setCheckState(this.checkState);
		country.setModifyTime(this.modifyTime);
		country.setModifyPlayer(this.modifyPlayer);
		country.setCountryName(this.countryName);
		return country;
	}

	// 日志反序列化
	private void dserDaily(byte[] daily) {
		if (daily == null) {
			return;
		}
		try {
			SerCountryDailyData data = SerCountryDailyData.parseFrom(daily);
			List<DataPb.CountryDailyData> list = data.getDailyDataList();
			int count = list.size();
			int i = 0;
			// 兼容处理
			if (count > CountryConst.DAILY_MAX) {
				i = count - CountryConst.DAILY_MAX;
			}
			for (; i < list.size(); i++) {
				DataPb.CountryDailyData e = list.get(i);
				CtyDaily cty = new CtyDaily(e);
				dailys.add(cty);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	// 日志序列化
	private byte[] serDailys() {
		SerCountryDailyData.Builder ser = SerCountryDailyData.newBuilder();
		try {
			Iterator<CtyDaily> it = dailys.iterator();
			while (it.hasNext()) {
				CtyDaily e = it.next();
				ser.addDailyData(e.ser());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ser.build().toByteArray();
	}

	private byte[] serHero() {
		SerCountryHero.Builder ser = SerCountryHero.newBuilder();
		try {
			Iterator<CountryHero> it = countryHeroMap.values().iterator();
			while (it.hasNext()) {
				CountryHero e = it.next();
				ser.addData(e.writeData());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ser.build().toByteArray();
	}

	// 临时官员序列化
	private byte[] serTempGovern() {
		SerCountryGovernData.Builder ser = SerCountryGovernData.newBuilder();
		Iterator<CtyGovern> it = tempGoverns.values().iterator();
		while (it.hasNext()) {
			CtyGovern e = it.next();
			ser.addGovernData(e.ser());
		}
		return ser.build().toByteArray();
	}

	// 临时官员反序列化
	private void dserTempGovern(byte[] govern) {
		if (govern == null) {
			return;
		}
		try {
			SerCountryGovernData data = SerCountryGovernData.parseFrom(govern);
			List<DataPb.CountryGovernData> list = data.getGovernDataList();
			for (DataPb.CountryGovernData e : list) {
				CtyGovern cty = new CtyGovern(e);

				if (cty.getGovernId() > 0 && cty.getGovernId() < CountryConst.GOVERN_GENERAL) {
					tempOfferes.put(cty.getGovernId(), cty);
				}

				tempGoverns.put(cty.getLordId(), cty);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	// 官员反序列化
	private void dserGovern(byte[] govern) {
		if (govern == null) {
			return;
		}
		try {
			SerCountryGovernData data = SerCountryGovernData.parseFrom(govern);
			List<DataPb.CountryGovernData> list = data.getGovernDataList();
			for (DataPb.CountryGovernData e : list) {
				CtyGovern cty = new CtyGovern(e);

				if (cty.getGovernId() > 0 && cty.getGovernId() < CountryConst.GOVERN_GENERAL) {
					offeres.put(cty.getGovernId(), cty);
				}

				governs.put(cty.getLordId(), cty);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	// 官员序列化
	private byte[] serGovern() {
		SerCountryGovernData.Builder ser = SerCountryGovernData.newBuilder();
		Iterator<CtyGovern> it = governs.values().iterator();
		while (it.hasNext()) {
			CtyGovern e = it.next();
			ser.addGovernData(e.ser());
		}
		return ser.build().toByteArray();
	}

	// 排名反序列化
	private void dserRank(byte[] rank) {
		if (rank == null) {
			return;
		}
		try {
			SerCountryRankData data = SerCountryRankData.parseFrom(rank);
			List<DataPb.CountryRankData> list = data.getCountryRankList();
			for (DataPb.CountryRankData e : list) {
				int type = e.getType();
				CountryRank countryRank = ranks.get(type);
				if (countryRank == null) {
					countryRank = new CountryRank();
					countryRank.setType(type);
					ranks.put(type, countryRank);
				}
				if (e.getCtyRankCount() > 0) {
					countryRank.iniRank(e.getCtyRankList());
				}
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	// 排名序列化
	private byte[] serRank() {
		SerCountryRankData.Builder ser = SerCountryRankData.newBuilder();
		Iterator<CountryRank> it = ranks.values().iterator();
		while (it.hasNext()) {
			CountryRank e = it.next();
			ser.addCountryRank(e.ser());
		}
		return ser.build().toByteArray();
	}

	// 国家荣誉记录反序列化
	private void dserGlory(byte[] glory) {
		if (glory == null) {
			return;
		}
		try {
			SerCountryGloryData data = SerCountryGloryData.parseFrom(glory);
			this.glory = new CtyGlory();
			if (data != null) {
				this.glory.setGlory(data);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	// 国家荣誉记录序列化
	private byte[] serGlory() {
		SerCountryGloryData.Builder ser = SerCountryGloryData.newBuilder();
		ser.setBuild(this.glory.getBuilds());
		ser.setCity(this.glory.getCityFight());
		ser.setState(this.glory.getStateFight());
		ser.setTime(this.glory.getRefreshTime());
		return ser.build().toByteArray();
	}

	// 是否有权限操作
	public boolean hasPermission(long lordId) {
		CtyGovern cityGoven = governs.get(lordId);
		if (cityGoven == null) {
			return false;
		}

		if (cityGoven.getGovernId() == CountryConst.GOVERN_KING || cityGoven.getGovernId() == CountryConst.GOVERN_PRIME
			|| cityGoven.getGovernId() == CountryConst.GOVERN_ADVISER) {
			return true;
		}

		return false;
	}

	// 获取国家荣誉
	public CtyGlory getGlory() {
		if (glory.getRefreshTime() != GameServer.getInstance().currentDay) {
			glory.setBuilds(0);
			glory.setCityFight(0);
			glory.setStateFight(0);
			glory.setRefreshTime(GameServer.getInstance().currentDay);
			glory.getCurrentDayPush().clear();
		}
		return glory;
	}

	/**
	 * 获取元帅数量
	 */
	public int getGeneralCount() {
		if (governs.size() <= 3) {
			return 0;
		}
		int total = 0;
		for (CtyGovern govern : governs.values()) {
			if (govern.getGovernId() == 4) {
				total += 1;
			}
		}
		return total;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getExp() {
		return exp;
	}

	public void setExp(long exp) {
		this.exp = exp;
	}

	public int getCountryId() {
		return countryId;
	}

	public String getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(String announcement) {
		this.announcement = announcement;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public long getVoteTime() {
		return voteTime;
	}

	public void setVoteTime(long voteTime) {
		this.voteTime = voteTime;
	}

	public long getTaskTime() {
		return taskTime;
	}

	public void setTaskTime(long taskTime) {
		this.taskTime = taskTime;
	}

	public int getAppoint() {
		return appoint;
	}

	public void setAppoint(int appoint) {
		this.appoint = appoint;
	}

	public Map<Integer, CountryRank> getRanks() {
		return ranks;
	}

	public ConcurrentLinkedQueue<CtyDaily> getDailys() {
		return dailys;
	}

	public ConcurrentHashMap<Long, CtyGovern> getGoverns() {
		return governs;
	}

	public int getGovernId(long lordId) {
		if (voteState == CountryConst.VOTE_END) {
			CtyGovern ctyGovern = governs.get(lordId);
			if (ctyGovern != null) {
				return ctyGovern.getGovernId();
			}
		} else {
			CtyGovern ctyGovern = tempGoverns.get(lordId);
			if (ctyGovern != null) {
				return ctyGovern.getGovernId();
			}
		}

		return 0;
	}

	public boolean isGovern(long lordId) {
		if (voteState == CountryConst.VOTE_END) {
			return governs.containsKey(lordId);
		} else {
			return tempGoverns.containsKey(lordId);
		}
	}

	public void setGoverns(ConcurrentHashMap<Long, CtyGovern> governs) {
		this.governs = governs;
	}

	public long getVoteHour() {
		return voteHour;
	}

	public CountryRank getCountryRank(int type) {
		return ranks.get(type);
	}

	public CtyGovern getCtyGovern(long lordId) {
		if (voteState == CountryConst.VOTE_END) {
			return governs.get(lordId);
		} else {
			return tempGoverns.get(lordId);
		}
	}

	/**
	 * 判断某个玩家是不是 主席 总理 或者总司令
	 *
	 * @param lordId
	 * @return
	 */
	public CtyGovern getCtyGovernOffer(long lordId) {
		if (voteState == CountryConst.VOTE_END) {
			for (CtyGovern ctyGovern : governs.values()) {
				if (ctyGovern.getLordId() == lordId) {
					return ctyGovern;
				}
			}
		} else {
			for (CtyGovern ctyGovern : tempGoverns.values()) {
				if (ctyGovern.getLordId() == lordId) {
					return ctyGovern;
				}
			}
		}
		return null;
	}

	public ConcurrentHashMap<Integer, CtyGovern> getOfferes() {
		return offeres;
	}

	public long getRankTime() {
		return rankTime;
	}

	public void setRankTime(long rankTime) {
		this.rankTime = rankTime;
	}

	/**
	 * 重新选举
	 */
	public void restartVote() {
		this.tempGoverns.clear();
		this.tempOfferes.clear();

		this.governs.forEach((key, value) -> {
			this.tempGoverns.put(key, value);
		});

		this.offeres.forEach((key, value) -> {
			this.tempOfferes.put(key, value);
		});
		this.offeres.clear();
		this.governs.clear();
	}

	/**
	 * 完成选举
	 *
	 * @return
	 */
	public void successVote() {
		this.tempGoverns.clear();
		this.tempOfferes.clear();
		this.appoint = 0;
//		this.announcement = null;(策划>>>>>夏鼎>>>>>要求保留)
//		this.publisher = null;
	}

	public Map<Integer, CountryHero> getCountryHeroMap() {
		return countryHeroMap;
	}

	public void setCountryHeroMap(Map<Integer, CountryHero> countryHeroMap) {
		this.countryHeroMap = countryHeroMap;
	}

	public CountryHero getCountryHero(int heroId) {
		return countryHeroMap.get(heroId);
	}

	public int getSoldierNum() {
		return soldierNum;
	}

	public void setSoldierNum(int soldierNum) {
		this.soldierNum = soldierNum;
	}

	public void updateSoldierNum(int num) {
		if (num <= 0) {
			LogHelper.CONFIG_LOGGER.error("country updateSoldierNum num is less then 0.");
			return;
		}
		this.soldierNum += num;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public int getCheckState() {
		return checkState;
	}

	public void setCheckState(int checkState) {
		this.checkState = checkState;
	}

	public String getModifyName() {
		return modifyName;
	}

	public void setModifyName(String modifyName) {
		this.modifyName = modifyName;
	}

	public int getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(int modifyTime) {
		this.modifyTime = modifyTime;
	}

	public long getModifyPlayer() {
		return modifyPlayer;
	}

	public void setModifyPlayer(long modifyPlayer) {
		this.modifyPlayer = modifyPlayer;
	}

	public ConcurrentHashMap<Long, CtyGovern> getTempGoverns() {
		return tempGoverns;
	}

	public void setTempGoverns(ConcurrentHashMap<Long, CtyGovern> tempGoverns) {
		this.tempGoverns = tempGoverns;
	}

	public ConcurrentHashMap<Integer, CtyGovern> getTempOfferes() {
		return tempOfferes;
	}

	public void setTempOfferes(ConcurrentHashMap<Integer, CtyGovern> tempOfferes) {
		this.tempOfferes = tempOfferes;
	}
}
