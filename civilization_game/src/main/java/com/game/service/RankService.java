package com.game.service;

import com.game.domain.p.AchievementInfo;
import com.game.flame.FlamePlayer;
import com.game.pb.CommonPb;
import com.game.util.PbHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.constant.GameError;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.p.Lord;
import com.game.domain.s.StaticWorldMap;
import com.game.manager.PlayerManager;
import com.game.manager.RankManager;
import com.game.manager.WorldManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.RankPb;
import com.game.pb.RankPb.GetAreaRankRq;
import com.game.pb.RankPb.GetAreaRankRs;
import com.game.pb.RankPb.GetRankRq;
import com.game.pb.RankPb.GetRankRs;
import com.game.util.LogHelper;
import com.game.util.MathHelper;
import com.game.util.BasePbHelper;

@Service
public class RankService {

    @Autowired
    private RankManager rankManager;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private StaticWorldMgr staticWorldMgr;

    /**
     * @param req
     * @param handler
     */
    public void getRankRq(GetRankRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        GetRankRs.Builder builder = GetRankRs.newBuilder();
        builder.setMyRank(rankManager.getMyRank(player.getLord().getLordId()));
        rankManager.readLock().lock();
        try {
            List<Lord> rankList = rankManager.getRankList();
            int page = req.getPage();
            if (page <= 0) {
                handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                return;
            }
            int maxPage = 0;
            if (!rankList.isEmpty()) {
                maxPage = MathHelper.devide(rankList.size(), 10);
                page = Math.max(1, page);
                page = Math.min(page, maxPage);
                for (int rank = (page - 1) * 10 + 1; rank <= page * 10 && rank <= rankList.size(); rank++) {
                    Lord next = rankList.get(rank - 1);
                    if (next == null) {
                        continue;
                    }
                    builder.addRankInfo(PbHelper.createRank(next, rank));
                }
            }
            builder.setPage(page);
            builder.setTotal(maxPage);
            handler.sendMsgToPlayer(GetRankRs.ext, builder.build());
        } catch (Exception e) {

        } finally {
            rankManager.readLock().unlock();
        }
    }


    /**
     * @param req
     * @param handler
     */
    public void getCountryRankRq(RankPb.GetCountryRankRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        RankPb.GetCountryRankRs.Builder builder = RankPb.GetCountryRankRs.newBuilder();
        List<Lord> rankList = rankManager.getCountryRankList(player.getCountry());
        builder.setMyRank(rankManager.getMyRank(rankList, player));
        int page = req.getPage();
        if (page <= 0) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        int maxPage = 0;
        if (!rankList.isEmpty()) {
            maxPage = MathHelper.devide(rankList.size(), 10);
            page = Math.max(1, page);
            page = Math.min(page, maxPage);
            for (int rank = (page - 1) * 10 + 1; rank <= page * 10 && rank <= rankList.size(); rank++) {
                Lord next = rankList.get(rank - 1);
                if (next == null) {
                    continue;
                }

                builder.addRankInfo(PbHelper.createRank(next, rank));

            }
        }

        builder.setPage(page);
        builder.setTotal(maxPage);
        handler.sendMsgToPlayer(RankPb.GetCountryRankRs.ext, builder.build());
    }

    /**
     * @param req
     * @param handler
     */
    public void getAreaRankRq(GetAreaRankRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int mapId = worldManager.getMapId(player);
        StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(mapId);
        if (staticWorldMap == null) {
            LogHelper.CONFIG_LOGGER.info("map is null.");
            handler.sendErrorMsgToPlayer(GameError.MAP_ID_ERROR);
            return;
        }

        int page = req.getPage();
        if (page <= 0) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        GetAreaRankRs.Builder builder = GetAreaRankRs.newBuilder();

        // 区域排行
        List<Lord> rankList = rankManager.getAreaRankList(staticWorldMap);

        int index = rankList.indexOf(player.getLord());
        builder.setMyRank(index + 1);


        int maxPage = 0;
        if (!rankList.isEmpty()) {
            maxPage = MathHelper.devide(rankList.size(), 10);
            page = Math.max(1, page);
            page = Math.min(page, maxPage);
            for (int rank = (page - 1) * 10 + 1; rank <= page * 10 && rank <= rankList.size(); rank++) {
                Lord next = rankList.get(rank - 1);
                if (next == null) {
                    continue;
                }

                builder.addRankInfo(PbHelper.createRank(next, rank));
            }
        }

        builder.setPage(page);
        builder.setTotal(maxPage);

        handler.sendMsgToPlayer(GetAreaRankRs.ext, builder.build());
    }

    public void getAchievementRank(RankPb.GetAchiRankRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int page = req.getPage();
        LinkedList<AchievementInfo> achievementInfos = rankManager.getAchievementInfos();
        int maxPage = MathHelper.devide(achievementInfos.size(), 10);
        page = Math.max(1, page);
        page = Math.min(page, maxPage);
        int index = achievementInfos.indexOf(player.getLord());
        RankPb.GetAchiRankRs.Builder builder = RankPb.GetAchiRankRs.newBuilder();
        builder.setPage(page);
        builder.setTotal(maxPage);
        builder.setMyRank(index + 1);
        if (page > 0) {
            for (int rank = (page - 1) * 10; rank <= page * 10 && rank < achievementInfos.size(); rank++) {
                AchievementInfo next = achievementInfos.get(rank);
                if (next == null) {
                    continue;
                }
                Player player1 = next.getPlayer();
                CommonPb.RankInfo.Builder rankInfo = CommonPb.RankInfo.newBuilder();
                rankInfo.setRank(rank + 1);
                rankInfo.setName(player1.getNick());
                rankInfo.setLevel(player1.getLevel());
                rankInfo.setCountry(player1.getCountry());
                rankInfo.setTitle(player1.getTitle());
                rankInfo.setBattleSocre(next.getScore());
                rankInfo.setLordId(player1.roleId);
                rankInfo.setPortrait(player1.getPortrait());
                builder.addRankInfo(rankInfo);
            }
        }

        handler.sendMsgToPlayer(RankPb.GetAchiRankRs.ext, builder.build());
    }
}
