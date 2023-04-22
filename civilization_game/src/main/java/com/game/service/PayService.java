package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.ActPassPortTaskType;
import com.game.constant.ActSevenConst;
import com.game.constant.ActivityConst;
import com.game.constant.AwardType;
import com.game.constant.ChatShowType;
import com.game.constant.DailyTaskId;
import com.game.constant.GameError;
import com.game.constant.LordPropertyType;
import com.game.constant.MailId;
import com.game.constant.Reason;
import com.game.constant.UcCodeEnum;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticVipMgr;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.p.ActivityRecord;
import com.game.domain.Award;
import com.game.domain.p.Lord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActEquipUpdate;
import com.game.domain.s.StaticActPayCard;
import com.game.domain.s.StaticActPayGift;
import com.game.domain.s.StaticActPayMoney;
import com.game.domain.s.StaticLimitGift;
import com.game.domain.s.StaticPay;
import com.game.domain.s.StaticPayCalculate;
import com.game.domain.s.StaticPayPassPort;
import com.game.domain.s.StaticPayPoint;
import com.game.domain.s.StaticResourceGift;
import com.game.log.consumer.EventManager;
import com.game.manager.ActivityManager;
import com.game.manager.ChatManager;
import com.game.manager.DailyTaskManager;
import com.game.manager.EquipManager;
import com.game.manager.PayManager;
import com.game.manager.PlayerManager;
import com.game.manager.ServerManager;
import com.game.manager.SurpriseGiftManager;
import com.game.message.handler.ClientHandler;
import com.game.pay.channel.ChannelConsts;
import com.game.pb.ActivityPb.SynResourceGiftShow;
import com.game.pb.ActivityPb.SynResourceGiftShow.Builder;
import com.game.pb.PayPb;
import com.game.pb.PayPb.GetOrderNumRq;
import com.game.pb.PayPb.GetOrderNumRs;
import com.game.pb.RolePb;
import com.game.server.exec.HttpExecutor;
import com.game.uc.Message;
import com.game.uc.PayOrder;
import com.game.uc.Server;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PayService {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private StaticVipMgr staticVipDataMgr;

	@Autowired
	private StaticActivityMgr staticActivityMgr;

	@Autowired
	private ChatManager chatManager;

	@Autowired
	private PayManager payManager;

	@Autowired
	private UcHttpService httpService;

	@Autowired
	private ServerManager serverManager;

	@Autowired
	private EquipManager equipManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private ActivityService activityService;
	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	private SurpriseGiftManager surpriseGiftManager;

	public Boolean payBack(PayOrder payOrder) {
		Boolean flag = false;
		try {
			long roleId = payOrder.getRoleId();
			Player player = playerManager.getPlayer(roleId);
			if (player == null) {
				return flag;
			}
			int rechargeType = payOrder.getProductType();
			if (payLogic(payOrder, player, rechargeType)) {
				payOrder.setRealAmount(payOrder.getRealAmount());
				payOrder.setStatus(PayOrder.ORDER_SUCCESS);
				payOrder.setFinishTime(new Date());
				payManager.updateOrder(payOrder);
				flag = true;
			}
			activityService.checkActLuxuryGift(player);
			if (flag) {
				dailyTaskManager.record(DailyTaskId.RECHARGE, player, payOrder.getRealAmount());
				ActivityEventManager.getInst().activityTip(EventEnum.PAY, player, 0, 0);// 充值相关所有的红点提示
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * @param target
	 * @param amount
	 * @param topup
	 * @param extraGold
	 * @param serialId
	 * @param reason
	 * @return
	 */
	public boolean addPayGold(Player target, int amount, int topup, int extraGold, int vipExp, String serialId, int payType, int productId, int reason) {
		if (amount <= 0) {
			return false;
		}

		Lord lord = target.getLord();

		playerManager.addGold(target, topup + extraGold, Reason.PAY);
		lord.setTopup(lord.getTopup() + amount);// 设置充值总金额

		int oldVip = lord.getVip();
		playerManager.addAward(target, AwardType.LORD_PROPERTY, LordPropertyType.VIP_EXP, vipExp, Reason.PAY, amount, topup, serverManager.getServerId());

		if (lord.getVip() > oldVip) {// 世界分享
			try {
				chatManager.updateChatShow(ChatShowType.VIP_LEVEL, lord.getVip(), target);
			} catch (Exception ex) {
				LogHelper.ERROR_LOGGER.error(ex.getMessage(), ex);
			}
		}
		int firstPay = target.getLord().getFirstPay();
		if (firstPay == 0) {
			firstPayAward(target);
			target.getLord().setFirstPay(1);
			target.getLord().setTvip(3);

		}

		playerManager.synGoldToPlayer(target, topup + extraGold, vipExp, payType, productId, null, serialId, false);
		playerManager.synBeautySTimes(target);

		return true;
	}

	public boolean addPayGift(Player target, int amount, int vipExp, String serialId, int payType, int productId, List<Award> awards, int reason, boolean flag) {
		if (amount <= 0) {
			return false;
		}

		Lord lord = target.getLord();
		lord.setTopup(lord.getTopup() + amount);

		int oldVip = lord.getVip();
//		playerManager.addAward(target, AwardType.LORD_PROPERTY, LordPropertyType.VIP_EXP, vipExp, Reason.PAY);
		playerManager.addAward(target, AwardType.LORD_PROPERTY, LordPropertyType.VIP_EXP, vipExp, Reason.PAY, amount, vipExp, serverManager.getServerId());

		if (lord.getVip() > oldVip) {// 世界分享
			try {
				chatManager.updateChatShow(ChatShowType.VIP_LEVEL, lord.getVip(), target);
			} catch (Exception ex) {
				LogHelper.ERROR_LOGGER.error(ex.getMessage(), ex);
			}
		}
		if (payType != RECHAR_4) {
			if (target.getLord().getFirstPay() == 0) {
				target.getLord().setFirstPay(1);
			}
		}
		playerManager.synGoldToPlayer(target, 0, vipExp, payType, productId, awards, serialId, flag);
		return true;
	}

	private void firstPayAward(Player player) {

	}

	final static int RECHAR_1 = 1;
	final static int RECHAR_2 = 2;
	final static int RECHAR_3 = 3;
	final static int RECHAR_4 = 4;
	final static int RECHAR_5 = 5;
	final static int RECHAR_6 = 6;
	final static int RECHAR_7 = 7;
	// 美女礼包
	final static int RECHAR_8 = 8;
	// 资源礼包
	final static int RECHAR_10 = 10;
	// 春节特惠
	final static int RECHAR_11 = 11;

	/**
	 * @param req
	 * @param player
	 * @param rechargeType 1.直冲 2.老版本特价礼包 3各种卡 4 每日特惠 5.通行证 6.惊喜好礼 7新版特价礼包 8美女礼包 9资源礼包
	 * @return
	 */
	public boolean payLogic(final PayOrder req, Player player, int rechargeType) {
		int vipExp = 0;
		int amount = req.getPayAmount() / 100;
		boolean flags = false;

		switch (rechargeType) {
			case RECHAR_1:
				StaticPay staticPay = staticVipDataMgr.getPayStaticPay(req.getProductId());
				int extraGold = staticPay.getExtraGold();// 额外赠送钻石数量
				// int relTopup = staticPay.getTopup();//充值钻石数量
				int topup = staticPay.getTopup();// 充值钻石数量
				vipExp = staticPay.getVipExp();// 充值vip经验数量

				// 记录每个计费点是否是首次充值(首充双倍钻石)
				List<Integer> payStatus = player.getLord().getPayStatusList();
				if (null == payStatus || payStatus.size() == 0) {
					payStatus = new ArrayList<>();
					topup = 2 * topup;
					payStatus.add(req.getProductId());
				} else {
					if (!payStatus.contains(req.getProductId())) {
						topup = 2 * topup;
						payStatus.add(req.getProductId());
					}
				}
				player.getLord().setPayStatusList(payStatus);
				if (addPayGold(player, amount, topup, extraGold, vipExp, req.getSpOrderId(), req.getProductType(), req.getProductId(), Reason.PAY)) {
//                    playerManager.sendNormalMail(player, MailId.RECHARGE_MAIL, String.valueOf(topup));

					flags = true;
				}
				break;
			default:
				boolean flag = false;
				boolean flagBag = false;
				Integer giftId = req.getProductId();
				List<Award> awards = null;
				List<List<Integer>> sellList = new ArrayList<>();
				StaticActPayMoney payMoney;
				boolean sellFlag = false;
				int cons = 0;
				switch (rechargeType) {
					case RECHAR_2:
					case RECHAR_8:
						StaticActPayGift payGift = staticActivityMgr.getPayGift(giftId);
						if (null == payGift) {
							return false;
						}
						vipExp = payGift.getVipExp();
						flag = activityManager.actPayGift(player, giftId);
						List<Award> allList = new ArrayList<>();
						List<Award> awardList = new ArrayList<>();
						sellList = payGift.getSellList();
						int equipCount = 0;
						for (List<Integer> e : sellList) {
							if (e == null) {
								continue;
							}
							int type = e.get(0);
							int id = e.get(1);
							int count = e.get(2);
							if (type == AwardType.EQUIP && count > 0) {
								equipCount += count;
								allList.add(new Award(type, id, count));
							} else {
								awardList.add(new Award(type, id, count));
							}
						}
						allList.addAll(PbHelper.finilAward(PbHelper.createAwardList(awardList)));
						if (sellList.size() > 0) {
							awards = new ArrayList<>();
							if (equipManager.getFreeSlot(player) < equipCount) {
								playerManager.sendAttachPbMail(player, PbHelper.createAwardList(allList), MailId.PAY_GIFT_MAIL, player.getNick(), String.valueOf(payGift.getPayGiftId()));
								flagBag = true;
							} else {
								sellFlag = true;
								cons = ActivityConst.ACT_DAY_PAY;
							}
						}
						break;
					case RECHAR_3:
						StaticActPayCard payCard = staticActivityMgr.getPayCard(giftId);
						if (null == payCard) {
							return false;
						}
						vipExp = payCard.getVipExp();
						flag = activityManager.actPayCard(player, giftId);
						break;
					case RECHAR_4:
						payMoney = staticActivityMgr.getPayMoney(giftId);
						if (null == payMoney) {
							return false;
						}
						vipExp = payMoney.getVipExp();
						flag = activityManager.actPayMoney(player, giftId);
						sellList = payMoney.getSellList();
						sellFlag = true;
						cons = ActivityConst.ACT_DAY_PAY;
						break;
					case RECHAR_5:
						StaticPayPassPort staticPayPassPort = staticActivityMgr.getStaticPayPassPort(giftId);
						if (null == staticPayPassPort) {
							return false;
						}
						vipExp = staticPayPassPort.getVipExp();
						flag = activityManager.actPayPassPort(player, giftId);
						sellList = staticPayPassPort.getSellList();
						sellFlag = true;
						cons = ActivityConst.ACT_DAY_PAY;
						break;
					case RECHAR_6:
						StaticLimitGift staticLimitGift = staticActivityMgr.getLimitGiftByKeyId(giftId);
						if (null == staticLimitGift) {
							return false;
						}
						sellList = staticLimitGift.getAwardList();
						flag = activityManager.updSurpriseGift(player, giftId);
						sellFlag = true;
						cons = ActivityConst.ACT_SURIPRISE_GIFT;
						break;
					case RECHAR_7:
						payMoney = staticActivityMgr.getPayMoney(giftId);
						if (null == payMoney) {
							return false;
						}
						vipExp = payMoney.getVipExp();
						flag = activityManager.actPaySpecial(player, giftId);
						sellList = payMoney.getSellList();
						sellFlag = true;
						cons = ActivityConst.ACT_SPECIAL_GIFT;
						break;
					case RECHAR_10:
						StaticResourceGift resourceGift = staticActivityMgr.getStaticResourceGift(giftId);
						if (null == resourceGift) {
							return false;
						}
						sellList = resourceGift.getAwardList();
						player.getSimpleData().getResourceGiftRecord().put(giftId, System.currentTimeMillis());
						Builder builder = SynResourceGiftShow.newBuilder();
						builder.setResourceGiftShow(playerManager.getResourceGift(player));
						SynHelper.synMsgToPlayer(player, SynResourceGiftShow.EXT_FIELD_NUMBER, SynResourceGiftShow.ext, builder.build());
						flag = true;
						sellFlag = true;
						cons = Reason.RESOURCE_GIFT;
						break;
					case RECHAR_11:
						StaticLimitGift springGift = staticActivityMgr.getSpringGift(giftId);
						if (null == springGift) {
							return false;
						}
						sellList = springGift.getAwardList();
						activityManager.buySpringGift(player, springGift);
						flag = true;
						sellFlag = true;
						cons = Reason.ACT_SPRING_GIFT;
						break;
					default:
						break;

				}
				if (sellFlag && sellList.size() > 0) {
					awards = new ArrayList<>();
					for (List<Integer> list : sellList) {
						int keyId = playerManager.addAward(player, list.get(0), list.get(1), list.get(2), cons);
						if (keyId != 0) {
							awards.add(new Award(keyId, list.get(0), list.get(1), list.get(2)));
						} else {
							awards.add(new Award(list.get(0), list.get(1), list.get(2)));
						}
					}
				}
				if (flag && addPayGift(player, amount, vipExp, req.getCpOrderId(), req.getProductType(), req.getProductId(), awards, Reason.PAY, flagBag)) {
					flags = true;
				}
				break;
		}
		if (flags) {
			StaticPayCalculate staticPayCalculate = staticActivityMgr.getStaticPayCalculate(rechargeType);
			if (staticPayCalculate != null && staticPayCalculate.getActivityId() != null && !staticPayCalculate.getActivityId().isEmpty()) {
				staticPayCalculate.getActivityId().forEach(x -> {
					checkActivity(player, amount, x);
				});
			}

		}
		return flags;
	}

	private void checkActivity(Player player, int amount, Integer id) {
		// 50,53,77,82,101,108,109
		// 7,54,55,59,62,63,69,69,74,79,87,89,97,114,115,118,123,126
		// [7,50,53,54,55,62,63,69,74,77,79,82,87,89,97,101,108,109,114,115,118,123,126]
		/**
		 * 50 晶体转盘 53 阵营崛起 77 晶体转盘 82 金币转盘 101 军功转盘 108 幸运奖池 109 九宫格
		 */
		switch (id) {
			case ActivityConst.ACT_TOPUP_SERVER:
				activityManager.updActServer(id, amount * 10, 0);
				break;
			case ActivityConst.ACT_SER_PAY:
				activityManager.updActServerReMoney(player, id, amount * 10, 0);
				break;
			case ActivityConst.ACT_CONTINUOUS_RECHARGE:
				activityManager.actPayEveryDay(player, 1);
				activityManager.updActPerson(player, ActivityConst.ACT_CONTINUOUS_RECHARGE, amount * 10, 0);// 每日充值69
				break;
			case ActivityConst.ACT_SEVEN:
				activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.TOPUP, 0, amount * 10);
				// 七日狂欢之充值礼包活动
				activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.GIFT_PAY, 0, 1);
				break;
			case ActivityConst.ACT_PAY_FIRST:
				activityManager.updActPerson(player, id, amount, 0);
				if (player.getLord().getFirstPay() == 0) {
					player.getLord().setFirstPay(1);
				}
				break;
			case ActivityConst.ACT_WASH_EQUIP:
				activityManager.updActWashEquip(player, ActivityConst.TYPE_ADD, StaticActEquipUpdate.PAY_CONUT, 1, ActivityConst.ACT_WASH_EQUIP);
				break;
			case ActivityConst.ACT_PASS_PORT:
				activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.PAY_FREE_MONEY, amount);
				break;
			case ActivityConst.ACT_DAYLY_RECHARGE:
				activityManager.updateActDailyRecharge(player, ActPassPortTaskType.PAY_FREE_MONEY, amount * 10, amount);
				break;
			case ActivityConst.ACT_TOPUP_RANK:// 充值排行
				activityManager.updActRecharScore(player, amount * 10, id);
				break;
			case ActivityConst.ACT_SPRING_FESTIVAL:// 春节活动
				activityManager.updActPerson(player, id, amount, 0);
				break;
			default:
				activityManager.updActPerson(player, id, amount * 10, 0);
				break;
		}
	}

//    public void updateActRechar(Player player,int relTopup,int amount){
//
//        //7,54,55,59,62,63,69,69,74,79,87,89,97,114,115,118,123,126
//        //[7,50,53,54,55,62,63,69,74,77,79,82,87,89,97,101,108,109,114,115,118,123,126]
//        //50,53,77,82,101,108,109
//        //boolean recharge = activityManager.updActPerson(player, ActivityConst.ACT_CONTINUOUS_RECHARGE, relTopup, 0);//每日充值69
//        //activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.TOPUP, 0, relTopup);//七日狂欢74
//        //boolean payFirst = activityManager.updActPerson(player, ActivityConst.ACT_PAY_FIRST, amount, 0);//首充礼包55
//        // activityManager.updActWashEquip(player, ActivityConst.TYPE_ADD, StaticActEquipUpdate.PAY_CONUT, 1, ActivityConst.ACT_WASH_EQUIP);//// 装备精研87
//        // boolean freeMoney = activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.PAY_FREE_MONEY, amount); //更新通行证活动进度89
//        // activityManager.updateActDailyRecharge(player, ActPassPortTaskType.PAY_FREE_MONEY, relTopup, amount); //每日充值97
//        // boolean dailyPay = activityManager.actPayEveryDay(player, relTopup);// 每日充值69
//        boolean sevenRecharge = activityManager.updActPerson(player, ActivityConst.ACT_SEVEN_RECHARGE, relTopup, 0);//七日充值79
//        boolean topUp = activityManager.updActPerson(player, ActivityConst.ACT_TOPUP_RANK, relTopup, 0);//充值排行榜7
//        boolean dayPay = activityManager.updActPerson(player, ActivityConst.ACT_DAY_PAY, relTopup, 0);// 每日返利  每日特惠59
//        boolean topPerson = activityManager.updActPerson(player, ActivityConst.ACT_TOPUP_PERSON, relTopup, 0);// 充值有礼(个人)62
//        //activityManager.updActPerson(player, ActivityConst.ACT_SQUA, relTopup, NineCellConst.CELL_5);
//        if (sevenRecharge ||  topUp ||  dayPay  || topPerson ) {
//            playerManager.synActivity(player, 0, 0);
//        }
//        activityManager.updActPerson(player, ActivityConst.ACT_RAIDERS, relTopup, 0);//夺宝奇兵114
//        activityManager.updActPerson(player, ActivityConst.RE_DIAL, relTopup, 0);//充值转盘115
//        activityManager.updActPerson(player, ActivityConst.ACT_GRAND_TOTAL, relTopup, 0);//累计充值123
//        activityManager.updActPerson(player, ActivityConst.ACT_SEARCH, relTopup, 0); //寻宝之路118
//        activityManager.updActPerson(player, ActivityConst.ACT_LUCKLY_EGG, relTopup, 0);//幸运砸蛋126
//    }

//    public boolean payGiftLogic(final PayOrder req, int giftId, Player player, int rechargeType) {
//        int vipExp = 0;
//        int amount = req.getPayAmount() / 100;
//
//        if (player == null) {
//            return false;
//        }
//
//        boolean flag = false;
//        boolean flagBag = false;
//        // 充值礼包 && 月卡,季卡
//        List<Award> awards = null;
//        //老版本特价礼包，双卡大礼包 建造礼包
//        if (rechargeType == 2) {
//            StaticActPayGift payGift = staticActivityMgr.getPayGift(giftId);
//            if (null == payGift) {
//                return false;
//            }
//            vipExp = payGift.getVipExp();
//            flag = activityManager.actPayGift(player, giftId);
//            List<Award> allList = new ArrayList<>();
//            List<Award> awardList = new ArrayList<>();
//            List<List<Integer>> sellList = payGift.getSellList();
//
//            int equipCount = 0;
//            for (List<Integer> e : sellList) {
//                if (e == null) {
//                    continue;
//                }
//                int type = e.get(0);
//                int id = e.get(1);
//                int count = e.get(2);
//                if (type == AwardType.EQUIP && count > 0) {
//                    equipCount += count;
//                    allList.add(new Award(type, id, count));
//                } else {
//                    awardList.add(new Award(type, id, count));
//                }
//            }
//            allList.addAll(PbHelper.finilAward(PbHelper.createAwardList(awardList)));
//            if (sellList.size() > 0) {
//                awards = new ArrayList<>();
//                if (equipManager.getFreeSlot(player) < equipCount) {
//                    playerManager.sendAttachPbMail(player, PbHelper.createAwardList(allList), MailId.PAY_GIFT_MAIL, player.getNick(), payGift.getName());
//                    flagBag = true;
//                } else {
//
//                    for (List<Integer> list : sellList) {
//                        int keyId = playerManager.addAward(player, list.get(0), list.get(1), list.get(2), ActivityConst.ACT_DAY_PAY);
//                        if (keyId != 0) {
//                            awards.add(new Award(keyId, list.get(0), list.get(1), list.get(2)));
//                        } else {
//                            awards.add(new Award(list.get(0), list.get(1), list.get(2)));
//                        }
//                    }
//                }
//            }
//
//        } else if (rechargeType == 3) { //各种卡
//            StaticActPayCard payCard = staticActivityMgr.getPayCard(giftId);
//            if (null == payCard) {
//                return false;
//            }
//            vipExp = payCard.getVipExp();
//            flag = activityManager.actPayCard(player, giftId);
//
//        } else if (rechargeType == 4) { //每日特惠（不计入）
//            StaticActPayMoney payMoney = staticActivityMgr.getPayMoney(giftId);
//            if (null == payMoney) {
//                return false;
//            }
//            vipExp = payMoney.getVipExp();
//            flag = activityManager.actPayMoney(player, giftId);
//            List<List<Integer>> sellList = payMoney.getSellList();
//            if (sellList.size() > 0) {
//                awards = new ArrayList<>();
//                for (List<Integer> list : sellList) {
//                    int keyId = playerManager.addAward(player, list.get(0), list.get(1), list.get(2), ActivityConst.ACT_DAY_PAY);
//                    if (keyId != 0) {
//                        awards.add(new Award(keyId, list.get(0), list.get(1), list.get(2)));
//                    } else {
//                        awards.add(new Award(list.get(0), list.get(1), list.get(2)));
//                    }
//                }
//            }
//
//        } else if (rechargeType == 5) {//通行证激活
//            StaticPayPassPort staticPayPassPort = staticActivityMgr.getStaticPayPassPort(giftId);
//            if (null == staticPayPassPort) {
//                return false;
//            }
//            vipExp = staticPayPassPort.getVipExp();
//            flag = activityManager.actPayPassPort(player, giftId);
//            List<List<Integer>> sellList = staticPayPassPort.getSellList();
//            if (sellList.size() > 0) {
//                awards = new ArrayList<>();
//                for (List<Integer> list : sellList) {
//                    int keyId = playerManager.addAward(player, list.get(0), list.get(1), list.get(2), ActivityConst.ACT_DAY_PAY);
//                    if (keyId != 0) {
//                        awards.add(new Award(keyId, list.get(0), list.get(1), list.get(2)));
//                    } else {
//                        awards.add(new Award(list.get(0), list.get(1), list.get(2)));
//                    }
//                }
//            }
//
//        } else if (rechargeType == 6) {//s_limit_gift 惊喜好礼
//            StaticLimitGift staticLimitGift = staticActivityMgr.getStaticLimitGiftMap().get(giftId);
//            if (null == staticLimitGift) {
//                return false;
//            }
//            List<List<Integer>> awardList = staticLimitGift.getAwardList();
//            if (awardList.size() > 0) {
//                awards = new ArrayList<>();
//                for (List<Integer> list : awardList) {
//                    int keyId = playerManager.addAward(player, list.get(0), list.get(1), list.get(2), ActivityConst.ACT_SURIPRISE_GIFT);
//                    if (keyId != 0) {
//                        awards.add(new Award(keyId, list.get(0), list.get(1), list.get(2)));
//                    } else {
//                        awards.add(new Award(list.get(0), list.get(1), list.get(2)));
//                    }
//                }
//            }
//            flag = activityManager.updSurpriseGift(player, giftId);
//
//        } else if (rechargeType == 7) {//特价尊享
//            StaticActPayMoney payMoney = staticActivityMgr.getPayMoney(giftId);
//            if (null == payMoney) {
//                return false;
//            }
//            vipExp = payMoney.getVipExp();
//            flag = activityManager.actPaySpecial(player, giftId);
//            List<List<Integer>> sellList = payMoney.getSellList();
//            if (sellList.size() > 0) {
//                awards = new ArrayList<>();
//                for (List<Integer> list : sellList) {
//                    int keyId = playerManager.addAward(player, list.get(0), list.get(1), list.get(2), ActivityConst.ACT_SPECIAL_GIFT);
//                    if (keyId != 0) {
//                        awards.add(new Award(keyId, list.get(0), list.get(1), list.get(2)));
//                    } else {
//                        awards.add(new Award(list.get(0), list.get(1), list.get(2)));
//                    }
//                }
//            }
//        }
//
//        //int firstPay = player.getLord().getFirstPay();
//        if (flag && addPayGift(player, amount, vipExp, req.getCpOrderId(), req.getProductType(), req.getProductId(), awards, Reason.PAY, flagBag)) {
//            List<Integer> activityIds = staticLimitMgr.getAddtion(244) == null ? new ArrayList<>() : staticLimitMgr.getAddtion(244);//充值计费的活动id
//            if (rechargeType != 4) {
//                activityIds = staticLimitMgr.getAddtion(243) == null ? new ArrayList<>() : staticLimitMgr.getAddtion(243);//充值计费的活动id
//            }
//            for (Integer id : activityIds) {
//                checkActivity(player, amount, id);
//            }
//            return true;
//        }
//        return false;
//    }

	/**
	 * 创建订单
	 *
	 * @param req
	 * @param handler
	 */
	public void createOrder(GetOrderNumRq req, ClientHandler handler) {
		// 查找玩家是否存在
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 获取参数
		int platNo = req.getPlatNo();
		int payAmount = 0;
		int productId = req.getProductId();
		int channelId = req.getChannelId();
		int productType = req.getProductType();
		int serverId = req.getServerId();

		Server server = serverManager.getServer();
//        if (serverId != server.getServerId()) {
//            serverId = server.getServerId();
//        }
		String platId = null;
		if (channelId == ChannelConsts.DEFAULT_CHANNEL) {
			platId = String.valueOf(player.account.getAccountKey());
		}
		int accountKey = player.account.getAccountKey();

		boolean flag = true;
		int realProductType = productType;
		if (productType == RECHAR_1) {
			StaticPay pay = staticVipDataMgr.getPayStaticPay(productId);
			if (null == pay) {
				flag = false;
			}
			payAmount = pay.getMoney();
		} else if (productType == RECHAR_2) {
			StaticActPayGift payGift = staticActivityMgr.getPayGift(productId);
			if (null == payGift) {
				flag = false;
			}
			if (flag) {
				int actConst;
				switch (productId) {
					case 8801: // 建造队列
						actConst = ActivityConst.ACT_BUILD_QUE;
						break;
					case 8401: // 双卡礼包
						actConst = ActivityConst.ACT_MONTH_GIFT;
						break;
					case 1281: // 限时礼包
						actConst = ActivityConst.ACT_FLASH_GIFT;
						break;
					default: // 特价礼包
						actConst = ActivityConst.ACT_PAY_GIFT;
						break;
				}
				ActRecord actRecord = activityManager.getActivityInfo(player, actConst);
				if (actRecord == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				long state = actRecord.getStatus(payGift.getPayGiftId());
				if (state > payGift.getCount()) {
					handler.sendErrorMsgToPlayer(GameError.GIFT_BUY_COUNT_ENOUGH);
					return;
				}
				ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
				if (activityData == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				payAmount = payGift.getMoney();
			}
		} else if (productType == RECHAR_3) {
			StaticActPayCard payCard = staticActivityMgr.getPayCard(productId);
			if (null == payCard) {
				flag = false;
			}
			if (flag) {
				ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_MONTH_CARD);
				if (actRecord == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
				if (activityData == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				payAmount = payCard.getMoney();
			}
		} else if (productType == RECHAR_4) {
			StaticActPayMoney payMoney = staticActivityMgr.getPayMoney(productId);
			if (null == payMoney) {
				flag = false;
			}
			if (flag) {
				ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_DAY_PAY);
				if (actRecord == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				if (actRecord.getReceived().containsKey(payMoney.getPayMoneyId())) {
					handler.sendErrorMsgToPlayer(GameError.GIFT_BUY_COUNT_ENOUGH);
					return;
				}
				ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
				if (activityData == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				payAmount = payMoney.getMoney();
			}
		} else if (productType == RECHAR_5) {
			StaticPayPassPort payPassPort = staticActivityMgr.getStaticPayPassPort(productId);
			if (null == payPassPort) {
				flag = false;
			}
			if (flag) {
				ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
				if (actRecord == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
				if (activityData == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				payAmount = payPassPort.getMoney();
			}
		} else if (productType == RECHAR_6) {
			StaticLimitGift limitGift = staticActivityMgr.getLimitGiftByKeyId(productId);
			if (null == limitGift) {
				flag = false;
			}
			if (flag) {
				ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SURIPRISE_GIFT);
				if (actRecord == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				ActivityRecord record = actRecord.getActivityRecord(limitGift.getKeyId());
				if (record.getBuyCount() >= limitGift.getCount()) {
					flag = false;
				} else {
					payAmount = limitGift.getMoney();
				}
			}
		} else if (productType == RECHAR_7) {
			realProductType = 4;
			StaticActPayMoney payMoney = staticActivityMgr.getPayMoney(productId);
			if (null == payMoney) {
				flag = false;
			}
			if (player.getLevel() < payMoney.getLevelDisplay()) {
				handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
				return;
			}
			if (flag) {
				ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SPECIAL_GIFT);
				if (actRecord == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
				if (activityData == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				int limit = actRecord.getReceived(payMoney.getPayMoneyId());
				if (limit >= payMoney.getLimit()) {
					handler.sendErrorMsgToPlayer(GameError.GIFT_BUY_COUNT_ENOUGH);
					return;
				}
				payAmount = payMoney.getMoney();
			}
		} else if (productType == RECHAR_8) {
			realProductType = 2;
			StaticActPayGift payGift = staticActivityMgr.getPayGift(productId);
			if (null == payGift) {
				flag = false;
			}
			payAmount = payGift.getMoney();
		} else if (productType == RECHAR_10) {
			realProductType = 4;
			StaticResourceGift staticResourceGift = staticActivityMgr.getStaticResourceGift(productId);
			if (null == staticResourceGift) {
				flag = false;
			}
			payAmount = staticResourceGift.getMoney();
		} else if (productType == RECHAR_11) {
			// 活动结束不让买
			ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_SPRING_FESTIVAL_GIFT);
			if (activityBase == null || activityBase.getStep() == ActivityConst.ACTIVITY_CLOSE) {
				flag = false;
			}
			realProductType = 4;
			StaticLimitGift springGift = staticActivityMgr.getSpringGift(productId);
			if (null == springGift) {
				flag = false;
			}
			payAmount = springGift.getMoney();
		}

		if (!flag) {
			// 活动结束
			// 订单创建失败
			handler.sendErrorMsgToPlayer(GameError.PAY_CREATE_ORDERNUM_ERROR);
			return;
		}
		// 创建订单 prc调用支付接口
		// 创建订单
		int PAY_AMOUNT = payAmount;
		String PLAT_ID = platId;
		int REAL_PRODUCT_TYPE = realProductType;
		SpringUtil.getBean(HttpExecutor.class).add(() -> {
			PayOrder payOrder = payManager.createOrderNum(new PayOrder(channelId, platNo, player.roleId, serverId, productType, productId, PAY_AMOUNT * 100, PLAT_ID, accountKey, player.getLevel()));
			if (null == payOrder.getCpOrderId()) {
				// 创建失败
				handler.sendErrorMsgToPlayer(GameError.PAY_CREATE_ORDERNUM_ERROR);
				return;
			}

			payOrder.setRealServer(server.getServerId());
			payOrder.setNick(player.getNick());
			Message msg = httpService.sendOrderNum(payOrder);
			if (msg == null || msg.getCode() != UcCodeEnum.SUCCESS.getCode()) {
				// 创建失败
				handler.sendErrorMsgToPlayer(GameError.PAY_CREATE_ORDERNUM_ERROR);
				return;
			}

			PayPb.GetOrderNumRs.Builder builder = GetOrderNumRs.newBuilder();
			builder.setOrderNum(payOrder.getCpOrderId());

			StaticPayPoint staticPayPoint = staticVipDataMgr.getStaticPayPoint(REAL_PRODUCT_TYPE, PAY_AMOUNT);// 设置计费点
			if (staticPayPoint == null) {
				// 创建失败
				handler.sendErrorMsgToPlayer(GameError.PAY_CREATE_ORDERNUM_ERROR);
				return;
			}

			builder.setPayPoint(staticPayPoint.getSdk_point());
			handler.sendMsgToPlayer(GetOrderNumRs.ext, builder.build());

			// 充值回调
			if (channelId == ChannelConsts.DEFAULT_CHANNEL) {
				Message defaultPayBack = httpService.defaultPayBack(payOrder);
			}

			List<Object> param = Lists.newArrayList(payOrder.getCpOrderId(), PAY_AMOUNT, "test", "test", productId, "test", payOrder.getCpOrderId(), "", false, player.getLord().getTopup() == 0, getPayGiftName(payOrder),payOrder.getProductType(),payOrder.getProductId());
			SpringUtil.getBean(EventManager.class).order_event(player, param);

		});
	}

	/**
	 * 通知可以推送充值信息了
	 *
	 * @param req
	 * @param handler
	 */
	public void getRechargeRq(PayPb.GetRechargeRq req, ClientHandler handler) {
		// 查找玩家是否存在
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		handler.sendMsgToPlayer(PayPb.GetRechargeRs.ext, PayPb.GetRechargeRs.newBuilder().build());
		if (player.getPayMsg().size() > 0) {
			player.getPayMsg().forEach(e -> {
				SynHelper.synMsgToPlayer(player, RolePb.SynGoldRq.EXT_FIELD_NUMBER, RolePb.SynGoldRq.ext, e);
			});
			player.getPayMsg().clear();
		}
	}

	public String getPayGiftName(PayOrder payOrder) {
		int productId = payOrder.getProductId();
		switch (payOrder.getProductType()) {
			default:
				return "未知_" + payOrder.getProductType() + "_" + payOrder.getProductId();
			// 直冲
			case 1:
				StaticPay pay = staticVipDataMgr.getPayStaticPay(payOrder.getProductId());
				if (pay == null) {
					return "未知_" + payOrder.getProductType() + "_" + payOrder.getProductId();
				}
				return "直冲" + pay.getMoney() + "元";
			case 2:
			case 8:
				StaticActPayGift payGift = staticActivityMgr.getPayGift(productId);
				if (payGift == null) {
					return "未知_" + payOrder.getProductType() + "_" + payOrder.getProductId();
				}
				return payGift.getName();
			case 3:
				StaticActPayCard payCard = staticActivityMgr.getPayCard(productId);
				if (payCard == null) {
					return "未知_" + payOrder.getProductType() + "_" + payOrder.getProductId();
				}
				return payCard.getName();
			case 4:
			case 7:
				StaticActPayMoney payMoney = staticActivityMgr.getPayMoney(productId);
				if (payMoney == null) {
					return "未知_" + payOrder.getProductType() + "_" + payOrder.getProductId();
				}
				return payMoney.getName();
			case 5:
				StaticPayPassPort payPassPort = staticActivityMgr.getStaticPayPassPort(productId);
				if (payPassPort == null) {
					return "未知_" + payOrder.getProductType() + "_" + payOrder.getProductId();
				}
				return "通行证_" + payPassPort.getMoney();
			case 6:
				StaticLimitGift limitGift = staticActivityMgr.getLimitGiftByKeyId(productId);
				if (null == limitGift) {
					return "未知_" + payOrder.getProductType() + "_" + payOrder.getProductId();
				}
				return limitGift.getName();
		}
	}
}
