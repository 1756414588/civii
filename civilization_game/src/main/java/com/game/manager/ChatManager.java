package com.game.manager;

import com.game.chat.domain.Chat;
import com.game.chat.domain.ManChat;
import com.game.chat.domain.ManShare;
import com.game.chat.domain.SystemChat;
import com.game.constant.ChatId;
import com.game.constant.CountryConst;
import com.game.dao.p.ServerChatDao;
import com.game.dataMgr.StaticChatMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.ChatShow;
import com.game.domain.p.City;
import com.game.domain.p.CtyGovern;
import com.game.domain.s.StaticChat;
import com.game.domain.s.StaticChatShow;
import com.game.pb.BasePb.Base;
import com.game.pb.ChatPb.SynChatRq;
import com.game.pb.CommonPb;
import com.game.server.GameServer;
import com.game.service.ChatService;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class ChatManager {

	static final int MAX_CHAT_COUNT = 40;// (每个阵营保存40条)
	static final int MAX_WORLD_COUNT = 6;// 全服聊天
	static final int MAX_MAP_COUNT = 10;
	static final int MAX_VIP_COUNT = 20;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticChatMgr staticChatMgr;

	@Autowired
	private ChatService chatService;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private ServerChatDao serverChatDao;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	private ConcurrentLinkedDeque<CommonPb.Chat> world = new ConcurrentLinkedDeque<CommonPb.Chat>();

	@Getter
	private Map<Integer, ConcurrentLinkedDeque<CommonPb.Chat>> countrys = new ConcurrentHashMap<>();

	// mapId, chat
	private Map<Integer, ConcurrentLinkedDeque<CommonPb.Chat>> mapChat = new HashMap<>();

	// vip相关
	private ConcurrentLinkedDeque<CommonPb.Chat> vipChat = new ConcurrentLinkedDeque<CommonPb.Chat>();

	private List<com.game.domain.p.Chat> ServerChats = new ArrayList<com.game.domain.p.Chat>();

	@PostConstruct
	public void init() {
		ServerChats = serverChatDao.selectAllServerChats();
		ServerChats.forEach(chat -> {
			com.game.pb.CommonPb.Chat decChat = com.game.domain.p.Chat.decChat(chat);
			switch (chat.getType()) {
				case com.game.domain.p.Chat.WORLD:
					world.add(decChat);
					break;
				case com.game.domain.p.Chat.VIPCHAT:
					vipChat.add(decChat);
					break;
				case com.game.domain.p.Chat.COUNTRYS:
					countrys.computeIfAbsent(chat.getCountry(), x -> new ConcurrentLinkedDeque<>()).add(decChat);
					break;
				case com.game.domain.p.Chat.MAPCHAT:
					int mapId = worldManager.getMapId(chat.getX(), chat.getY());
					mapChat.computeIfAbsent(mapId, x -> new ConcurrentLinkedDeque()).add(decChat);
					break;
				default:
					break;
			}
		});
	}

	public CommonPb.Chat addWorldChat(Chat chat) {
		CommonPb.Chat b = chat.ser(1, 0);
		world.add(b);
		if (world.size() > MAX_WORLD_COUNT) {
			world.removeFirst();
		}
		return b;
	}

	// vip通告
	public CommonPb.Chat addVipChat(Chat chat) {
		CommonPb.Chat b = chat.ser(1, 0);
		getVipChat().add(b);
		if (getVipChat().size() > MAX_VIP_COUNT) {
			getVipChat().removeFirst();
		}
		return b;
	}

	public synchronized CommonPb.Chat addCountryChat(int country, int officerId, Chat chat) {
		CommonPb.Chat b = chat.ser(0, officerId);
		ConcurrentLinkedDeque<CommonPb.Chat> countryChat = countrys.computeIfAbsent(country, x -> new ConcurrentLinkedDeque<>());
		countryChat.add(b);
		if (countryChat.size() > MAX_CHAT_COUNT) {
			countryChat.removeFirst();
		}
		return b;
	}

	public synchronized CommonPb.Chat addCountryChat(int country, int officerId, Chat chat, long playerId) {
		CommonPb.Chat b = chat.ser(0, officerId);
		ConcurrentLinkedDeque<CommonPb.Chat> countryChat = countrys.computeIfAbsent(country, x -> new ConcurrentLinkedDeque<>());
		if (playerId == 0) {
			countryChat.add(b);
		}
		if (countryChat.size() > MAX_CHAT_COUNT) {
			countryChat.removeFirst();
		}
		return b;
	}

	public CommonPb.Chat addMapChat(int mapId, Chat chat) {
		int officeId = 0;
		if (chat instanceof ManShare) {
			ManShare manShare = (ManShare) chat;
			if (manShare.getPlayer() != null) {
				officeId = SpringUtil.getBean(CountryManager.class).getOfficeId(manShare.getPlayer());
			}
		}
		CommonPb.Chat b = chat.ser(0, officeId);
		ConcurrentLinkedDeque<CommonPb.Chat> chatList = this.mapChat.computeIfAbsent(mapId, x -> new ConcurrentLinkedDeque<>());
		chatList.add(b);
		if (chatList.size() > MAX_MAP_COUNT && !chatList.isEmpty()) {
			chatList.removeFirst();
		}
		return b;
	}

	/**
	 * 世界聊天消息(喇叭消息)
	 * <p>
	 * 大于三天不显示
	 * <p>
	 * 重新登录只显示最新的6条
	 *
	 * @return
	 */
	public List<CommonPb.Chat> getLimitWorld() {
		List<CommonPb.Chat> chatList = new LinkedList<>(world);
		int num = 0;
		Iterator<CommonPb.Chat> iterator = chatList.iterator();
		while (iterator.hasNext()) {
			CommonPb.Chat next = iterator.next();
			long time = next.getTime();
			long now = System.currentTimeMillis();
			long currentTime = now - time;
			if (currentTime >= TimeHelper.DAY_MS * 3) {
				iterator.remove();
				continue;
			}
			if (num >= 6) {
				iterator.remove();
			}
			num++;
		}
		return chatList;
	}

	public ConcurrentLinkedDeque<CommonPb.Chat> getWorld() {
		return world;
	}

	public ConcurrentLinkedDeque<CommonPb.Chat> getCountryChat(int country) {
		if(countrys.containsKey(country)) {
			return countrys.get(country);
		}
		return null;
	}

	public ConcurrentLinkedDeque<CommonPb.Chat> getMapChat(int mapId) {
		if(mapChat.containsKey(mapId)) {
			return mapChat.get(mapId);
		}
		return null;
	}

	/**
	 * 发送走马灯
	 *
	 * @param chatId
	 * @param params
	 */
	public void sendWorldChat(int chatId, String... params) {
		StaticChat staticChat = staticChatMgr.getChat(chatId);
		if (staticChat == null || staticChat.getType() == 0) {
			return;
		}
		SystemChat systemChat = createSysChat(chatId, params);
		chatService.sendChat(systemChat, staticChat);
	}

	/**
	 * 发送对应渠道走马灯
	 *
	 * @param chatId
	 * @param channel
	 * @param params
	 */
	public void sendChannelWorldChat(int chatId, int channel, String... params) {
		StaticChat staticChat = staticChatMgr.getChat(chatId);
		if (staticChat == null || staticChat.getType() == 0) {
			return;
		}
		SystemChat systemChat = createSysChat(chatId, params);
		chatService.sendChannelChat(channel, systemChat, staticChat);

	}

	public void sendVipMsg(int chatId, String... params) {
		StaticChat staticChat = staticChatMgr.getChat(chatId);
		if (staticChat == null || staticChat.getType() == 0) {
			return;
		}
		if (!isVipOk(staticChat.getChatId())) {
			return;
		}
		SystemChat systemChat = createSysChat(chatId, params);
		chatService.sendVipChat(systemChat, staticChat);
	}

	public boolean isVipOk(int chatId) {
		return chatId == ChatId.BUY_VIP_GIFTS || chatId == ChatId.VIP_LEVEL || chatId == ChatId.FIRST_PAY;
	}

	/**
	 * 发送国家公告
	 *
	 * @param countryId
	 * @param chatId
	 * @param params
	 */
	public void sendCountryChat(int countryId, int chatId, String... params) {
		StaticChat staticChat = staticChatMgr.getChat(chatId);
		if (staticChat == null || staticChat.getType() == 0) {
			return;
		}
		SystemChat systemChat = createSysChat(chatId, params);
		CommonPb.Chat b = addCountryChat(countryId, 0, systemChat);

		SynChatRq.Builder builder = SynChatRq.newBuilder();
		builder.setChat(b);
		Base.Builder msg = PbHelper.createSynBase(SynChatRq.EXT_FIELD_NUMBER, SynChatRq.ext, builder.build());

		playerManager.getOnlinePlayer().forEach(next -> {
			if (next.isLogin && next.getCountry() == countryId && next.getLevel() >= staticChat.getLimitLevel()) {
				GameServer.getInstance().sendMsgToPlayer(next, msg);
			}
		});
	}

	public CommonPb.Chat sendCountryShare(Player player, Chat chat) {
		return sendCountryShare(player.getCountry(), chat);
	}

	public CommonPb.Chat sendCountryShare(int country, Chat chat) {
		int officeId = 0;
		if (chat instanceof ManShare) {
			ManShare manShare = (ManShare) chat;
			if (manShare.getPlayer() != null) {
				officeId = SpringUtil.getBean(CountryManager.class).getOfficeId(manShare.getPlayer());
			}
		}
		CommonPb.Chat b = addCountryChat(country, officeId, chat);

		SynChatRq.Builder builder = SynChatRq.newBuilder();
		builder.setChat(b);
		Base.Builder msg = PbHelper.createSynBase(SynChatRq.EXT_FIELD_NUMBER, SynChatRq.ext, builder.build());
		playerManager.getOnlinePlayer().forEach(next -> {
			if (next.isLogin && next.getCountry() == country) {
				GameServer.getInstance().sendMsgToPlayer(next, msg);
			}
		});

		return b;
	}

	/**
	 * 通知玩家发起国战
	 *
	 * @param caller
	 * @param city
	 * @param mapId
	 * @param x
	 * @param y
	 */
	public void synCountryWar(Player caller, City city, int mapId, int x, int y) {
		// <font
		// color=#ffffff>我国的%s对%s%s%s发起了国战。养兵千日，用兵一时，一鸣惊人正在今朝，请给位指挥官加入！</font><font
		// color=#c5f0ff event=click>[点击进入国家战争参战]</font>
		String[] params = new String[5];
		params[0] = caller.getNick();
		params[1] = String.valueOf(city.getCountry());
		params[2] = String.valueOf(mapId);
		params[3] = String.valueOf(city.getCityId());
		params[4] = String.format("%s,%s", x, y);
		Chat aChat = createSysChat(ChatId.ATTACK_COUNTRY, params);

		Chat bChat = null;
		if (city.getCountry() != 0) {
			String[] p = new String[5];
			p[0] = String.valueOf(caller.getCountry());
			p[1] = caller.getNick();
			p[2] = String.valueOf(mapId);
			p[3] = String.valueOf(city.getCityId());
			p[4] = String.format("%s,%s", x, y);
			bChat = createSysChat(ChatId.COUNTRY_ATTACK, p);
		}
		synMapCountryWar115(mapId, caller.getCountry(), aChat);
		if (city.getCountry() != 0) {
			synMapCountryWar105(mapId, city.getCountry(), bChat);
		}

	}

	/**
	 * 系统播报
	 *
	 * @param chatId
	 * @param params
	 */
	public SystemChat createSysChat(int chatId, String... params) {
		SystemChat systemChat = new SystemChat();
		systemChat.setChatId(chatId);
		systemChat.setTime(System.currentTimeMillis());
		systemChat.setParam(params);
		StaticChat staticChat = staticChatMgr.getChat(chatId);
		if (staticChat != null) {
			systemChat.setStyle(staticChat.getType());
		}
		return systemChat;
	}

	/**
	 * 玩家分享的聊天信息
	 *
	 * @param player
	 * @param chatId
	 * @param params
	 * @return
	 */
	public ManShare createManShare(Player player, int chatId, String... params) {
		ManShare manShare = new ManShare();
		manShare.setPlayer(player);
		manShare.setTime(System.currentTimeMillis());
		manShare.setChatId(chatId);
		manShare.setParam(params);
		return manShare;
	}

	/**
	 * 分享邮件
	 *
	 * @param player
	 * @param chatId
	 * @param mailKeyId
	 * @param param
	 * @return
	 */
	public ManShare createManShare(Player player, int chatId, int mailKeyId, String... param) {
		ManShare manShare = new ManShare();
		manShare.setPlayer(player);
		manShare.setTime(System.currentTimeMillis());
		manShare.setChatId(chatId);
		manShare.setParam(param);
		if (mailKeyId != 0) {
			manShare.setMailKeyId(mailKeyId);
		}
		return manShare;
	}

	/**
	 * 聊天
	 *
	 * @param player
	 * @param msg
	 * @return
	 */
	public ManChat createManChat(Player player, String msg) {
		ManChat manChat = new ManChat();
		manChat.setPlayer(player);
		manChat.setTime(System.currentTimeMillis());
		manChat.setMsg(msg);
		return manChat;
	}

	public void updateChatShow(int type, int id, Player player) {
		StaticChatShow config = staticChatMgr.getChatShow(type, id);
		if (config == null) {
			return;
		}
		WorldData worldData = worldManager.getWolrdInfo();
		Map<Integer, ChatShow> chatShowMap = worldData.getChatShowMap();
		int keyId = config.getKeyId();
		ChatShow chatShow = chatShowMap.get(keyId);
		if (chatShow == null) {
			chatShow = new ChatShow(keyId, 1);
			chatShowMap.put(keyId, chatShow);
			handleChatShow(type, id, chatShow, player, config.getChatId());
		} else {
			if (type != 1) { // 非科技类的
				// 检查上限
				if (chatShow.hasLord(player.roleId)) {
					return;
				}

				if (chatShow.getNumber() >= config.getNumber()) {
					return;
				}
			}
			chatShow.setNumber(chatShow.getNumber() + 1);
			handleChatShow(type, id, chatShow, player, config.getChatId());
		}
	}

	public void handleChatShow(int type, int id, ChatShow chatShow, Player player, int chatId) {
//        int chatId = 0;
//        if (type == 1) {
//            chatId = ChatId.TECH_RANK;
//        } else if (type == 2) {
//            chatId = ChatId.EQUIP_RANK;
//        } else if (type == 3) {
//            chatId = ChatId.BUY_VIP_GIFTS;
//        } else if (type == 4) {
//            chatId = ChatId.VIP_LEVEL;
//        } else if (type == 5) {
//            chatId = ChatId.PASS_MISSION;
//        }

		chatShow.addLord(player.roleId);
		if (chatId == ChatId.BUY_VIP_GIFTS || chatId == ChatId.VIP_LEVEL) {
			sendVipMsg(chatId, player.getNick(), String.valueOf(chatShow.getNumber()), String.valueOf(id));
		} else if (type == 1) {
			sendWorldChat(chatId, player.getNick(), String.valueOf(id));
		} else {
			sendWorldChat(chatId, player.getNick(), String.valueOf(chatShow.getNumber()), String.valueOf(id));
		}

	}

	public void governLoginChat(Player player, CtyGovern govern) {
		if (govern == null || govern.getGovernId() == 4) {
			return;
		}

		String params[] = new String[3];
		params[0] = String.valueOf(player.getCountry());
		params[1] = String.valueOf(govern.getGovernId());
		params[2] = player.getNick();

		if (govern.getGovernId() == CountryConst.GOVERN_KING) {// 国王，全服公告
			sendWorldChat(ChatId.KING_ONLINE, params);
		} else if (govern.getGovernId() == CountryConst.GOVERN_PRIME) {// 首相，本国公告
			SystemChat chat = createSysChat(ChatId.KING_ONLINE, params);
			sendCountryShare(player, chat);
		} else if (govern.getGovernId() == CountryConst.GOVERN_ADVISER) {// 国务卿,本国公告
			SystemChat chat = createSysChat(ChatId.KING_ONLINE, params);
			sendCountryShare(player, chat);
		}
	}

	public void governVoteChat(int country, String nick, CtyGovern govern) {
		if (govern == null || govern.getGovernId() == 4) {
			return;
		}

		String params[] = new String[3];
		params[0] = nick;
		params[1] = String.valueOf(country);
		params[2] = String.valueOf(govern.getGovernId());

		if (govern.getGovernId() == CountryConst.GOVERN_KING) {// 国王，全服公告
			sendWorldChat(ChatId.VOTE_GOVERN, params);
		} else if (govern.getGovernId() == CountryConst.GOVERN_PRIME) {// 首相，本国公告
			SystemChat chat = createSysChat(ChatId.VOTE_GOVERN, params);
			sendCountryShare(country, chat);
		} else if (govern.getGovernId() == CountryConst.GOVERN_ADVISER) {// 国务卿,本国公告
			SystemChat chat = createSysChat(ChatId.VOTE_GOVERN, params);
			sendCountryShare(country, chat);
		}
	}

	public Map<Integer, ConcurrentLinkedDeque<CommonPb.Chat>> getMapChat() {
		return mapChat;
	}

	public void setMapChat(Map<Integer, ConcurrentLinkedDeque<CommonPb.Chat>> mapChat) {
		this.mapChat = mapChat;
	}

	// 101, 102 [区域通知]
	public void sendMapShare(Chat chat, int mapId, int country) {
		CommonPb.Chat b = addMapChat(mapId, chat);
		SynChatRq.Builder builder = SynChatRq.newBuilder();
		builder.setChat(b);
		Base.Builder msg = PbHelper.createSynBase(SynChatRq.EXT_FIELD_NUMBER, SynChatRq.ext, builder.build());

		Iterator<Player> it = playerManager.getOnlinePlayer().iterator();
		ChannelHandlerContext ctx;
		while (it.hasNext()) {
			Player next = it.next();
			if (next == null) {
				continue;
			}

			int playerMapId = worldManager.getMapId(next);
			if (playerMapId == 0) {
				continue;
			}

			if (playerMapId != mapId) {
				continue;
			}

			if (next.getCountry() != country) {
				continue;
			}

			if (next.isLogin) {
//				ctx = next.ctx;
//				if (ctx != null) {
					GameServer.getInstance().sendMsgToPlayer(next, msg);
//				}
			}
		}
	}

	/**
	 * %s国%s对我国的%s%s发起了国战。敌国来犯，我城势单力孤，还请诸位同胞申出援手
	 */
	public void synMapCountryWar105(int mapId, int cityCountry, Chat defendChat) {
		Base.Builder msg = null;
		if (cityCountry != 0) {
			CommonPb.Chat c = addMapChat(mapId, defendChat);
			SynChatRq.Builder cbuilder = SynChatRq.newBuilder();
			cbuilder.setChat(c);
			msg = PbHelper.createSynBase(SynChatRq.EXT_FIELD_NUMBER, SynChatRq.ext, cbuilder.build());
		}


		Iterator<Player> it = playerManager.getOnlinePlayer().iterator();
		ChannelHandlerContext ctx;
		while (it.hasNext()) {
			Player next = it.next();
			if (next == null) {
				continue;
			}

			if (!next.isLogin) {
				continue;
			}

			if (next.getGateId() == null) {
				continue;
			}

			int playerMapId = worldManager.getMapId(next);
			if (playerMapId == 0) {
				continue;
			}

			if (playerMapId != mapId) {
				continue;
			}

//			ctx = next.ctx;
			if (next.getCountry() == cityCountry) {
				GameServer.getInstance().sendMsgToPlayer(next, msg);
			}
		}
	}

	// 我国的%s对%s%s%s发起了国战。养兵千日，用兵一时，一鸣惊人正在今朝，请给位主公加入 ok [区域,测试1ok，测试2ok]
	public void synMapCountryWar115(int mapId, int callerCountry, Chat attackChat) {
		CommonPb.Chat b = addMapChat(mapId, attackChat);
		SynChatRq.Builder builder = SynChatRq.newBuilder();
		builder.setChat(b);
		Base.Builder msg = PbHelper.createSynBase(SynChatRq.EXT_FIELD_NUMBER, SynChatRq.ext, builder.build());
		Iterator<Player> it = playerManager.getOnlinePlayer().iterator();
		while (it.hasNext()) {
			Player next = it.next();
			if (next == null) {
				continue;
			}

			if (!next.isLogin) {
				continue;
			}

			if (next.getChannelId() == -1) {
				continue;
			}

			int playerMapId = worldManager.getMapId(next);
			if (playerMapId == 0) {
				continue;
			}

			if (playerMapId != mapId) {
				continue;
			}

			if (next.getCountry() == callerCountry) {
				GameServer.getInstance().sendMsgToPlayer(next, msg);
			}
		}
	}

	public ConcurrentLinkedDeque<CommonPb.Chat> getVipChat() {
		return vipChat;
	}

	public void cleanChat() {
		serverChatDao.cleanChat();
	}

	public int addServerChat(com.game.domain.p.Chat chat) {
		return serverChatDao.insertSelective(chat);
	}
}
