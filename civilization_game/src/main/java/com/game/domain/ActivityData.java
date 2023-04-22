package com.game.domain;

import com.game.constant.ActivityConst;
import com.game.domain.p.*;
import com.game.domain.s.ActivityBase;
import com.game.manager.PlayerManager;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.pb.DataPb.CampMemberDate;
import com.game.pb.SerializePb;
import com.game.pb.SerializePb.SerActAddtionData;
import com.game.pb.SerializePb.SerRecords;
import com.game.pb.SerializePb.SerStatusData;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
public class ActivityData extends ActRecord {

	private String params;// 不定参数

	private ReadWriteLock lock = new ReentrantReadWriteLock();

	// 排行
	private int sortord = ActivityConst.DESC;// 默认排序方式倒序
	private LinkedList<ActPlayerRank> ranks = new LinkedList<ActPlayerRank>();
	private Map<Long, ActPlayerRank> actRanks = new ConcurrentHashMap<Long, ActPlayerRank>();

	//阵营骨干排行
	private Map<Integer, LinkedList<CampMembersRank>> campMembers = new ConcurrentHashMap<>();

	//奖池抽奖记录
	private LinkedList<LuckPoolRewardRecord> records = new LinkedList<>();

	//
	private Map<Long, Long> addtions = new HashMap<Long, Long>();

	// 个人历史最高排名记录
	private int history;

	// 最近一次更新时间
	public long lastSaveTime;

	public ActivityData() {

	}

	public ActivityData(ActivityBase activityBase, int begin) {
		super(activityBase, begin);
	}

	public ActivityData(Activity activity) throws InvalidProtocolBufferException {
		this.activityId = activity.getActivityId();
		this.beginTime = activity.getBeginTime();
		this.sortord = activity.getSortord();
		this.params = activity.getParams();
		this.history = activity.getHistory();
		this.awardId = activity.getAwardId();
		this.lastSaveTime = System.currentTimeMillis();

		dserActRank(activity.getRanks());
		dserAddtion(activity.getAddtion());
		dserStatus(activity.getStatus());
		dserRecords(activity.getRecords());
		dserCampMembers(activity.getCampMembers());
		dserRecord(activity.getRecord());
	}

	public Activity copyData() {
		Activity entity = new Activity();
		entity.setActivityId(this.activityId);// 活动ID
		entity.setBeginTime(this.beginTime);
		entity.setSortord(this.sortord);
		entity.setAwardId(this.awardId);
		entity.setHistory(this.history);
		entity.setRanks(this.serActRank());
		entity.setAddtion(this.serAddtion());
		entity.setStatus(this.serStatus());
		entity.setRecords(this.serRecords());
		entity.setCampMembers(this.serCampMembers());
		if (this.getParams() != null) {
			entity.setParams(new String(this.getParams()));// 额外参数
		}
		entity.setLastSaveTime(this.lastSaveTime);
		entity.setRecord(this.serRecord());
		return entity;
	}

	public int getSortord() {
		return sortord;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public int getHistory() {
		return history;
	}

	public void setHistory(int history) {
		this.history = history;
	}

	public Map<Long, Long> getAddtions() {
		return addtions;
	}

	/**
	 * 记录玩家的个人历史记录
	 */
	public void recordHistory() {
		try {
			lock.readLock().lock();
			Iterator<ActPlayerRank> it = ranks.iterator();
			long rank = 1;
			while (it.hasNext()) {
				ActPlayerRank next = it.next();
				long prank = this.getStatus(next.getLordId());
				if (prank == 0 || prank > rank) {
					this.putState(next.getLordId(), rank++);
				}
			}
		} catch (Exception e) {
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean isReset(int begin) {
		boolean flag = super.isReset(begin);
		if (flag) {
			this.history = 0;
			this.params = "";
			this.addtions.clear();
			this.actRanks.clear();
			this.ranks.clear();
		}
		return flag;
	}

	public byte[] serActRank() {
		SerializePb.SerActRankData.Builder ser = SerializePb.SerActRankData.newBuilder();
		try {
			lock.readLock().lock();
			Iterator<ActPlayerRank> it = ranks.iterator();
			while (it.hasNext()) {
				ActPlayerRank playRank = it.next();
				ser.addActivityRank(PbHelper.createActRank(playRank));
			}
		} catch (Exception e) {
		} finally {
			lock.readLock().unlock();
		}
		return ser.build().toByteArray();
	}

	public void dserActRank(byte[] data) throws InvalidProtocolBufferException {
		if (data == null) {
			return;
		}
		SerializePb.SerActRankData ser = SerializePb.SerActRankData.parseFrom(data);
		List<DataPb.ActivityRank> list = ser.getActivityRankList();
		for (DataPb.ActivityRank e : list) {
			if (e.getValue() <= 0) {
				continue;
			}
			addPlayerRank(e.getLordId(), e.getValue());
		}
	}

	public byte[] serAddtion() {
		SerActAddtionData.Builder ser = SerActAddtionData.newBuilder();
		Iterator<Entry<Long, Long>> it = addtions.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Long, Long> next = it.next();
			ser.addAddtion(PbHelper.createAddtionPb(next.getKey(), next.getValue()));
		}
		return ser.build().toByteArray();
	}

	public void dserAddtion(byte[] data) throws InvalidProtocolBufferException {
		if (data == null) {
			return;
		}
		SerActAddtionData ser = SerActAddtionData.parseFrom(data);

		List<DataPb.Addtion> list = ser.getAddtionList();
		for (DataPb.Addtion e : list) {
			addtions.put(e.getAddtionId(), e.getAddtionValue());
		}
	}

	public byte[] serStatus() {
		SerStatusData.Builder ser = SerStatusData.newBuilder();
		Iterator<Entry<Long, Long>> it = status.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Long, Long> next = it.next();
			ser.addStatus(PbHelper.createStatusPb(next.getKey(), next.getValue()));
		}
		return ser.build().toByteArray();
	}

	public void dserStatus(byte[] data) throws InvalidProtocolBufferException {
		if (data == null) {
			return;
		}
		SerStatusData ser = SerStatusData.parseFrom(data);

		List<DataPb.Status> list = ser.getStatusList();
		for (DataPb.Status e : list) {
			status.put(e.getK(), e.getV());
		}
	}

	public byte[] serRecords() {
		SerRecords.Builder ser = SerRecords.newBuilder();
		Iterator<LuckPoolRewardRecord> it = records.iterator();
		while (it.hasNext()) {
			LuckPoolRewardRecord next = it.next();
			ser.addRecord(next.serRecordData());
		}
		return ser.build().toByteArray();
	}

	public byte[] serRecord() {
		SerializePb.SerTwoInt.Builder bu = SerializePb.SerTwoInt.newBuilder();
		record.forEach((key, val) -> {
			CommonPb.TwoInt.Builder rPb = CommonPb.TwoInt.newBuilder();
			rPb.setV1(key);
			rPb.setV2(val);
			bu.addData(rPb.build());
		});
		return bu.build().toByteArray();
	}

	public void dserRecord(byte[] data) throws InvalidProtocolBufferException {
		if (data == null) {
			return;
		}
		SerializePb.SerTwoInt ser = SerializePb.SerTwoInt.parseFrom(data);
		List<CommonPb.TwoInt> list = ser.getDataList();
		for (CommonPb.TwoInt e : list) {
			record.put(e.getV1(), e.getV2());
		}
	}

	public void dserRecords(byte[] data) throws InvalidProtocolBufferException {
		if (data == null) {
			return;
		}
		SerRecords ser = SerRecords.parseFrom(data);
		List<DataPb.RecordData> list = ser.getRecordList();
		for (DataPb.RecordData e : list) {
			records.add(new LuckPoolRewardRecord(e));
			if (records.size() > 20) {
				records.remove(0);
			}
		}
	}

	public byte[] serCampMembers() {
		SerializePb.SerCampMembers.Builder ser = SerializePb.SerCampMembers.newBuilder();
		for (LinkedList<CampMembersRank> value : campMembers.values()) {
			for (CampMembersRank campMembersRank : value) {
				ser.addCampMembers(campMembersRank.serCampMembers());
			}
		}
		return ser.build().toByteArray();
	}

	public void dserCampMembers(byte[] data) throws InvalidProtocolBufferException {
		if (data == null) {
			return;
		}
		SerializePb.SerCampMembers ser = SerializePb.SerCampMembers.parseFrom(data);
		List<CampMemberDate> list = ser.getCampMembersList();
		for (DataPb.CampMemberDate e : list) {
			if (e.getRank() <= 0) {
				continue;
			}
			addCampMembersRank(e);
		}
	}


	/**
	 * 分页拉取排名
	 *
	 * @param page
	 * @return
	 */
	public LinkedList<ActPlayerRank> getPlayerRankList(int page) {
		LinkedList<ActPlayerRank> rs = new LinkedList<ActPlayerRank>();
		int[] pages = {page * 20, (page + 1) * 20};
		try {
			lock.readLock().lock();
			Iterator<ActPlayerRank> it = ranks.iterator();
			int count = 0;
			while (it.hasNext()) {
				ActPlayerRank next = it.next();
				if (count >= pages[0]) {
					rs.add(next);
				}
				if (++count >= pages[1]) {
					break;
				}
			}
		} catch (Exception e) {
		} finally {
			lock.readLock().unlock();
		}
		return rs;

	}

	/**
	 * 获取在rank位玩家
	 *
	 * @param rank
	 * @return
	 */
	public ActPlayerRank getActRank(int rank) {
		int size = actRanks.size();
		if (rank > size) {
			return null;
		}
		int index = rank - 1;
		try {
			lock.readLock().lock();
			return ranks.get(index);
		} catch (Exception e) {
		} finally {
			lock.readLock().unlock();
		}
		return null;
	}

	/**
	 * 获取用户的排名
	 *
	 * @param lordId
	 * @return
	 */
	public ActPlayerRank getLordRank(long lordId) {
		int rank = 0;
		try {
			lock.writeLock().lock();
			Iterator<ActPlayerRank> it = ranks.iterator();
			while (it.hasNext()) {
				rank++;
				ActPlayerRank next = it.next();
				if (next.getLordId() == lordId) {
					next.setRank(rank);
					return next;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.writeLock().unlock();
		}
		return null;
	}

	public ActPlayerRank getLordCostRank(long lordId, int mk) {
		int rank = 0;
		lock.writeLock().lock();
		try {
			Iterator<ActPlayerRank> it = ranks.iterator();
			while (it.hasNext()) {
				rank++;
				ActPlayerRank next = it.next();
				if (next.getLordId() == lordId && next.getRankValue() >= mk) {
					next.setRank(rank);
					return next;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.writeLock().unlock();
		}
		return null;
	}


	/**
	 * 添加排名
	 *
	 * @param lordId
	 * @param value
	 * @param
	 * @param
	 * @return
	 */
	public void addPlayerRank(long lordId, long value) {
		if (value == 0) {
			return;
		}
		lock.writeLock().lock();
		try {
			if (actRanks.containsKey(lordId)) {
				ActPlayerRank actRank = actRanks.get(lordId);
				actRank.setRankValue(value);
			} else {
				ActPlayerRank actRank = new ActPlayerRank(lordId, value, System.currentTimeMillis());
				actRanks.put(lordId, actRank);
			}
			ranks = actRanks.values().stream()
				.sorted(Comparator.comparingLong(ActPlayerRank::getRankValue).reversed()//先根据值
					.thenComparing(ActPlayerRank::getTime)                //再根据时间
					.thenComparing(ActPlayerRank::getLordId))             //最后根据用户ID
				.collect(Collectors.toCollection(LinkedList::new));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void addRank(long lordId, long value) {
		if (value == 0) {
			return;
		}
		lock.writeLock().lock();
		try {
			if (actRanks.containsKey(lordId)) {
				ActPlayerRank actRank = actRanks.get(lordId);
				actRank.setRankValue(actRank.getRankValue() + value);
			} else {
				ActPlayerRank actRank = new ActPlayerRank(lordId, value, System.currentTimeMillis());
				actRanks.put(lordId, actRank);
			}
			ranks = actRanks.values().stream()
				.sorted(Comparator.comparingLong(ActPlayerRank::getRankValue).reversed()//先根据值
					.thenComparing(ActPlayerRank::getTime)                //再根据时间
					.thenComparing(ActPlayerRank::getLordId))             //最后根据用户ID
				.collect(Collectors.toCollection(LinkedList::new));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 最高排名
	 *
	 * @param lordId
	 * @param value
	 * @param max
	 */
	public void addPlayerRank(long lordId, int max, long value) {
		try {
			lock.writeLock().lock();
			if (actRanks.containsKey(lordId)) {
				ActPlayerRank actRank = actRanks.get(lordId);
				actRank.setRankValue(value);
			} else {
				ActPlayerRank actRank = new ActPlayerRank(lordId, value, System.currentTimeMillis());
				actRanks.put(lordId, actRank);
			}
			ranks = actRanks.values().stream()
				.sorted(Comparator.comparingLong(ActPlayerRank::getRankValue).reversed()//先根据值
					.thenComparing(ActPlayerRank::getTime)                //再根据时间
					.thenComparing(ActPlayerRank::getLordId))             //最后根据用户ID
				.collect(Collectors.toCollection(LinkedList::new));
			//超过排名了 移除最后一位
			if (ranks.size() >= max) {
				ranks.removeLast();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public long getAddtion(long id) {
		Long v = addtions.get(id);
		if (v == null) {
			return 0L;
		} else {
			return v.longValue();
		}
	}

	public void putAddtion(long id, long v) {
		addtions.put(id, v);
	}

	public LinkedList<LuckPoolRewardRecord> getRecords() {
		return records;
	}

	public void addRewardRecord(LuckPoolRewardRecord rewardRecord) {
		records.add(rewardRecord);
		if (records.size() > 20) {
			records.remove(0);
		}
	}


	/**
	 * @Description 阵营骨干添加排名
	 * @Date 2021/3/15 13:57
	 * @Param [lordId, value]
	 * @Return
	 **/
	public void addCampMembersRank(DataPb.CampMemberDate e) {
		CampMembersRank campMembersRank = new CampMembersRank(e);
		LinkedList<CampMembersRank> linkedList = campMembers.computeIfAbsent(e.getCountry(), x -> new LinkedList<>());
		linkedList.add(campMembersRank);
	}

	/**
	 * @Description 刷新阵营骨干排名
	 * @Date 2021/3/18 20:35
	 * @Param [country]
	 * @Return
	 **/
	public void refreshCampMembersRank(int country) {
		LinkedList<CampMembersRank> linkedList = campMembers.computeIfAbsent(country, x -> new LinkedList<>());
		for (Player player : SpringUtil.getBean(PlayerManager.class).getPlayers().values()) {
			if (player.getCountry() != country || player.getBattleScore() < 10000) {
				continue;
			}
			CampMembersRank campMembersRankBak = new CampMembersRank(player);
			if (linkedList.isEmpty()) {
				linkedList.add(campMembersRankBak);
			} else {
				boolean flag = false;
				CampMembersRank campMembersRank = null;
				for (CampMembersRank c : linkedList) {
					if (c.getLordId().longValue() == player.roleId.longValue()) {
						campMembersRank = c;
						flag = true;
						break;
					}
				}
				if (flag) {
					if (player.getBattleScore() > campMembersRank.getFightMax()) {
						campMembersRank.setFightMax(player.getBattleScore());
						campMembersRank.setFight(player.getBattleScore());
					} else {
						campMembersRank.setFight(player.getBattleScore());
					}
				} else {
					linkedList.add(campMembersRankBak);
				}
			}
		}
		Collections.sort(linkedList, new ComparatorCampMembers());
		for (int i = 0; i < linkedList.size(); i++) {
			CampMembersRank campMembersRank = linkedList.get(i);
			campMembersRank.setRank(i + 1);
		}
	}

	public LinkedList<CampMembersRank> getCampMembers(int country) {
		return campMembers.computeIfAbsent(country, e -> new LinkedList<>());
	}

	/**
	 * 获取阵营骨干用户的排名
	 *
	 * @param
	 * @return
	 */
	public CampMembersRank getCampMembersRank(Player player) {
		try {
			lock.writeLock().lock();
			List<CampMembersRank> list = getCampMembers(player.getCountry());
			for (CampMembersRank campMembersRank : list) {
				if (campMembersRank.getLordId().longValue() == player.roleId.longValue()) {
					return campMembersRank;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.writeLock().unlock();
		}
		return null;
	}

	class ComparatorCampMembers implements Comparator<CampMembersRank> {

		@Override
		public int compare(CampMembersRank o1, CampMembersRank o2) {
			// by title
			if (o1.getFightMax() < o2.getFightMax()) {
				return 1;
			}

			if (o1.getFightMax() > o2.getFightMax()) {
				return -1;
			}

			// by score
			if (o1.getFight() < o2.getFight()) {
				return 1;
			}

			if (o1.getFight() > o2.getFight()) {
				return -1;
			}
			return 0;
		}
	}
}

