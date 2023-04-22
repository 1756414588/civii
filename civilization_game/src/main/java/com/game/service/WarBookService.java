package com.game.service;

import com.game.constant.AwardType;
import com.game.constant.GameError;
import com.game.constant.Reason;
import com.game.dataMgr.StaticWarBookMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticWarBook;
import com.game.domain.s.StaticWarBookBaseProperty;
import com.game.domain.s.StaticWarBookDecom;
import com.game.domain.s.StaticWarBookExchange;
import com.game.log.domain.WarBookLog;
import com.game.log.domain.WearBookLog;
import com.game.manager.HeroManager;
import com.game.manager.PlayerManager;
import com.game.manager.WarBookManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.WarBookPb;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author CaoBing
 * @date 2020/12/9 14:38
 */
@Service
public class WarBookService {

    @Autowired
    private StaticWarBookMgr staticWarBookMgr;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private HeroManager heroDataManager;

    @Autowired
    private WarBookManager warBookManager;

    @Autowired
    private com.game.log.LogUser logUser;

    /**
     * 查看玩家兵书背包
     *
     * @param handler
     */
    public void getWarBookBagRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        Map<Integer, WarBook> warBooks = player.getWarBooks();
        WarBookPb.GetWarBookBagRs.Builder builder = WarBookPb.GetWarBookBagRs.newBuilder();
        for (Map.Entry<Integer, WarBook> item : warBooks.entrySet()) {
            if (item == null) {
                continue;
            }
            WarBook book = item.getValue();
            if (book == null) {
                continue;
            }
            CommonPb.WarBook.Builder wrapBook = book.wrapPb();
            builder.addBook(wrapBook);
        }

        Map<Integer, Item> items = player.getItemMap();
        Item item1 = items.get(226);
        Item item2 = items.get(227);
        int item1Num = item1 == null ? 0 : item1.getItemNum();//残卷
        int item2Num = item2 == null ? 0 : item2.getItemNum();//荣誉
        builder.setItem1(item1Num);
        builder.setItem2(item2Num);

        handler.sendMsgToPlayer(GameError.OK, WarBookPb.GetWarBookBagRs.ext, builder.build());
    }

    /**
     * 兵书上锁请求
     *
     * @param req
     * @param handler
     */
    public void lockWarBookRq(WarBookPb.LockWarBookRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        // 检查物品是否存在
        int keyId = req.getKeyId();
        int heroId = req.getHeroId();

        if (keyId == 0) {
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK);
            return;
        }

        WarBook book = null;
        if (heroId == 0) {
            Map<Integer, WarBook> warBooks = player.getWarBooks();
            // 兵书不存在
            book = warBooks.get(keyId);
        } else {
            HashMap<Integer, Hero> heros = player.getHeros();
            Hero hero = heros.get(heroId);
            ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
            if (null != heroBooks) {
                for (HeroBook heroBook : heroBooks) {
                    WarBook warBook = heroBook.getBook();
                    if (warBook.getKeyId() == keyId) {
                        book = warBook;
                    }
                }
            }
        }

        if (book == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK);
            return;
        }
        // 找到兵书
        int bookId = book.getBookId();
        // 查找配置
        StaticWarBook staticWarBook = staticWarBookMgr.getWarBookConfigById(bookId);
        if (staticWarBook == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK_CONFIG);
            return;
        }
        //设置兵书上锁的属性值
        int isLock = book.getIsLock();
        if (isLock == 1) {
            book.setIsLock(0);
        } else {
            book.setIsLock(1);
        }
        WarBookPb.LockWarBookRs.Builder builder = WarBookPb.LockWarBookRs.newBuilder();
        CommonPb.WarBook.Builder wrapBook = book.wrapPb();
        builder.setBook(wrapBook);
        handler.sendMsgToPlayer(GameError.OK, WarBookPb.LockWarBookRs.ext, builder.build());
    }

    /**
     * 分解兵书请求
     *
     * @param req
     * @param handler
     */
    public void decompoundWarBookRq(WarBookPb.DecompoundWarBookRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        // 检查物品是否存在
        List<Integer> keyIdList = req.getKeyIdList();
        Map<Integer, WarBook> warBooks = player.getWarBooks();
        // 兵书不存在
        WarBookPb.DecompoundWarBookRs.Builder builder = WarBookPb.DecompoundWarBookRs.newBuilder();
        List<CommonPb.Award> awardList = new ArrayList<>();

        for (Integer keyId : keyIdList) {
            builder.addKeyId(keyId);
            WarBook book = warBooks.get(keyId);
            if (book == null) {
                handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK);
                return;
            }
            if (book.getIsLock() == 1) {
                handler.sendErrorMsgToPlayer(GameError.WAR_BOOK_LOCK);
                return;
            }
            // 找到兵书
            int bookId = book.getBookId();
            // 查找配置
            StaticWarBook staticWarBook = staticWarBookMgr.getWarBookConfigById(bookId);
            if (staticWarBook == null) {
                handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK_CONFIG);
                return;
            }
            int quality = staticWarBook.getQuality();
            int level = 0;
            ArrayList<Integer> baseProperty = book.getBaseProperty();
            if (baseProperty.size() > 0) {
                Integer basePropId = book.getBaseProperty().get(0);
                StaticWarBookBaseProperty warBookBaseProperty = staticWarBookMgr.getWarBookBasePropById(basePropId);
                if (null != warBookBaseProperty) {
                    level = warBookBaseProperty.getLevel();
                }
            }
            StaticWarBookDecom warBookWarBookDecom = staticWarBookMgr.getWarBookWarBookDecom(quality, level);
            if (warBookWarBookDecom == null) {
                handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK_CONFIG);
                return;
            }

            List<Award> awards = warBookManager.decompoundWarBook(player,  warBookWarBookDecom);
            for (Award award : awards) {
                int newKeyId = award.getKeyId();
                //兵书类型的道具在生成时候已经添加到玩家身上不需要重新添加
                if (newKeyId != 0) {
                    awardList.add(PbHelper.createAward(player, award.getType(), award.getId(), award.getCount(), newKeyId).build());
                } else {
                    playerManager.addAward(player, award.getType(), award.getId(), award.getCount(), Reason.DECOMPOUSE_BOOK);
                    awardList.add(PbHelper.createAward(award.getType(), award.getId(), award.getCount()).build());
                }
            }
            warBooks.remove(keyId);
            logUser.war_book_log(WarBookLog.builder()
                    .lordId(player.roleId)
                    .level(player.getLevel())
                    .nick(player.getNick())
                    .vip(player.getVip())
                    .reason(Reason.DECOMPOUSE_BOOK)
                    .bookName(staticWarBook.getName())
                    .build());
        }

        List<Award> awardListsFinal = PbHelper.finilAward1(awardList);//合并奖励
        builder.addAllAward(PbHelper.createAwardList1(player, awardListsFinal));
        handler.sendMsgToPlayer(GameError.OK, WarBookPb.DecompoundWarBookRs.ext, builder.build());
    }

    /**
     * Function:英雄穿兵书 从兵书背包删除一个兵书，英雄身上产生一个兵书
     *
     * @param handler
     */
    public void wearWarBookRq(WarBookPb.WearWarBookRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        HashMap<Integer, Hero> heros = player.getHeros();
        int heroId = req.getHeroId();
        Hero hero = heros.get(heroId);
        if (hero == null) {
            LogHelper.CONFIG_LOGGER.info("hero not exists, id = " + heroId);
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        // 检查物品是否存在
        int keyId = req.getKeyId();
        Map<Integer, WarBook> warBooks = player.getWarBooks();

        // 兵书不存在
        WarBook book = warBooks.get(keyId);
        //System.err.println(book);
        if (book == null) {
            // LogHelper.CONFIG_LOGGER.info("equip not exists, equip unqiueId = "
            // + keyId);
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK);
            return;
        }

        // 英雄正在行军之中
        if (player.isInMarch(hero)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
            return;
        }

        if (player.isInMass(heroId)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_MASS);
            return;
        }

        if (!hero.isActivated()) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IS_NOT_ACTIVATE);
            return;
        }

        if (player.hasPvpHero(heroId)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_PVP_BATTLE);
            return;
        }

        // 检查有无兵书
        int bookId = book.getBookId();
        int bookType = staticWarBookMgr.getBookType(bookId);
        if (bookType == 0) {
            LogHelper.CONFIG_LOGGER.info("no this equip type, not in [1~6], equipId = " + bookId);
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK_CONFIG);
            return;
        }

        HeroBook heroBook = heroDataManager.getWarBook(hero, bookType);
        WarBookPb.WearWarBookRs.Builder builder = WarBookPb.WearWarBookRs.newBuilder();
        // 删除兵书
        if (heroBook != null) {
            builder.setRemoveWarBookItemId(keyId);
            builder.setRemoveHeroWarBookId(heroBook.getBook().getKeyId());
            // 克隆一份
            WarBook cloneBook = book.cloneInfo();

            // 生成一个新的英雄兵书
            HeroBook newHeroBook = new HeroBook();
            newHeroBook.setPos(bookType);
            newHeroBook.setBook(cloneBook);

            // 生成一个新的背包兵书
            WarBook newWarBook = new WarBook();
            newWarBook.copyData(heroBook.getBook());
            // remove and add
            hero.removeBook(bookType);
            hero.addHerobook(newHeroBook);

            // remove and add
            warBooks.remove(book.getKeyId());
            warBooks.put(newWarBook.getKeyId(), newWarBook);
            builder.setAddWarBookItem(newWarBook.wrapPb());
            builder.setAddHeroWarBook(newHeroBook.wrapPb());
            builder.setHeroId(heroId);
            builder.setHasWarBook(true);
        } else {
            // 增加英雄身上兵书
            heroBook = new HeroBook();
            heroBook.setPos(bookType);
            heroBook.setBook(book.cloneInfo());
            hero.addHerobook(heroBook);
            builder.setAddHeroWarBook(heroBook.wrapPb());
            // 减少背包兵书
            builder.setRemoveWarBookItemId(book.getKeyId());
            warBooks.remove(book.getKeyId());
            builder.setHeroId(heroId);
            builder.setHasWarBook(false);
        }
        StaticWarBook warBook = staticWarBookMgr.getWarBookConfigById(book.getBookId());
        logUser.wear_book_log(WearBookLog.builder()
                .lordId(player.roleId)
                .level(player.getLevel())
                .nick(player.getNick())
                .vip(player.getVip())
                .bookName(warBook.getName())
                .build());

        heroDataManager.caculateProp(hero, player);
        builder.setHeroProperty(hero.getTotalProp().wrapPb());
        handler.sendMsgToPlayer(GameError.OK, WarBookPb.WearWarBookRs.ext, builder.build());
        heroDataManager.updateHero(player, hero, Reason.WEAR_BOOK);
        heroDataManager.synBattleScoreAndHeroList(player,player.getAllHeroList());

        warBookManager.addWarBookBuff(player, hero);//添加兵书技能buff加成
    }

    /**
     * 脱兵书
     *
     * @param req
     * @param handler
     */
    public void takeOffWarBookRq(WarBookPb.TakeOffWarBookRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        HashMap<Integer, Hero> heros = player.getHeros();
        int heroId = req.getHeroId();
        // 有无英雄
        Hero hero = heros.get(heroId);
        if (hero == null) {
            LogHelper.CONFIG_LOGGER.info("hero not exists, id = " + heroId);
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        int keyId = req.getKeyId();
        // 检查有无兵书
        HeroBook heroBook = hero.getBookByUId(keyId);
        if (heroBook == null) {
            LogHelper.CONFIG_LOGGER.info("hero book not exists, keyId" + " = " + keyId);
            handler.sendErrorMsgToPlayer(GameError.NO_HERO_EQUIP);
            return;
        }

        WarBook book = heroBook.getBook();
        // 获取兵书类型
        int bookId = book.getBookId();
        int bookType = staticWarBookMgr.getBookType(bookId);
        if (bookType == 0) {
            LogHelper.CONFIG_LOGGER.info("no equip type, type = " + bookType);
            handler.sendErrorMsgToPlayer(GameError.NO_EQUIP_TYPE);
            return;
        }

        // 英雄正在行军之中
        if (player.isInMarch(hero)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
            return;
        }

        if (player.isInMass(heroId)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_MASS);
            return;
        }

        if (player.hasPvpHero(heroId)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_PVP_BATTLE);
            return;
        }

        if (!hero.isActivated()) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IS_NOT_ACTIVATE);
            return;
        }
        // 脱兵书
        hero.removeBook(bookType);
        // 增加背包兵书
        WarBook bookAdd = book.cloneInfo();
        Map<Integer, WarBook> warBooks = player.getWarBooks();
        warBooks.put(keyId, bookAdd);
        heroDataManager.caculateProp(hero, player);

        WarBookPb.TakeOffWarBookRs.Builder builder = WarBookPb.TakeOffWarBookRs.newBuilder();
        builder.setHeroId(heroId);
        builder.setHeroWarBookId(keyId);
        builder.setAddWarBookItem(bookAdd.wrapPb());
        builder.setHeroProperty(hero.getTotalProp().wrapPb());

        handler.sendMsgToPlayer(GameError.OK, WarBookPb.TakeOffWarBookRs.ext, builder.build());
        heroDataManager.updateHero(player, hero, Reason.TAKE_BOOK);
        heroDataManager.synBattleScoreAndHeroList(player,player.getAllHeroList());

        warBookManager.removeWarBookBuff(player, hero);//移除兵书技能buff加成
    }

    /**
     * 强化兵书技能
     *
     * @param req
     * @param handler
     */
    public void strongWarBookRq(WarBookPb.StrongWarBookRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        // 检查物品是否存在
        int keyId = req.getKeyId();
        int heroId = req.getHeroId();

        if (keyId == 0) {
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK);
            return;
        }

        WarBook book = null;
        Hero hero = null;
        if (heroId == 0) {
            Map<Integer, WarBook> warBooks = player.getWarBooks();
            // 兵书不存在
            book = warBooks.get(keyId);
        } else {
            HashMap<Integer, Hero> heros = player.getHeros();
            hero = heros.get(heroId);
            if (null == hero) {
                LogHelper.CONFIG_LOGGER.info("hero not exists, id = " + heroId);
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
                return;
            }
            ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
            if (null != heroBooks) {
                for (HeroBook heroBook : heroBooks) {
                    WarBook warBook = heroBook.getBook();
                    if (warBook.getKeyId() == keyId) {
                        book = warBook;
                    }
                }
            }
        }

        if (book == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK);
            return;
        }
        // 找到兵书
        int bookId = book.getBookId();
        // 查找配置
        StaticWarBook staticWarBook = staticWarBookMgr.getWarBookConfigById(bookId);
        if (staticWarBook == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK_CONFIG);
            return;
        }
        StaticWarBookBaseProperty warBookBasePropById = staticWarBookMgr.getWarBookBasePropById(book.getBaseProperty().get(0));
        if (warBookBasePropById == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK_CONFIG);
            return;
        }
        int basePropType = warBookBasePropById.getBasePropType();
        int baseProplevel = warBookBasePropById.getLevel();
        //4.升级兵书主技能的配置
        StaticWarBookBaseProperty warBookBasePropByTypeAndlev = staticWarBookMgr.getWarBookBasePropByTypeAndlev(basePropType, baseProplevel + 1);
        if (warBookBasePropByTypeAndlev == null) {
            handler.sendErrorMsgToPlayer(GameError.WAR_BOOK_LEVLE_ENOUGH);
            return;
        }

        int itemNum = player.getItemNum(226);
        if (itemNum < warBookBasePropById.getNeedStrengthenNum()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
            return;
        }

        int strongSkillId = warBookManager.strongWarBook(book);
        playerManager.subAward(player, AwardType.PROP, 226, warBookBasePropById.getNeedStrengthenNum(), Reason.BOOK_STRONG);
        WarBookPb.StrongWarBookRs.Builder builder = WarBookPb.StrongWarBookRs.newBuilder();
        builder.setBook(book.wrapPb());
        builder.setGold(player.getGold());
        builder.setItem1(player.getItemNum(226));
        builder.setItem2(player.getItemNum(227));
        builder.setStrongSkillId(strongSkillId);
        if (null != hero) {
            heroDataManager.caculateProp(hero, player);
            builder.setHeroProperty(hero.getTotalProp().wrapPb());
            heroDataManager.updateHero(player, hero, Reason.TAKE_BOOK);
            heroDataManager.synBattleScoreAndHeroList(player,player.getAllHeroList());
        }
        handler.sendMsgToPlayer(GameError.OK, WarBookPb.StrongWarBookRs.ext, builder.build());
        warBookManager.addWarBookBuff(player, hero);//添加兵书技能buff加成
    }

    /**
     * 获取兵书商城物品
     *
     * @param handler
     */
    public void getWarBookShopRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        WarBookPb.GetWarBookShopRs.Builder builder = WarBookPb.GetWarBookShopRs.newBuilder();
        Map<Integer, CommonPb.WarBookShopItem> warBookShops = player.getWarBookShops();

        long warBookShopRefreshTime = player.getLord().getWarBookShopRefreshTime();
        long now = System.currentTimeMillis();

        if (warBookShops.size() == 0) {
            warBookManager.refreshWarbookShop(player);
            //player.getLord().setWarBookShopRefreshTime(now + 6 * TimeHelper.HOUR_MS);
            warBookManager.updateRefeshWarShopTime(player);
            player.getLord().setWarBookShopRefresh(500);
        } else {
            if (warBookShopRefreshTime < now) {
                warBookManager.refreshWarbookShop(player);
                warBookManager.updateRefeshWarShopTime(player);
                // player.getLord().setWarBookShopRefreshTime(now + 6 * TimeHelper.HOUR_MS);
            }
        }

        Iterator<CommonPb.WarBookShopItem> warBookShopItem = warBookShops.values().iterator();
        while (warBookShopItem.hasNext()) {
            CommonPb.WarBookShopItem next = warBookShopItem.next();
            if (null != next) {
                builder.addItem(next);
            }
        }
        builder.setRefreshEndTime(player.getLord().getWarBookShopRefreshTime());
        builder.setRefreshTimes(player.getLord().getWarBookShopRefresh());
        handler.sendMsgToPlayer(GameError.OK, WarBookPb.GetWarBookShopRs.ext, builder.build());
        player.setBookFlush(2);

    }

    /**
     * 获取兵书兑换列表
     *
     * @param handler
     */
    public void getWarBookExchangeRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        WarBookPb.GetWarBookExchangeRs.Builder builder = WarBookPb.GetWarBookExchangeRs.newBuilder();
        Map<Integer, StaticWarBookExchange> staticWarBookExchangeMap = staticWarBookMgr.getStaticWarBookExchangeMap();
        if (null != staticWarBookExchangeMap) {
            Iterator<StaticWarBookExchange> iterator = staticWarBookExchangeMap.values().iterator();
            while (iterator.hasNext()) {
                StaticWarBookExchange next = iterator.next();
                if (null == next) {
                    continue;
                }
                CommonPb.WarBookShopItem.Builder item = CommonPb.WarBookShopItem.newBuilder();
                item.setPos(next.getId());
                List<Integer> award = next.getAward();
                item.setAward(PbHelper.createAward(award.get(0), award.get(1), award.get(2)));
                item.setPrice(PbHelper.createAward(AwardType.GOLD, 1, next.getPrice()));
                item.setIsDiscount(next.getIsDiscount());
                builder.addItem(item);
            }
        }
        handler.sendMsgToPlayer(GameError.OK, WarBookPb.GetWarBookExchangeRs.ext, builder.build());

        HashMap<Integer, Hero> heros = player.getHeros();
        Set<Map.Entry<Integer, Hero>> entries = heros.entrySet();
        for (Map.Entry<Integer, Hero> entry : entries) {
            Hero value = entry.getValue();
            if (value != null) {
                ArrayList<HeroBook> heroBooks = value.getHeroBooks();
                if (heroBooks != null && heroBooks.size() == 1) {
                    HeroBook heroBook = heroBooks.get(0);
                    HashMap<Integer, Buff> buffMap = heroBook.getBuffMap();
                    System.err.println(buffMap);
                }
            }
        }
    }

    /**
     * 购买兵书商城物品
     *
     * @param req
     * @param handler
     */
    public void doWarBookShopRq(WarBookPb.DoWarBookShopRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int pos = req.getPos();
        Map<Integer, CommonPb.WarBookShopItem> warBookShops = player.getWarBookShops();
        if (warBookShops.size() == 0) {
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK);
            return;
        }

        CommonPb.WarBookShopItem warBookShopItem = warBookShops.get(pos);
        if (null == warBookShopItem) {
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK);
            return;
        }

        int isbuy = warBookShopItem.getIsbuy();
        if (isbuy == 1) {
            handler.sendErrorMsgToPlayer(GameError.NO_WAR_BOOK);
            return;
        }

        int isFreeBuy = warBookShopItem.getIsFreeBuy();
        CommonPb.Award award = warBookShopItem.getAward();
        CommonPb.Award price = warBookShopItem.getPrice();
        //是否免费,0不免费,1免费
        if (isFreeBuy == 0) {
            int count = price.getCount();
            if (price.getType() == AwardType.GOLD) {
                if (player.getGold() < count) {
                    handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                    return;
                }
            } else {
                Item item = player.getItem(price.getId());
                if (item == null) {
                    handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
                    return;
                }
                int itemNum = item.getItemNum();
                if (itemNum < count) {
                    handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
                    return;
                }
            }
        }

        if (award.getType() == AwardType.WAR_BOOK) {
            WarBook book = new WarBook();
            Map<Integer, WarBook> warBookMap = player.getWarBooks();

            book.setKeyId(player.maxKey());
            book.setBookId(award.getId());
            book.setAllSkill(new ArrayList<>(award.getAllSkillList()));
            book.setCurrentSkill(new ArrayList<>(award.getCurrentSkillList()));
            book.setBaseProperty(new ArrayList<>(award.getBasePropertyList()));
            book.setBasePropertyLv(award.getBasePropertyLv());
            book.setSoldierType(award.getSoldierType());
            warBookMap.put(book.getKeyId(), book);
            StaticWarBook staticWarBook = staticWarBookMgr.getWarBookConfigById(book.getBookId());
            logUser.war_book_log(WarBookLog.builder()
                    .lordId(player.roleId)
                    .level(player.getLevel())
                    .nick(player.getNick())
                    .vip(player.getVip())
                    .reason(Reason.BOOK_SHOP)
                    .bookName(staticWarBook.getName())
                    .build());

            CommonPb.WarBookShopItem.Builder builder = CommonPb.WarBookShopItem.newBuilder();
            builder.setPos(pos);
            builder.setPlace(warBookShopItem.getPlace());
            builder.setPrice(warBookShopItem.getPrice());
            builder.setIsbuy(1);
            builder.setAward(PbHelper.createAward(player, award.getType(), award.getId(), award.getCount(), book.getKeyId()));
            builder.setIsFreeBuy(warBookShopItem.getIsFreeBuy());
            warBookShops.put(pos, builder.build());
            warBookShopItem = builder.build();

            //System.err.println("购买的兵书----------------------------------" + book);
            //System.err.println("兵书商店列表----------------------------------" + warBookMap);
        } else {
            CommonPb.WarBookShopItem.Builder builder = CommonPb.WarBookShopItem.newBuilder();
            builder.setPos(pos);
            builder.setPlace(warBookShopItem.getPlace());
            builder.setPrice(warBookShopItem.getPrice());
            builder.setIsbuy(1);
            builder.setAward(PbHelper.createAward(award.getType(), award.getId(), award.getCount()));
            builder.setIsFreeBuy(warBookShopItem.getIsFreeBuy());
            warBookShops.put(pos, builder.build());
            warBookShopItem = builder.build();

            playerManager.addAward(player, award.getType(), award.getId(), award.getCount(), Reason.BOOK_SHOP);
        }

        if (isFreeBuy == 0) {
            playerManager.subAward(player, price.getType(), price.getId(), price.getCount(), Reason.BOOK_SHOP);
        }

        WarBookPb.DoWarBookShopRs.Builder builder = WarBookPb.DoWarBookShopRs.newBuilder();
        builder.setAward(warBookShopItem.getAward());
        builder.setGold(player.getGold());
        builder.setItem1(player.getItemNum(226));
        builder.setItem2(player.getItemNum(227));
        handler.sendMsgToPlayer(GameError.OK, WarBookPb.DoWarBookShopRs.ext, builder.build());
    }

    /**
     * 刷新兵书商城
     *
     * @param handler
     */
    public void refreshWarBookShopRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        long now = System.currentTimeMillis();
        long warBookShopRefreshTime = player.getLord().getWarBookShopRefreshTime();
        int warBookShopRefresh = player.getLord().getWarBookShopRefresh();

        WarBookPb.RefreshWarBookShopRs.Builder builder = WarBookPb.RefreshWarBookShopRs.newBuilder();
        if (now > warBookShopRefreshTime) {
            warBookManager.refreshWarbookShop(player);
            //player.getLord().setWarBookShopRefreshTime(now + 6 * TimeHelper.HOUR_MS);
            warBookManager.updateRefeshWarShopTime(player);
        } else {
            if (warBookShopRefresh <= 0) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
                return;
            }
            if (player.getGold() < 10) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }
            warBookManager.refreshWarbookShop(player);
            playerManager.subAward(player, AwardType.GOLD, 1, 10, Reason.BOOK_SHOP);
            player.getLord().setWarBookShopRefresh(warBookShopRefresh - 1);
        }

        Map<Integer, CommonPb.WarBookShopItem> warBookShops = player.getWarBookShops();
        Iterator<CommonPb.WarBookShopItem> warBookShopItem = warBookShops.values().iterator();
        while (warBookShopItem.hasNext()) {
            CommonPb.WarBookShopItem next = warBookShopItem.next();
            if (null != next) {
                builder.addItem(next);
            }
        }

        builder.setRefreshTimes(player.getLord().getWarBookShopRefresh());
        builder.setRefreshEndTime(player.getLord().getWarBookShopRefreshTime());
        builder.setGold(player.getGold());
        handler.sendMsgToPlayer(GameError.OK, WarBookPb.RefreshWarBookShopRs.ext, builder.build());
    }

    /**
     * 兵书商店物品兑换
     *
     * @param req
     * @param handler
     */
    public void doWarBookExchangeRq(WarBookPb.DoWarBookExchangeRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int pos = req.getPos();
        StaticWarBookExchange staticWarBookExchange = staticWarBookMgr.getStaticWarBookExchange(pos);
        if (null == staticWarBookExchange) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        int price = staticWarBookExchange.getPrice();
        if (player.getGold() < price) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        List<Integer> award = staticWarBookExchange.getAward();
        playerManager.addAward(player, award.get(0), award.get(1), award.get(2), Reason.BOOK_EXCHANGE);
        playerManager.subAward(player, AwardType.GOLD, 1, price, Reason.BOOK_EXCHANGE);

        WarBookPb.DoWarBookExchangeRs.Builder builder = WarBookPb.DoWarBookExchangeRs.newBuilder();
        builder.setAward(PbHelper.createAward(award.get(0), award.get(1), award.get(2)));
        builder.setGold(player.getGold());
        builder.setItem1(player.getItemNum(226));
        builder.setItem2(player.getItemNum(227));
        handler.sendMsgToPlayer(GameError.OK, WarBookPb.DoWarBookExchangeRs.ext, builder.build());
    }
}
