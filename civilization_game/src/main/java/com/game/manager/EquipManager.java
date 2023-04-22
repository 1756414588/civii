package com.game.manager;

import com.game.activity.ActivityEventManager;
import com.game.activity.actor.EquipAddActor;
import com.game.activity.define.EventEnum;
import com.game.constant.AwardType;
import com.game.constant.MailId;
import com.game.constant.PropertyType;
import com.game.constant.Quality;
import com.game.dataMgr.StaticEquipDataMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticSkillMgr;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.BattleProperty;
import com.game.domain.p.Equip;
import com.game.domain.p.Hero;
import com.game.domain.p.HeroEquip;
import com.game.domain.p.Lord;
import com.game.domain.p.Property;
import com.game.domain.s.StaticEquip;
import com.game.domain.s.StaticSkill;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.log.domain.EquipDecompoundLog;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.game.spring.SpringUtil;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EquipManager {

    @Autowired
    private StaticEquipDataMgr staticEquipMgr;

    @Autowired
    private StaticSkillMgr staticSkillDataMgr;

    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private TechManager techManager;

    @Autowired
    private StaticSkillMgr staticSkillMgr;

    @Autowired
    private HeroManager heroManager;

    @Autowired
    private ActivityManager activityManager;
    @Autowired
    private PlayerManager playerManager;
    @Autowired
    ChatManager chatManager;

    // 添加装备,需要同步物品和背包格数到客户端[背包格子数已+1]
    public Equip addEquip(Player player, int equipId, int reason) {
        // 检查是否背包已满
        Equip equip = new Equip();
        Map<Integer, Equip> equipMap = player.getEquips();
        int freeSlot = getFreeSlot(player);
        if (freeSlot <= 0) {
            //发邮件咯
            List<Award> list = Lists.newArrayList(new Award(AwardType.EQUIP, equipId, 1));
            playerManager.sendAttachMail(player, list, MailId.GRID_IS_FULL);
            return equip;
        }

        equip.setKeyId(player.maxKey());
        equip.setEquipId(equipId);
        Map<Integer, StaticEquip> equipMapConfig = staticEquipMgr.getEquipMap();
        StaticEquip staticEquip = equipMapConfig.get(equipId);
        if (staticEquip == null) {
            LogHelper.CONFIG_LOGGER.info("no equip config, equipId = " + equipId + ", reason = " + reason);
            return equip;
        }

        Property property = new Property();
        property.setAttack(staticEquip.getAttack());
        property.setDefence(staticEquip.getDefence());
        property.setSoldierNum(staticEquip.getSoldierCount());

        if (staticEquip.getSecretSkill() == 0) {
            List<Integer> skillList = staticEquip.getSkillId();
            List<Integer> randSkills = new ArrayList<Integer>();
            if (!skillList.isEmpty()) {
                randSkills = staticSkillDataMgr.getRandomSkills(skillList.size());
            }
            equip.getSkills().addAll(randSkills);
        } else {
            Stream.of(1, 2, 3, 4).forEach(x -> {
                equip.getSkills().add(staticEquip.getSecretSkill());
            });
        }
        equipMap.put(equip.getKeyId(), equip);
        // 记录收集
        player.updateMakeEquipNum(staticEquip.getQuality(), staticEquip.getEquipType());
        /**
         * 装备获得日志埋点
         */
        EquipDecompoundLog log = EquipDecompoundLog.builder()
            .roleId(player.roleId)
            .roleName(player.getNick())
            .roleLv(player.getLevel())
            .title(player.getTitle())
            .country(player.getCountry())
            .vip(player.getVip())
            .equipId(equipId)
            .quality(staticEquip.getQuality())
            .roleCreateTime(player.account.getCreateDate())
            .channel(player.account.getChannel())
            .keyId(player.account.getAccountKey())
            .decompose(false)
            .reason(reason)
            .build();
        SpringUtil.getBean(com.game.log.LogUser.class).equipDecompoundLog(log);
        SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.add_equip);
        SpringUtil.getBean(EventManager.class).equip_add(player, Lists.newArrayList(
            equipId,
            staticEquip.getEquipName()
        ));

        if (staticEquip.getQuality() >= Quality.BLUE.get()) {
            ActivityEventManager.getInst().updateActivityHandler(EventEnum.EQUIP_ADD, new EquipAddActor(player, staticEquip));
        }

        return equip;
    }

    // 有一定几率打造极品装备,获取打造极品装备的概率, 千分比
    public boolean canMakeSpecialEquip(Player player, StaticEquip staticEquip) {
        if (staticEquip.getQuality() <= Quality.GREEN.get()) {
            return false;
        }
        // 有一定几率打造极品装备,获取打造极品装备的概率, 千分比
        int specialEquip = techManager.getSpecialEquip(player);
        int randNum = RandomHelper.threadSafeRand(1, 1000);
        if (randNum <= specialEquip) {
            return true;
        }
        return false;
    }

    // 打造极品装备
    public void makeSpecialEquip(Equip equip, StaticEquip staticEquip) {
        ArrayList<Integer> skills = equip.getSkills();
        List<Integer> skillLevelList = new ArrayList<Integer>();
        List<Integer> skillTypeList = new ArrayList<Integer>();
        for (Integer skillId : skills) {
            StaticSkill staticSkill = staticSkillDataMgr.getStaticSkill(skillId);
            if (staticSkill == null) {
                LogHelper.CONFIG_LOGGER.info("static skill is null, skillId = " + skillId);
                continue;
            }
            skillLevelList.add(staticSkill.getLevel());
            skillTypeList.add(staticSkill.getSkillType());
        }

        if (skills.size() == 3) {
            int randSkillType = RandomHelper.randSkillType(); // 技能暂时不洗练出命中和闪避
            for (int i = 0; i < skillTypeList.size(); i++) {
                skillTypeList.set(i, randSkillType);
                skillLevelList.set(i, staticEquip.getQuality());
            }
            skillTypeList.add(randSkillType); // 新增一个技能类型
            skillLevelList.add(staticEquip.getQuality()); // 前面的都满级了
        } else if (skills.size() == 4) {
            int randSkillType = RandomHelper.randSkillType(); // 技能暂时不洗练出命中和闪避
            for (int i = 0; i < skillTypeList.size(); i++) {
                skillTypeList.set(i, randSkillType);
                skillLevelList.set(i, staticEquip.getQuality());
            }
        }

        skills.clear();
        for (int i = 0; i < skillLevelList.size(); i++) {
            StaticSkill staticSkill = staticSkillDataMgr.getSkillByLvType(skillLevelList.get(i), skillTypeList.get(i));
            if (staticSkill == null) {

                continue;
            }
            skills.add(staticSkill.getSkillId());
        }
    }

    // 减少装备, 装备类型从配置表读取，不要写死在程序中
    public Equip subEquip(Player player, int keyId, int reason) {
        Map<Integer, Equip> equipMap = player.getEquips();
        Equip equip = equipMap.get(keyId);
        if (equip != null) {
            equipMap.remove(keyId);
        }
        return equip;
    }

    // 获得装备的一级属性
    public Property getProperty(Equip equip) {
        Property property = new Property();
        if (equip == null) {
            return property;
        }

        // 获得装备配置
        int equipId = equip.getEquipId();
        StaticEquip staticEquip = staticEquipMgr.getStaticEquip(equipId);
        if (staticEquip == null) {

            return property;
        }

        // 基础属性
        property.setAttack(property.getAttack() + staticEquip.getAttack());
        property.setDefence(property.getDefence() + staticEquip.getDefence());
        property.setSoldierNum(property.getSoldierNum() + staticEquip.getSoldierCount());

        // 技能属性
        List<Integer> skills = equip.getSkills();
        for (Integer skillId : skills) {
            StaticSkill staticSkill = staticSkillDataMgr.getStaticSkill(skillId);
            if (staticSkill == null) {
                LogHelper.CONFIG_LOGGER.info("static skill is null, skillId = " + skillId);
                continue;
            }
            if (staticSkill.getSkillType() == PropertyType.ATTCK) {
                property.setAttack(property.getAttack() + staticSkill.getSkillNum());
            } else if (staticSkill.getSkillType() == PropertyType.DEFENCE) {
                property.setDefence(property.getDefence() + staticSkill.getSkillNum());
            } else if (staticSkill.getSkillType() == PropertyType.SOLDIER_NUM) {
                property.setSoldierNum(property.getSoldierNum() + staticSkill.getSkillNum());
            } else if (staticSkill.getSkillType() == PropertyType.ATTACK_CITY) {
                property.setAttackCity(property.getAttackCity() + staticSkill.getSkillNum());
            } else if (staticSkill.getSkillType() == PropertyType.DEFENCE_CITY) {
                property.setDefenceCity(property.getDefenceCity() + staticSkill.getSkillNum());
            } else if (staticSkill.getSkillType() == PropertyType.STRONG_ATTACK) {
                property.setStrongAttack(property.getStrongAttack() + staticSkill.getSkillNum());
            } else if (staticSkill.getSkillType() == PropertyType.STRONG_DEFENCE) {
                property.setStrongDefence(property.getStrongDefence() + staticSkill.getSkillNum());
            }
        }
        return property;
    }

    // 获取二级战斗属性
    public BattleProperty getBattleProperty(List<Integer> skills) {
        BattleProperty property = new BattleProperty();
        for (Integer skillId : skills) {
            StaticSkill staticSkill = staticSkillDataMgr.getStaticSkill(skillId);
            if (staticSkill == null) {
                LogHelper.CONFIG_LOGGER.info("static skill is null, skillId = " + skillId);
                continue;
            }

            if (staticSkill.getSkillType() == PropertyType.STRONG_ATTACK) {
                property.setStrongAttack(property.getStrongAttack() + staticSkill.getSkillNum());
            } else if (staticSkill.getSkillType() == PropertyType.STRONG_DEFENCE) {
                property.setStrongDefence(property.getStrongDefence() + staticSkill.getSkillNum());
            }
        }

        return property;
    }

    // 获得装备的品质
    public int getQuality(int equipId) {
        StaticEquip staticEquip = staticEquipMgr.getStaticEquip(equipId);
        if (staticEquip == null) {
            return 6;
        } else {
            return staticEquip.getQuality();
        }
    }

    // 计算格子剩余数
    public int getFreeSlot(Player player) {
        int equipNum = player.getEquips().size();
        Lord lord = player.getLord();
        int buyTimes = lord.getBuyEquipSlotTimes();
        int slotNum = staticLimitMgr.getEquipSlotNum();
        int add = buyTimes * slotNum;
        int initSlot = staticLimitMgr.getInitSlot();
        int total = initSlot + add;
        int freeSlot = total - equipNum;
        freeSlot = Math.max(0, freeSlot);
        return freeSlot;
    }

    // 获取装备
    public Equip getEquip(Player player, int equiType, int skillType) {
        Map<Integer, Equip> equips = player.getEquips();
        for (Equip equip : equips.values()) {
            int equipId = equip.getEquipId();
            StaticEquip staticEquip = staticEquipMgr.getStaticEquip(equipId);
            if (staticEquip.getEquipType() != equiType) {
                continue;
            }

            ArrayList<Integer> skills = equip.getSkills();
            //判定下颜色
            if (staticEquip.getQuality() != Quality.GOLD.get()) {
                continue;
            }

            boolean isSkillOk = true;
            for (Integer skillId : skills) {
                StaticSkill staticSkill = staticSkillMgr.getStaticSkill(skillId);
                if (staticSkill.getLevel() < staticEquip.getQuality()) {
                    isSkillOk = false;
                    break;
                }

                if (staticSkill.getSkillType() != skillType) {
                    isSkillOk = false;
                    break;
                }
            }

            if (isSkillOk) {
                return equip;
            }
        }
        return null;
    }

    // remove equip
    public void removeEquips(Player player, List<Integer> keyIds) {
        Map<Integer, Equip> equips = player.getEquips();
        for (Integer keyId : keyIds) {
            equips.remove(keyId);
        }
    }

    // 检查所有玩家的装备是否触发秘技
    public void checkSpecialSkill(Player player) {
        checkHeroEquip(player);
        checkBagEquip(player);
        heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
    }

    public void checkHeroEquip(Player player) {
        HashMap<Integer, Hero> heroHashMap = player.getHeros();
        for (Hero hero : heroHashMap.values()) {
            ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
            for (HeroEquip heroEquip : heroEquips) {
                Equip equip = heroEquip.getEquip();
                checkEquipSkill(equip);
            }
        }
    }

    public void checkBagEquip(Player player) {
        Map<Integer, Equip> equips = player.getEquips();
        for (Equip equip : equips.values()) {
            checkEquipSkill(equip);
        }
    }

    public void checkEquipSkill(Equip equip) {
        HashSet<Integer> isSame = new HashSet<Integer>();
        if (equip == null) {
            return;
        }

        StaticEquip staticEquip = staticEquipMgr.getStaticEquip(equip.getEquipId());
        if (staticEquip == null) {
            return;
        }

        if (staticEquip.getQuality() <= Quality.GREEN.get()) {
            return;
        }

        ArrayList<Integer> skills = equip.getSkills();
        // 技能满级
        int totalLevel = 0;
        int maxLevel = 0;
        boolean isAllSkillLvOk = false;
        for (Integer skillId : skills) {
            StaticSkill staticSkill = staticSkillDataMgr.getStaticSkill(skillId);
            if (staticSkill == null) {
                LogHelper.CONFIG_LOGGER.info("static skill is null, skillId = " + skillId);
                continue;
            }
            totalLevel += staticSkill.getLevel();
            maxLevel += staticEquip.getQuality();
            isSame.add(skillId);
        }

        if (totalLevel >= maxLevel) {
            isAllSkillLvOk = true;
        }

        // 再新增一个技能
        if (isAllSkillLvOk && isSame.size() == 1 && skills.size() == 3 && staticEquip.getQuality() >= Quality.GOLD.get()) {
            skills.add(skills.get(0));
        }

    }

    public int getQualityNum(Player player, int quality, int equipType) {
        int count = 0;
        Iterator<Equip> it = player.getEquips().values().iterator();
        while (it.hasNext()) {
            Equip next = it.next();
            if (next == null) {
                continue;
            }
            StaticEquip staticEquip = staticEquipMgr.getStaticEquip(next.getEquipId());
            if (staticEquip == null || staticEquip.getQuality() < quality) {
                continue;
            }
            if (equipType != 0 && staticEquip.getEquipType() != equipType) {
                continue;
            }
            count++;
        }

        Iterator<Hero> heros = player.getHeros().values().iterator();
        while (heros.hasNext()) {
            Hero next = heros.next();
            if (next == null) {
                continue;
            }
            for (HeroEquip heroEquip : next.getHeroEquips()) {
                Equip equip = heroEquip.getEquip();
                if (equip == null) {
                    continue;
                }
                StaticEquip staticEquip = staticEquipMgr.getStaticEquip(equip.getEquipId());
                if (staticEquip == null || staticEquip.getQuality() < quality) {
                    continue;
                }
                if (equipType != 0 && staticEquip.getEquipType() != equipType) {
                    continue;
                }
                count++;
            }
        }
        return count;
    }

    public int getQualityWashMax(Player player, int quality) {
        int count = 0;
        Iterator<Equip> it = player.getEquips().values().iterator();
        while (it.hasNext()) {
            Equip next = it.next();
            if (next == null) {
                continue;
            }
            StaticEquip staticEquip = staticEquipMgr.getStaticEquip(next.getEquipId());
            if (staticEquip == null || staticEquip.getQuality() < quality) {
                continue;
            }

            boolean flag = true;
            for (int skillId : next.getSkills()) {
                StaticSkill staticSkill = staticSkillMgr.getStaticSkill(skillId);
                if (staticSkill == null || staticSkill.getLevel() < staticEquip.getQuality()) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                count++;
            }
        }

        Iterator<Hero> heros = player.getHeros().values().iterator();
        while (heros.hasNext()) {
            Hero next = heros.next();
            if (next == null) {
                continue;
            }
            for (HeroEquip heroEquip : next.getHeroEquips()) {
                Equip equip = heroEquip.getEquip();
                if (equip == null) {
                    continue;
                }
                StaticEquip staticEquip = staticEquipMgr.getStaticEquip(equip.getEquipId());
                if (staticEquip == null || staticEquip.getQuality() < quality) {
                    continue;
                }

                boolean flag = true;
                if (equip.getSkills().isEmpty()) {
                    continue;
                }
                for (int skillId : equip.getSkills()) {
                    StaticSkill staticSkill = staticSkillMgr.getStaticSkill(skillId);
                    if (staticSkill == null || staticSkill.getLevel() < staticEquip.getQuality()) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isEquipWashFull(Equip equip) {
        StaticEquip staticEquip = staticEquipMgr.getStaticEquip(equip.getEquipId());
        if (staticEquip == null) {
            return false;
        }

        boolean flag = true;
        if (equip.getSkills().isEmpty()) {
            return false;
        }

        for (int skillId : equip.getSkills()) {
            StaticSkill staticSkill = staticSkillMgr.getStaticSkill(skillId);
            if (staticSkill == null || staticSkill.getLevel() < staticEquip.getQuality()) {
                flag = false;
                break;
            }
        }
        return flag;
    }


    public void checkWashEquip(Player player) {
        Iterator<Equip> it = player.getEquips().values().iterator();
        while (it.hasNext()) {
            Equip next = it.next();
            if (next == null) {
                continue;
            }
            StaticEquip staticEquip = staticEquipMgr.getStaticEquip(next.getEquipId());
            if (staticEquip == null) {
                continue;
            }
            player.updateMakeEquipNum(next.getKeyId(), staticEquip.getEquipType());
            boolean flag = true;
            for (int skillId : next.getSkills()) {
                StaticSkill staticSkill = staticSkillMgr.getStaticSkill(skillId);
                if (staticSkill == null || staticSkill.getLevel() < staticEquip.getQuality()) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                player.updateWashEquipNum(next.getKeyId(), staticEquip.getQuality());
            }
        }

        Iterator<Hero> heros = player.getHeros().values().iterator();
        while (heros.hasNext()) {
            Hero next = heros.next();
            if (next == null) {
                continue;
            }
            for (HeroEquip heroEquip : next.getHeroEquips()) {
                Equip equip = heroEquip.getEquip();
                if (equip == null) {
                    continue;
                }

                StaticEquip staticEquip = staticEquipMgr.getStaticEquip(equip.getEquipId());
                if (staticEquip == null) {
                    continue;
                }

                player.updateMakeEquipNum(equip.getKeyId(), staticEquip.getEquipType());

                boolean flag = true;
                if (equip.getSkills().isEmpty()) {
                    continue;
                }
                for (int skillId : equip.getSkills()) {
                    StaticSkill staticSkill = staticSkillMgr.getStaticSkill(skillId);
                    if (staticSkill == null || staticSkill.getLevel() < staticEquip.getQuality()) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    player.updateWashEquipNum(equip.getKeyId(), staticEquip.getQuality());
                }
            }
        }

    }

}
