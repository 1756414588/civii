package com.game.manager;

import com.game.Loading;
import com.game.dao.p.PublicDataDao;
import com.game.define.LoadData;
import com.game.domain.Player;
import com.game.domain.p.EndlessTDRank;
import com.game.domain.p.PublicData;
import com.game.pb.CommonPb.TDRank;
import com.game.pb.CommonPb.TDRankInfo;
import com.game.pb.SerializePb.EndlessTDRankInfo;
import com.game.spring.SpringUtil;
import com.game.util.StringUtil;
import com.game.util.TimeHelper;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/2 11:53
 **/
@Getter
@Setter
@Component
@LoadData(name = "塔防管理", type = Loading.LOAD_USER_DB, initSeq = 3000)
public class PublicDataManager extends BaseManager {

	@Autowired
	private PublicDataDao publicDataDao;
	@Autowired
	private TDManager tdManager;

	private Map<Integer, PublicData> publicDataMap = new ConcurrentHashMap<>();

	@Override
	public void load() throws InvalidProtocolBufferException {
		PublicData publicData = publicDataDao.queryPublicData(1);
		if (publicData == null) {
			publicData = new PublicData(1);
			publicDataDao.update(publicData);
		}
		publicDataMap.put(1, publicData);
	}

	@Override
	public void init() throws InvalidProtocolBufferException {
		dserEndlessTDInfo(getPublicData());
	}

	public PublicData getPublicData() {
		return publicDataMap.computeIfAbsent(1, x -> new PublicData(1));
	}

	// 每个小时 数据入库
	public void logic() {
		long now = System.currentTimeMillis();
		if ((getPublicData().getLastSaveTime() - now) > TimeHelper.HOUR_MS) {
			update();
		}
	}

	public void update() {
		getPublicData().setLastSaveTime(System.currentTimeMillis());
		getPublicData().setEndlessTDRank(serEndlessTDInfo().build().toByteArray());
		publicDataDao.update(getPublicData());
	}

	public void dserEndlessTDInfo(PublicData publicData) throws InvalidProtocolBufferException {
		if (publicData == null || StringUtil.isNullOrEmpty(publicData.getEndlessTDRank())) {
			return;
		}
		EndlessTDRankInfo endlessTDRankInfo = EndlessTDRankInfo.parseFrom(publicData.getEndlessTDRank());
		// 反序列化排行信息
		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		List<EndlessTDRank> weekEndlessTDRanks = tdManager.getWeekEndlessTDRanks();
		Map<Integer, List<EndlessTDRank>> historyEndlessTDRanks = tdManager.getHistoryEndlessTDRanks();
		endlessTDRankInfo.getTDRankInfoList().forEach(e -> {
			int rankDate = Integer.valueOf(e.getRankdate());
			List<TDRank> tdRankList = e.getTdRankList();
			tdRankList.forEach(x -> {
				EndlessTDRank endlessTDRank = new EndlessTDRank();
				endlessTDRank.setRank(x.getRank());
				endlessTDRank.setWeekMaxFraction(x.getScore());
				endlessTDRank.setLordId(Long.valueOf(x.getNick()));
				endlessTDRank.setHistoryMaxFraction(x.getCountry());
				Player player = playerManager.getPlayer(endlessTDRank.getLordId());
				if (player != null) {
					endlessTDRank.setPlayer(player);
					if (rankDate == 0) {
						weekEndlessTDRanks.add(endlessTDRank);
					} else {
						historyEndlessTDRanks.computeIfAbsent(rankDate, y -> new LinkedList<>()).add(endlessTDRank);
					}
				}
			});
		});
	}

	public EndlessTDRankInfo.Builder serEndlessTDInfo() {
		EndlessTDRankInfo.Builder builder = EndlessTDRankInfo.newBuilder();
		// 序列化排行信息
		tdManager.getHistoryEndlessTDRanks().forEach((k, v) -> {
			TDRankInfo.Builder b = TDRankInfo.newBuilder();
			b.setRankdate(String.valueOf(k));
			v.forEach(x -> {
				TDRank.Builder u = TDRank.newBuilder();
				u.setRank(x.getRank());
				u.setScore(x.getWeekMaxFraction());
				u.setNick(String.valueOf(x.getLordId()));
				u.setCountry(x.getHistoryMaxFraction());
				b.addTdRank(u);
			});
			builder.addTDRankInfo(b);
		});
		TDRankInfo.Builder e = TDRankInfo.newBuilder();
		e.setRankdate("0");
		tdManager.getWeekEndlessTDRanks().forEach(x -> {
			TDRank.Builder u = TDRank.newBuilder();
			u.setRank(x.getRank());
			u.setScore(x.getWeekMaxFraction());
			u.setNick(String.valueOf(x.getLordId()));
			u.setCountry(x.getHistoryMaxFraction());
			e.addTdRank(u);
		});
		builder.addTDRankInfo(e);
		return builder;
	}
}
