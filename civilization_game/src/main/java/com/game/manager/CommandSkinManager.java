package com.game.manager;

import com.game.constant.BookEffectType;
import com.game.constant.PropertyType;
import com.game.constant.SkinSkillType;
import com.game.constant.SoldierType;
import com.game.dataMgr.StaticBaseSkinMgr;
import com.game.dataMgr.StaticHeroMgr;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.pb.BeautyPb;
import com.game.pb.CommonPb;
import com.game.pb.SkinPb;
import com.game.pb.SkinPb.SynCommandSkinListRs;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.SynHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.PlayerCity;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author CaoBing
 * @date 2021/2/2 14:40
 */
@Component
public class CommandSkinManager {
	@Autowired
	private StaticBaseSkinMgr staticBaseSkinMgr;

	@Autowired
	private HeroManager heroManager;

	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private WorldManager worldManager;

	// 检查皮肤过期
	public void checkCommandSkin() {
		long now = System.currentTimeMillis();
		for (Player player : playerManager.getPlayers().values()) {
			Map<Integer, CommandSkin> commandSkins = player.getCommandSkins();
			List<CommandSkin> list = Lists.newArrayList(commandSkins.values());
			boolean flag = false;
			for (CommandSkin commandSkin : list) {
				if (commandSkin == null || commandSkin.getEndTime() == 0 || commandSkin.getEndTime() > now) {
					continue;
				}
				if (player.getLord().getSkin() == commandSkin.getKeyId()) {
					player.getLord().setSkin(1);
				}
				commandSkins.remove(commandSkin.getKeyId());
				flag = true;
			}
			if (flag) {
				heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
				// 删除实体
				int newMapId = worldManager.getMapId(player);
				MapInfo newMapInfo = worldManager.getMapInfo(newMapId);
				PlayerCity playerCity = newMapInfo.getPlayerCity(player.getPos());
				if (playerCity != null) {
					playerManager.getOnlinePlayer().forEach(e -> {
						worldManager.synEntityRq(playerCity, newMapInfo.getMapId(), player.getOldPos()); // 同步城池
					});
				}
				SynCommandSkinListRs.Builder builder = SynCommandSkinListRs.newBuilder();
				builder.setNowSkin(player.getLord().getSkin());
				builder.addAllSkins(getCommandSkinList(player).getSkinsList());
				SynHelper.synMsgToPlayer(player, SkinPb.SynCommandSkinListRs.EXT_FIELD_NUMBER, SkinPb.SynCommandSkinListRs.ext, builder.build());
			}
		}
	}

	/**
	 * 获取主城皮肤列表
	 *
	 * @param player
	 * @return
	 */
	public SkinPb.GetCommandSkinRs.Builder getCommandSkinList(Player player) {
		SkinPb.GetCommandSkinRs.Builder builder = SkinPb.GetCommandSkinRs.newBuilder();
		Map<Integer, CommandSkin> commandSkins = player.getCommandSkins();

		// 设置默认皮肤
		CommandSkin defaultSkin = commandSkins.get(1);
		if (null == defaultSkin) {
			StaticBaseSkin staticBaseSkin = staticBaseSkinMgr.getStaticBaseSkin(1);
			if (null != staticBaseSkin) {
				CommandSkin commandSkin = new CommandSkin();
				commandSkin.setKeyId(staticBaseSkin.getKeyId());
				commandSkin.setEndTime(staticBaseSkin.getTime());
				commandSkin.setStatus(1);
				commandSkins.put(staticBaseSkin.getKeyId(), commandSkin);
			}
		}

		long now = System.currentTimeMillis();
		int nowSkin = player.getLord().getSkin();
		CommandSkin nowCommandSkin = commandSkins.get(nowSkin);
		if (null != nowCommandSkin) {
			long endTime = nowCommandSkin.getEndTime();
			if (endTime != 0 && endTime <= now) {
				commandSkins.remove(nowSkin);
				player.getLord().setSkin(1);
			}
		} else {
			player.getLord().setSkin(1);
		}

		List<StaticBaseSkin> staticBaseSkinList = staticBaseSkinMgr.getStaticBaseSkinList();
		for (StaticBaseSkin staticBaseSkin : staticBaseSkinList) {
			CommonPb.CommandSkin.Builder commandSkin = CommonPb.CommandSkin.newBuilder();
			commandSkin.setKeyId(staticBaseSkin.getKeyId());

			CommandSkin skin = commandSkins.get(staticBaseSkin.getKeyId());
			if (null != skin) {
				commandSkin.setStatus(1);
				commandSkin.setEndTime(skin.getEndTime());
				commandSkin.setStar(skin.getStar());
			} else {
				commandSkin.setStar(staticBaseSkin.getStar());
				commandSkin.setStatus(0);
				commandSkin.setEndTime(0);
			}
			builder.addSkins(commandSkin);
		}
		return builder;
	}

	/**
	 * 添加主城皮肤
	 *
	 * @param player
	 * @param skinId
	 * @param reason
	 */
	public void addCommandSkin(Player player, int skinId, int reason) {
		if (null == player) {
			return;
		}
		StaticBaseSkin staticBaseSkin = staticBaseSkinMgr.getStaticBaseSkin(skinId);
		if (null == staticBaseSkin) {
			return;
		}
		LogHelper.GAME_DEBUG.debug("addCommandSkin(添加主城皮肤),lordId:[{}],skinId:[{}]", player.roleId, skinId);
		long now = System.currentTimeMillis();
		Map<Integer, CommandSkin> commandSkins = player.getCommandSkins();
		CommandSkin commandSkin = commandSkins.get(skinId);
		if (null == commandSkin) {
			commandSkin = new CommandSkin();
			commandSkin.setKeyId(staticBaseSkin.getKeyId());
			if (staticBaseSkin.getTime() == 0) {
				commandSkin.setEndTime(staticBaseSkin.getTime());
			} else {
				commandSkin.setEndTime(System.currentTimeMillis() + staticBaseSkin.getTime() * 1000L);
			}
			commandSkin.setStatus(1);
			commandSkin.setStar(staticBaseSkin.getStar());
			commandSkins.put(staticBaseSkin.getKeyId(), commandSkin);
		} else {
			if (staticBaseSkin.getTime() == 0) {
				commandSkin.setEndTime(staticBaseSkin.getTime());
			} else if (commandSkin.getEndTime() > now) {
				commandSkin.setEndTime(commandSkin.getEndTime() + staticBaseSkin.getTime() * 1000L);
			} else {
				commandSkin.setEndTime(now + staticBaseSkin.getTime() * 1000L);
			}
		}
		SkinPb.SynCommandSkinRs.Builder builder = SkinPb.SynCommandSkinRs.newBuilder();
		builder.setSkin(commandSkin.wrapPb());
		SynHelper.synMsgToPlayer(player, SkinPb.SynCommandSkinRs.EXT_FIELD_NUMBER, SkinPb.SynCommandSkinRs.ext, builder.build());
	}

	/**
	 * 获取主城皮肤的属性加成
	 *
	 * @param book
	 * @param player
	 * @param hero
	 * @return
	 */
	public Property getProperty(Player player, Hero hero) {
		Property property = new Property();
		if (player == null || hero == null) {
			return property;
		}

		Map<Integer, CommandSkin> commandSkins = player.getCommandSkins();
		if (commandSkins.size() == 0) {
			return property;
		}

		Iterator<CommandSkin> iterator = commandSkins.values().iterator();
		while (iterator.hasNext()) {
			CommandSkin commandSkin = iterator.next();
			if (commandSkin == null) {
				break;
			}

			StaticBaseSkin staticBaseSkin = staticBaseSkinMgr.getStaticBaseSkin(commandSkin.getKeyId());
			if (null == staticBaseSkin) {
				break;
			}

			int star = commandSkin.getStar();
			StaticSkinSkill staticSkinSkill = staticBaseSkinMgr.getStaticSkinSkill(commandSkin.getKeyId(), star);
			if (null == staticSkinSkill) {
				break;
			}

			int heroId = hero.getHeroId();
			List<Integer> embattleList = player.getEmbattleList();
			if (null == embattleList) {
				break;
			}

			int soldierType = heroManager.getSoldierType(heroId);

			List<List<Integer>> effect = staticSkinSkill.getEffectValue();
			if (effect.size() > 0) {
				for (List<Integer> affectValue : effect) {
					if (affectValue.size() == 2) {
						Integer propType = affectValue.get(0);
						switch (propType) {
						case (SkinSkillType.EMBATTLE_ROCKET_SOLDIERNUM):
							if (embattleList.contains(heroId) && soldierType == SoldierType.ROCKET_TYPE) {
								property.add(new Property(0, 0, affectValue.get(1)));
							}
							break;
						case (SkinSkillType.EMBATTLE_ATTACK):
							if (embattleList.contains(heroId)) {
								property.add(new Property(affectValue.get(1), 0, 0));
							}
							break;
						case (SkinSkillType.EMBATTLE_SOLDIERNUM):
							if (embattleList.contains(heroId)) {
								property.add(new Property(0, 0, affectValue.get(1)));
							}
							break;
						case (SkinSkillType.EMBATTLE_DEFENCE):
							if (embattleList.contains(heroId)) {
								property.add(new Property(0, affectValue.get(1), 0));
							}
							break;
						case (SkinSkillType.EMBATTLE_TANK_ATTACK):
							if (embattleList.contains(heroId) && soldierType == SoldierType.TANK_TYPE) {
								property.add(new Property(affectValue.get(1), 0, 0));
							}
							break;
						case (SkinSkillType.EMBATTLE_ROCKET_DEFENCE):
							if (embattleList.contains(heroId) && soldierType == SoldierType.ROCKET_TYPE) {
								property.add(new Property(0, affectValue.get(1), 0));
							}
							break;
						case (SkinSkillType.EMBATTLE_ROCKET_ATTACK):
							if (embattleList.contains(heroId) && soldierType == SoldierType.ROCKET_TYPE) {
								property.add(new Property(affectValue.get(1), 0, 0));
							}
							break;
						}
					}
				}
			}
		}
		return property;
	}

	public Property getProperty(Player player, Hero hero, Property property) {
		if (player == null || hero == null) {
			return property;
		}

		Map<Integer, CommandSkin> commandSkins = player.getCommandSkins();
		if (commandSkins.size() == 0) {
			return property;
		}

		Iterator<CommandSkin> iterator = commandSkins.values().iterator();
		while (iterator.hasNext()) {
			CommandSkin commandSkin = iterator.next();
			if (commandSkin == null) {
				break;
			}

			StaticBaseSkin staticBaseSkin = staticBaseSkinMgr.getStaticBaseSkin(commandSkin.getKeyId());
			if (null == staticBaseSkin) {
				break;
			}

			int star = commandSkin.getStar();
			StaticSkinSkill staticSkinSkill = staticBaseSkinMgr.getStaticSkinSkill(commandSkin.getKeyId(), star);
			if (null == staticSkinSkill) {
				break;
			}

			int heroId = hero.getHeroId();
			List<Integer> embattleList = player.getEmbattleList();
			if (null == embattleList) {
				break;
			}

			int soldierType = heroManager.getSoldierType(heroId);

			List<List<Integer>> effect = staticSkinSkill.getEffectValue();
			if (effect.size() > 0) {
				for (List<Integer> affectValue : effect) {
					if (affectValue.size() == 2) {
						Integer propType = affectValue.get(0);
						switch (propType) {
						case (SkinSkillType.EMBATTLE_ROCKET_SOLDIERNUM):
							if (soldierType == SoldierType.ROCKET_TYPE) {
								property.add(new Property(0, 0, affectValue.get(1)));
							}
							break;
						case (SkinSkillType.EMBATTLE_ATTACK):
							if (embattleList.contains(heroId)) {
								property.add(new Property(affectValue.get(1), 0, 0));
							}
							break;
						case (SkinSkillType.EMBATTLE_SOLDIERNUM):
							if (embattleList.contains(heroId)) {
								property.add(new Property(0, 0, affectValue.get(1)));
							}
							break;
						case (SkinSkillType.EMBATTLE_DEFENCE):
							if (embattleList.contains(heroId)) {
								property.add(new Property(0, affectValue.get(1), 0));
							}
							break;
						case (SkinSkillType.EMBATTLE_TANK_ATTACK):
							if (embattleList.contains(heroId) && soldierType == SoldierType.TANK_TYPE) {
								property.add(new Property(affectValue.get(1), 0, 0));
							}
							break;
						case (SkinSkillType.EMBATTLE_ROCKET_DEFENCE):
							if (embattleList.contains(heroId) && soldierType == SoldierType.ROCKET_TYPE) {
								property.add(new Property(0, affectValue.get(1), 0));
							}
							break;
						case (SkinSkillType.EMBATTLE_ROCKET_ATTACK):
							if (embattleList.contains(heroId) && soldierType == SoldierType.ROCKET_TYPE) {
								property.add(new Property(affectValue.get(1), 0, 0));
							}
							break;
						}
					}
				}
			}
		}
		return property;
	}

	/**
	 * 获取主城皮肤技能加成
	 *
	 * @param hero
	 * @param effectType
	 * @return
	 */
	public List<Float> getSkinSkillEffect(Player player, int effectType) {
		List<Float> skinSkills = new ArrayList<Float>();
		Map<Integer, CommandSkin> commandSkins = player.getCommandSkins();
		if (commandSkins == null || commandSkins.size() == 0) {
			return skinSkills;
		}

		Iterator<CommandSkin> commandSkinIterator = commandSkins.values().iterator();
		while (commandSkinIterator.hasNext()) {
			CommandSkin commandSkin = commandSkinIterator.next();
			if (commandSkin == null) {
				break;
			}

			StaticBaseSkin staticBaseSkin = staticBaseSkinMgr.getStaticBaseSkin(commandSkin.getKeyId());
			if (null == staticBaseSkin) {
				break;
			}

			int star = commandSkin.getStar();
			StaticSkinSkill staticSkinSkill = staticBaseSkinMgr.getStaticSkinSkill(commandSkin.getKeyId(), star);
			if (null == staticSkinSkill) {
				break;
			}

			List<List<Integer>> effect = staticSkinSkill.getEffectValue();
			if (effect.size() > 0) {
				for (List<Integer> affectValue : effect) {
					if (affectValue.size() == 2) {
						switch (effectType) {
						case (SkinSkillType.SPEED_UP_MARCH):
							skinSkills.add(affectValue.get(1) / 100f);
						}
					}
				}
			}
		}
		return skinSkills;
	}
}
