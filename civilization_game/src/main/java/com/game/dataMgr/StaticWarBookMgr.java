package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.p.ConfigException;
import com.game.domain.s.*;
import com.google.common.collect.HashBasedTable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author CaoBing
 * @date 2020/12/8 16:25
 */
@Component
@LoadData(name = "兵书")
public class StaticWarBookMgr extends BaseDataMgr {
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 按照兵书类型存放
     */
    private Map<Integer, List<StaticWarBook>> warBookMapForType = new HashMap<Integer, List<StaticWarBook>>();
    /**
     * 按照兵书品质存放
     */
    private Map<Integer, List<StaticWarBook>> warBookMapForQuality = new HashMap<Integer, List<StaticWarBook>>();
    /**
     * 按照兵书ID存放
     */
    private Map<Integer, StaticWarBook> warBookMap = new HashMap<Integer, StaticWarBook>();
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 按照兵书基础属性ID存放
     */
    private Map<Integer, StaticWarBookBaseProperty> warBookBasePropertyMap = new HashMap<Integer, StaticWarBookBaseProperty>();
    /**
     * 按照兵书基础属性类型AND等级存放
     */
    private HashBasedTable<Integer, Integer, StaticWarBookBaseProperty> warBookBasePropertyMapForTypeAndLev = HashBasedTable.create();
    /**
     * 按照兵书技能ID存放
     */
    private Map<Integer, StaticWarBookSkill> warBookSkillMap = new HashMap<Integer, StaticWarBookSkill>();
    /**
     * 按照兵书基础技能类型AND等级存放
     */
    private HashBasedTable<Integer, Integer, StaticWarBookSkill> warBookSkillMapForTypeAndLev = HashBasedTable.create();
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 兵书掉落配置
     */
    private Map<Integer, StaticWarBookDrop> warBookDropMap = new HashMap<Integer, StaticWarBookDrop>();
    /**
     * 兵书掉落配置按照品质
     */
    private Map<Integer, List<StaticWarBookDrop>> warBookDropMapForQuality = new HashMap<Integer, List<StaticWarBookDrop>>();

    List<StaticWarBookDrop> staticWarBookDrops = new ArrayList<>();
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 兵书分解配置按照品质
     */
    private HashBasedTable<Integer, Integer, StaticWarBookDecom> warBookDecomMapForQualityAndLevle = HashBasedTable.create();
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 兵书商城类型配置
     */
    private Map<Integer, StaticWarBookBuy> warBookBuyMap = new HashMap<Integer, StaticWarBookBuy>();
    /**
     * 兵书商城物品配置
     */
    private Map<Integer, StaticWarBookShopGoods> warBookShopGoodMap = new HashMap<Integer, StaticWarBookShopGoods>();

    private Map<Integer, List<StaticWarBookShopGoods>> warBookShopGoodMapForType = new HashMap<Integer, List<StaticWarBookShopGoods>>();
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 兵书兑换配置
     */
    private Map<Integer, StaticWarBookExchange> warBookExchangeMap = new HashMap<Integer, StaticWarBookExchange>();

    private Map<Integer, StaticBookSkillEffectType> bookSkillEffectTypeMap = new HashMap<Integer, StaticBookSkillEffectType>();

    @Getter
    @Setter
    private Map<Integer,List<StaticWarBookSkill>> skillForTypeMap = new HashMap<>();

    @Autowired
    private StaticDataDao staticDataDao;

    @Override
    public void load() throws Exception {
        clearConfig();

        initWarBooks();
        initWarBookBaseProperty();
        initWarBookSkill();
        initWarBookDrop();
        initWarBookDecom();
        initWarBookBuy();
        initWarBookExchange();
        initWarBookSkillType();
    }

    @Override
    public void init() throws Exception{
    }

    /**
     * 清空兵书配置数据
     */
    private void clearConfig() {
        warBookMapForType.clear();
        warBookMapForQuality.clear();
        warBookMap.clear();

        warBookBasePropertyMap.clear();
        warBookBasePropertyMapForTypeAndLev.clear();

        warBookSkillMap.clear();
        warBookSkillMapForTypeAndLev.clear();

        staticWarBookDrops.clear();
        warBookDropMap.clear();
        warBookDropMapForQuality.clear();

        warBookDecomMapForQualityAndLevle.clear();

        warBookBuyMap.clear();
        warBookShopGoodMap.clear();
        warBookShopGoodMapForType.clear();
        warBookExchangeMap.clear();
        bookSkillEffectTypeMap.clear();
    }

    /**
     * 初始化兵书配置数据
     */
    private void initWarBooks() throws ConfigException {
        List<StaticWarBook> staticWarBooks = staticDataDao.selectStaticWarBooks();
        if (staticWarBooks != null && staticWarBooks.size() > 0) {
            for (StaticWarBook staticWarBook : staticWarBooks) {
                if (null != staticWarBook) {
                    List<List<Integer>> baseProperty = staticWarBook.getBaseProperty();
                    for (List<Integer> integers : baseProperty) {
                        if (integers.size() != 2) {
                            throw new ConfigException("baseProperty is size != 2");
                        }
                    }

                    List<List<Integer>> skill = staticWarBook.getSkill();
                    for (List<Integer> integers : skill) {
                        if (integers.size() != 2) {
                            throw new ConfigException("skill is size != 2");
                        }
                    }

                    List<List<Integer>> randSkillNum = staticWarBook.getRandSkillNum();
                    for (List<Integer> integers : randSkillNum) {
                        if (integers.size() != 2) {
                            throw new ConfigException("randSkillNum is size != 2");
                        }
                    }

                    int type = staticWarBook.getType();
                    List<StaticWarBook> staticWarBookForType = warBookMapForType.get(type);
                    if (null == staticWarBookForType) {
                        staticWarBookForType = new ArrayList<>();
                        warBookMapForType.put(type, staticWarBookForType);
                    }

                    int quality = staticWarBook.getQuality();
                    List<StaticWarBook> staticWarBookForQuality = warBookMapForQuality.get(quality);
                    if (null == staticWarBookForQuality) {
                        staticWarBookForQuality = new ArrayList<>();
                        warBookMapForQuality.put(quality, staticWarBookForQuality);
                    }

                    staticWarBookForType.add(staticWarBook);
                    staticWarBookForQuality.add(staticWarBook);
                    warBookMap.put(staticWarBook.getId(), staticWarBook);
                }
            }
        }
    }

    /**
     * 初始化兵书基础属性数据
     */
    private void initWarBookBaseProperty() throws ConfigException {
        List<StaticWarBookBaseProperty> staticWarBookBasePropertys = staticDataDao.selectWarBookBaseProperty();
        if (staticWarBookBasePropertys != null && staticWarBookBasePropertys.size() > 0) {
            for (StaticWarBookBaseProperty staticWarBookBaseProperty : staticWarBookBasePropertys) {
                if (null != staticWarBookBaseProperty) {

                    List<List<Integer>> affect = staticWarBookBaseProperty.getAffect();
                    for (List<Integer> integers : affect) {
                        if (integers.size() != 2) {
                            throw new ConfigException("affect is size != 2");
                        }
                    }

                    warBookBasePropertyMap.put(staticWarBookBaseProperty.getId(), staticWarBookBaseProperty);
                    warBookBasePropertyMapForTypeAndLev.put(staticWarBookBaseProperty.getBasePropType(), staticWarBookBaseProperty.getLevel(), staticWarBookBaseProperty);
                }
            }
        }
    }

    /**
     * 初始化兵书技能
     */
    private void initWarBookSkill() throws ConfigException {
        List<StaticWarBookSkill> staticWarBookSkills = staticDataDao.selectWarBookSkill();
        if (staticWarBookSkills != null && staticWarBookSkills.size() > 0) {
            for (StaticWarBookSkill staticWarBookSkill : staticWarBookSkills) {
                warBookSkillMap.put(staticWarBookSkill.getId(), staticWarBookSkill);
                warBookSkillMapForTypeAndLev.put(staticWarBookSkill.getSkillType(), staticWarBookSkill.getLevel(), staticWarBookSkill);
                List<StaticWarBookSkill> staticWarBookSkills1 = skillForTypeMap.computeIfAbsent(staticWarBookSkill.getSkillType(), x -> new ArrayList<>());
                staticWarBookSkills1.add(staticWarBookSkill);
            }
        }
    }


    /**
     * 初始化兵书掉落配置
     */
    private void initWarBookDrop() {
        staticWarBookDrops = staticDataDao.selectWarBookDrop();
        if (null != staticWarBookDrops && staticWarBookDrops.size() != 0) {
            for (StaticWarBookDrop staticWarBookDrop : staticWarBookDrops) {
                warBookDropMap.put(staticWarBookDrop.getId(), staticWarBookDrop);
            }
        }
    }

    /**
     * 兵书分解配置
     */
    private void initWarBookDecom() {
        List<StaticWarBookDecom> staticWarBookDecoms = staticDataDao.selectWarBookDecom();
        if (null != staticWarBookDecoms && staticWarBookDecoms.size() > 0) {
            for (StaticWarBookDecom staticWarBookDecom : staticWarBookDecoms) {
                if (null != staticWarBookDecom) {
                    warBookDecomMapForQualityAndLevle.put(staticWarBookDecom.getQuality(), staticWarBookDecom.getLevel(), staticWarBookDecom);
                }
            }
        }
    }

    /**
     * 兵书商城配置
     */
    private void initWarBookBuy() {
        List<StaticWarBookBuy> staticWarBookBuys = staticDataDao.selectWarBookBuy();
        if (null != staticWarBookBuys && staticWarBookBuys.size() > 0) {
            for (StaticWarBookBuy staticWarBookBuy : staticWarBookBuys) {
                if (null != staticWarBookBuy) {
                    warBookBuyMap.put(staticWarBookBuy.getType(), staticWarBookBuy);
                }
            }
        }

        List<StaticWarBookShopGoods> staticWarBookShopGoods = staticDataDao.selectWarBookShopGoods();
        if (null != staticWarBookShopGoods && staticWarBookShopGoods.size() > 0) {
            for (StaticWarBookShopGoods staticWarBookShopGood : staticWarBookShopGoods) {
                if (null != staticWarBookShopGood) {

                    int type = staticWarBookShopGood.getType();
                    List<StaticWarBookShopGoods> staticWarBookShopGoodsList = warBookShopGoodMapForType.get(type);
                    if (staticWarBookShopGoodsList == null) {
                        staticWarBookShopGoodsList = new ArrayList<>();
                    }
                    warBookShopGoodMapForType.put(type, staticWarBookShopGoodsList);
                    staticWarBookShopGoodsList.add(staticWarBookShopGood);
                    warBookShopGoodMap.put(staticWarBookShopGood.getId(), staticWarBookShopGood);
                }
            }
        }
    }

    /**
     * 兵书物品兑换配置
     */
    private void initWarBookExchange() {
        List<StaticWarBookExchange> staticWarBookExchanges = staticDataDao.selectWarBookExchanges();
        if (null != staticWarBookExchanges && staticWarBookExchanges.size() > 0) {
            for (StaticWarBookExchange staticWarBookExchange : staticWarBookExchanges) {
                if (null != staticWarBookExchange) {
                    warBookExchangeMap.put(staticWarBookExchange.getId(), staticWarBookExchange);
                }
            }
        }
    }

    /**
     * 初始化兵书技能影响值类型
     */
    private void initWarBookSkillType(){
        List<StaticBookSkillEffectType> staticBookSkillTypes = staticDataDao.selectBookSkillEffectType();
        if(null != staticBookSkillTypes && staticBookSkillTypes.size() > 0){
            for (StaticBookSkillEffectType staticBookSkillType : staticBookSkillTypes) {
                if(null != staticBookSkillType){
                    bookSkillEffectTypeMap.put(staticBookSkillType.getId(),staticBookSkillType);
                }
            }
        }
    }

    /**
     * 通过Id获取对应的兵书
     *
     * @param bookId
     * @return
     */
    public StaticWarBook getWarBookConfigById(int bookId) {
        return warBookMap.get(bookId);
    }

    public List<StaticWarBook> getWarBookConfigByQuality(int quality) {
        return warBookMapForQuality.get(quality);
    }

    public Map<Integer, StaticWarBookDrop> getWarBookDropMap() {
        return warBookDropMap;
    }

    public List<StaticWarBookDrop> getWarBookDrops() {
        return staticWarBookDrops;
    }

    public StaticWarBookBaseProperty getWarBookBasePropById(int basePropId) {
        return warBookBasePropertyMap.get(basePropId);
    }

    public StaticWarBookBaseProperty getWarBookBasePropByTypeAndlev(int basePropType, int lev) {
        return warBookBasePropertyMapForTypeAndLev.get(basePropType, lev);
    }

    public StaticWarBookSkill getWarBookSkillById(int skillId) {
        return warBookSkillMap.get(skillId);
    }

    public StaticWarBookSkill getWarBookSkillByTypeAndLev(int skillType, int lev) {
        return warBookSkillMapForTypeAndLev.get(skillType, lev);
    }

    public StaticWarBookDecom getWarBookWarBookDecom(int quality, int level) {
        return warBookDecomMapForQualityAndLevle.get(quality, level);
    }

    public int getBookType(int bookId) {
        StaticWarBook staticWarBook = warBookMap.get(bookId);
        if (staticWarBook == null) {
            return 0;
        }
        return staticWarBook.getType();
    }

    public List<Integer> getSoldierSkillLev1() {
        List<Integer> soldierSkillLev1s = new ArrayList<Integer>();
        Iterator<StaticWarBookSkill> iterator = warBookSkillMap.values().iterator();
        while (iterator.hasNext()) {
            StaticWarBookSkill next = iterator.next();
            if (next != null && next.getIsSoldierSkill() == 1 && next.getLevel() == 1) {
                soldierSkillLev1s.add(next.getId());
            }
        }
        return soldierSkillLev1s;
    }

    public int getSkillType(int skillId) {
        StaticWarBookSkill staticWarBookSkill = warBookSkillMap.get(skillId);
        if (staticWarBookSkill == null) {
            return 0;
        }
        return staticWarBookSkill.getSkillType();
    }

    public Map<Integer, StaticWarBookBuy> getWarBookBuy() {
        return warBookBuyMap;
    }

    public StaticWarBookBuy getWarBookBuyLast() {
        Collection<StaticWarBookBuy> values = warBookBuyMap.values();
        for (StaticWarBookBuy value : values) {
            if (null != value && value.getLast() == 1) {
                return value;
            }
        }
        return null;
    }

    public List<StaticWarBookShopGoods> getStaticWarBookShopGoodsByType(int type) {
        return warBookShopGoodMapForType.get(type);
    }

    public StaticWarBookShopGoods getStaticWarBookShopGoodsById(int id) {
        return warBookShopGoodMap.get(id);
    }

    public StaticWarBookExchange getStaticWarBookExchange(int id){
        return warBookExchangeMap.get(id);
    }

    public Map<Integer,StaticWarBookExchange> getStaticWarBookExchangeMap(){
        return warBookExchangeMap;
    }

    public StaticBookSkillEffectType getBookSkillEffectType(int effectType){
        return bookSkillEffectTypeMap.get(effectType);
    }

    public StaticWarBookSkill getWarBookSkillByRandomType(int skillType) {
        switch (skillType) {
            case 11:
                List<StaticWarBookSkill> warBookSkillByType = this.getWarBookSkillByType(skillType);
                Random random = new Random();
                int index = random.nextInt(warBookSkillByType.size());
                return warBookSkillByType.get(index);
            default:
                return getWarBookSkillByTypeAndLev(skillType,1);
        }
    }

    public List<StaticWarBookSkill> getWarBookSkillByType(int skillType) {
        return skillForTypeMap.get(skillType);
    }
}
