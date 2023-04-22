package com.game.manager;

import com.game.dataMgr.StaticPersonalityMgr;
import com.game.domain.Player;
import com.game.domain.p.Frame;
import com.game.domain.s.StaticPersonality;
import com.game.enumerate.FrameState;
import com.game.pb.RolePb;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 *
 * @date 2021/1/26 14:25
 * @description
 */
@Component
public class PersonalityManager {

	@Autowired
	private StaticPersonalityMgr staticPersonalityMgr;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private ActivityManager activityManager;

	/**
	 * 过期检测
	 */
	public void overdue() {
		Date now = new Date();
		playerManager.getAllPlayer().values().parallelStream().forEach(e -> {
			e.getFrameMap().values().stream().filter(f -> {
				if (f.getExpireTime() == null) {
					return false;
				}
				return now.after(f.getExpireTime());
			}).forEach(f -> {
				f.setState(FrameState.lock);
				if (f.getIndex() == 0) { // 头像框
					e.getLord().setHeadIndex(0);
				} else {
					e.getLord().setChatIndex(0);
				}
				initPlayer(e);
			});
		});
	}

	public void initPlayer(Player player) {
		staticPersonalityMgr.getDataMap().values().forEach(e -> {
			Frame frame = player.getFrameMap().get(e.getId());
			if (frame == null) {
				frame = Frame.builder().id(e.getId()).state(FrameState.lock).index(e.getType()).build();
				player.getFrameMap().put(e.getId(), frame);
			}
			if (e.getOpenCond().get(0) == 2 && e.getOpenCond().get(1) == 1) {
				frame.setState(FrameState.unlock);
			}
		});
		int headIndex = player.getLord().getHeadIndex();
		int chatIndex = player.getLord().getChatIndex();
		boolean def = false;
		if (headIndex == 0) {
			def = true;
			// 获取下默认
			headIndex = staticPersonalityMgr.getByType(1).stream().filter(e -> e.getOpenCond().get(0) == 0).findFirst().get().getId();
			player.getLord().setHeadIndex(headIndex);
			Frame frame = player.getFrameMap().get(headIndex);
			if (frame != null) {
				frame.setState(FrameState.unlock);
				frame.setShow(true);
			}
		}
		if (chatIndex == 0) {
			def = true;
			// 获取下默认
			chatIndex = staticPersonalityMgr.getByType(2).stream().filter(e -> e.getOpenCond().get(0) == 0).findFirst().get().getId();
			player.getLord().setChatIndex(chatIndex);
			Frame frame = player.getFrameMap().get(chatIndex);
			if (frame != null) {
				frame.setState(FrameState.unlock);
				frame.setShow(true);
			}
		}
		if (!def) {
			loginPush(player);
		}
	}

	public List<StaticPersonality> getHeadList() {
		return staticPersonalityMgr.getByType(1);
	}

	public List<StaticPersonality> getChatList() {
		return staticPersonalityMgr.getByType(2);
	}

	public void addAward(Player player, int id, int count) {
		Frame frame = player.getFrameMap().get(id);
		StaticPersonality staticPersonality = staticPersonalityMgr.get(id);
		if (staticPersonality == null) {
			return;
		}
		frame.setState(FrameState.unlock);
		frame.expireTime(staticPersonality.getTime() * TimeHelper.HOUR_MS * count);
		RolePb.SynFrameRq.Builder builder = RolePb.SynFrameRq.newBuilder();
		builder.addState(staticPersonality.getType());
		SynHelper.synMsgToPlayer(player, RolePb.SynFrameRq.EXT_FIELD_NUMBER, RolePb.SynFrameRq.ext, builder.build());
	}

	/**
	 * @param player
	 * @param state  0:默认; 1:vip; 3:母巢
	 */
	public void checkIconOpen(Player player, int state) {
		List<StaticPersonality> list = Lists.newArrayList(staticPersonalityMgr.getDataMap().values());
		boolean isReset = true;
		for (StaticPersonality staticPersonality : list) {
			if (staticPersonality.getOpenCond().size() <= 1) {
				continue;
			}
			if (staticPersonality.getOpenCond().get(0) != state) {
				continue;
			}
			// 只检查玩家正在使用的是否合法
			if (staticPersonality.getType() == 1) {
				if (player.getLord().getChatIndex() != staticPersonality.getId()) {
					continue;
				}
			} else {
				if (player.getLord().getHeadIndex() != staticPersonality.getId()) {
					continue;
				}
			}
			Frame frame = player.getFrameMap().get(staticPersonality.getId());
			switch (state) {
			case 1: // vip
				if (player.getVip() >= staticPersonality.getOpenCond().get(1)) {
					if (frame != null) {
						frame.setState(FrameState.unlock);
						frame.expireTime(staticPersonality.getTime() * TimeHelper.HOUR_MS);
					}
				} else {
					if (frame != null) {
						frame.setState(FrameState.lock);
						if (staticPersonality.getType() == 1) {
							// 头像框
							player.getLord().setHeadIndex(0);
						} else if (staticPersonality.getType() == 2) {
							// 聊天框
							player.getLord().setChatIndex(0);
						}
						isReset = true;
					}
				}
				break;
			case 3:
				if (frame != null) {
					frame.setState(FrameState.unlock);
					frame.expireTime(staticPersonality.getTime() * TimeHelper.HOUR_MS);
				}
				break;
			}
		}
		if (isReset) {
			initPlayer(player);
		}
	}

	public StaticPersonality get(int id) {
		return staticPersonalityMgr.get(id);
	}

	/**
	 * @param roleId
	 * @param vip
	 */
	public void isPush(long roleId, int vip) {
		Player player = playerManager.getPlayer(roleId);
		if (player == null) {
			return;
		}
		List<StaticPersonality> list = Lists.newArrayList(staticPersonalityMgr.getDataMap().values());
		RolePb.SynFrameRq.Builder builder = RolePb.SynFrameRq.newBuilder();
		for (StaticPersonality staticPersonality : list) {
			if (staticPersonality.getOpenCond().size() <= 1 || staticPersonality.getOpenCond().get(0) != 1) {
				continue;
			}
			if (vip >= staticPersonality.getOpenCond().get(1)) {
				Frame frame = player.getFrameMap().get(staticPersonality.getId());
				if (frame != null) {
					if (frame.getState() != FrameState.unlock) {
						frame.setState(FrameState.unlock);
						frame.expireTime(staticPersonality.getTime() * TimeHelper.HOUR_MS);
						builder.addState(staticPersonality.getType());

					}
				}
			}
		}
		SynHelper.synMsgToPlayer(player, RolePb.SynFrameRq.EXT_FIELD_NUMBER, RolePb.SynFrameRq.ext, builder.build());
	}

	/**
	 * 登陆确认下是否推送
	 *
	 * @param player
	 */
	private void loginPush(Player player) {
		List<StaticPersonality> list = Lists.newArrayList(staticPersonalityMgr.getDataMap().values());
		RolePb.SynFrameRq.Builder builder = RolePb.SynFrameRq.newBuilder();
		for (StaticPersonality staticPersonality : list) {
			if (staticPersonality.getOpenCond().size() <= 1) {
				continue;
			}
			Frame frame = player.getFrameMap().get(staticPersonality.getId());
			if (frame != null) {
				if (frame.getState() == FrameState.unlock && !frame.isShow() && !builder.getStateList().contains(staticPersonality.getType())) {
					builder.addState(staticPersonality.getType());
				}
			}
		}
		if (!builder.getStateList().isEmpty()) {
			SynHelper.synMsgToPlayer(player, RolePb.SynFrameRq.EXT_FIELD_NUMBER, RolePb.SynFrameRq.ext, builder.build());
		}
	}
}
