package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticApprenticeAward;
import com.game.domain.s.StaticApprenticeRank;
import com.game.domain.s.StaticFriendshipScoreShop;
import com.game.domain.s.StaticMentorAward;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liyue
 */
@Component
public class StaticFriendMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao staticDataDao;

    //拜师奖励
    private Map<Integer, StaticMentorAward> menterAwards = new HashMap<>();

    //积分商店
    private Map<Integer, StaticFriendshipScoreShop> friendshipScoreShop = new HashMap<>();

    //徒弟个数奖励列表
    private Map<Integer, StaticApprenticeAward> apprenticeAward = new HashMap<>();

    //徒弟个数奖励列表
    private Map<Integer, StaticApprenticeRank> apprenticeRankMap = new HashMap<>();



    @Override
    public void init() throws Exception{
        initMentorAwards();
        initApprenticeAward();
        initFriendshipScoreShop();
        initApprenticeRank();
    }

    private void initApprenticeAward() {
        if (apprenticeAward.size() > 0) {
            apprenticeAward.clear();
        }
        apprenticeAward.putAll(staticDataDao.selectApprenticeAward());
    }

    private void initFriendshipScoreShop() {
        if (friendshipScoreShop.size() > 0) {
            friendshipScoreShop.clear();
        }
        friendshipScoreShop.putAll(staticDataDao.selectFriendshipScoreShop());
    }

    private void initMentorAwards(){
        if (menterAwards.size() > 0) {
            menterAwards.clear();
        }
        menterAwards.putAll(staticDataDao.selectMentorAward());
    }

    private void initApprenticeRank(){
        if (apprenticeRankMap.size() > 0) {
            apprenticeRankMap.clear();
        }
        apprenticeRankMap.putAll(staticDataDao.selectMentorRank());
    }

    public Map<Integer, StaticMentorAward> getMenterAwards() {
        return menterAwards;
    }

    public Map<Integer, StaticFriendshipScoreShop> getFriendshipScoreShop() {
        return friendshipScoreShop;
    }

    public Map<Integer, StaticApprenticeAward> getApprenticeAward() {
        return apprenticeAward;
    }

    public Map<Integer, StaticApprenticeRank> getApprenticeRankMap() {
        return apprenticeRankMap;
    }
}
