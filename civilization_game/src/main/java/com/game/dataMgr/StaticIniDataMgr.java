package com.game.dataMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticIniLord;
import com.game.domain.s.StaticInitName;
import com.game.domain.s.StaticLoginAward;
import com.game.domain.s.StaticPortrait;
import com.game.manager.PlayerManager;
import com.game.pb.CommonPb;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.RandomHelper;

@Component
public class StaticIniDataMgr extends BaseDataMgr {

    private StaticLoginAward staticLoginAward;

    @Autowired
    private StaticDataDao staticDataDao;

    @Autowired
    private PlayerManager playerManager;

    // 随机角色名
    private List<StaticInitName> initNameMap = new ArrayList<StaticInitName>();

    private List<String> listName1 = new ArrayList<String>();
    private List<String> listName2 = new ArrayList<String>();
    private List<String> listName3 = new ArrayList<String>();

    @Getter
    private Map<Integer, StaticPortrait> portraitMap = new HashMap<Integer, StaticPortrait>();
    private StaticIniLord staticIniLord;

    @Override
    public void init() throws Exception{
        staticLoginAward = staticDataDao.selectLoginAward();
        List<List<Integer>> awardList = staticLoginAward.getAwardList();
        List<CommonPb.Award> alist = new ArrayList<CommonPb.Award>();
        for (List<Integer> e : awardList) {
            if (e.size() < 3) {
                continue;
            }
            alist.add(PbHelper.createAward(e.get(0), e.get(1), e.get(2)).build());
        }
        staticLoginAward.setAwardPbList(alist);
        initNameMap = staticDataDao.selectInitName();
        listName1.clear();
        listName2.clear();
        listName3.clear();
        checkInitNameNum();
        portraitMap = staticDataDao.selectPortraitMap();
        staticIniLord = staticDataDao.selectLord();
    }

    public StaticIniLord getLordIniData() {
        //return staticDataDao.selectLord();
        return staticIniLord;
    }

    public StaticLoginAward getStaticLoginAward() {
        return staticLoginAward;
    }

    public void checkInitNameNum() {
        for (StaticInitName initName : initNameMap) {
            String name1 = initName.getName1();
            if (name1 != null && !name1.isEmpty()) {
                listName1.add(name1);
            }

            String name2 = initName.getName2();
            if (name2 != null && !name2.isEmpty()) {
                listName2.add(name2);

            }

            String name3 = initName.getName3();
            if (name3 != null && !name3.isEmpty()) {
                listName3.add(name3);
            }
        }

        if (listName1.size() <= 0) {
            LogHelper.CONFIG_LOGGER.error("listName1.size() <= 0");
        }

        if (listName2.size() <= 0) {
            LogHelper.CONFIG_LOGGER.error("listName2.size() <= 0");
        }

        if (listName3.size() <= 0) {
            LogHelper.CONFIG_LOGGER.error("listName3.size() <= 0");
        }

        //System.out.println("name1 = " + listName1.size() + ", name2 = " + listName2.size() + ", name3 =" + listName3.size());

    }

    public String randName() {
        // 开始随机角色
        int tryTimes = 0;
        while (tryTimes < 5) {
            String myName = "";
            myName += listName1.get(RandomHelper.threadSafeRand(0, listName1.size() - 1));
            myName += listName2.get(RandomHelper.threadSafeRand(0, listName2.size() - 1));
            myName += listName3.get(RandomHelper.threadSafeRand(0, listName3.size() - 1));
            boolean isTaken = playerManager.takeNick(myName);
            if (!isTaken) {
                //LogHelper.GAME_DEBUG.error("随机角色名=" + myName);
                return myName;
            } else {
                ++tryTimes;
            }
        }

        return "null";

    }

    public StaticPortrait getPortrait(int portrait) {
        return portraitMap.get(portrait);
    }

}
