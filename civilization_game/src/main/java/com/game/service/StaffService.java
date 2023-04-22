package com.game.service;

import com.game.constant.GameError;
import com.game.constant.StaffTaskState;
import com.game.domain.Player;
import com.game.domain.p.SimpleData;
import com.game.domain.p.StaffTask;
import com.game.manager.CountryManager;
import com.game.manager.PlayerManager;
import com.game.manager.StaffManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.StaffPb.OpenStaffTaskRq;
import com.game.pb.StaffPb.OpenStaffTaskRs;
import com.game.pb.StaffPb.GetStaffTaskRs;
import com.game.pb.StaffPb.ActivateTaskRq;
import com.game.pb.StaffPb.ActivateTaskRs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class StaffService {
    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private StaffManager staffManager;

    @Autowired
    private CountryManager countryManager;

    // 开启任务
    public void openStaffTask(OpenStaffTaskRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int taskId = req.getTaskId();

        // 检查任务Id是否合法
        if (!staffManager.isTaskIdOk(taskId)) {
            handler.sendErrorMsgToPlayer(GameError.STAFF_TASK_ID_IS_NOT_INVALID);
            return;
        }

        SimpleData simpleData = player.getSimpleData();
        HashMap<Integer, StaffTask> staffTaskMap = simpleData.getStaffTaskMap();
        staffManager.checkTaskMap(staffTaskMap);
        // 检查任务是否开启
        StaffTask staffTask = staffTaskMap.get(taskId);
        if (staffTask == null) {
            handler.sendErrorMsgToPlayer(GameError.STAFF_TASK_NOT_EXISTS);
            return;
        }

        if (staffTask.getStatus() != StaffTaskState.NOT_OPEN) {
            handler.sendErrorMsgToPlayer(GameError.STAFF_TASK_STATE_ERROR);
            return;
        }

        // 检查前置任务是否合法
        if (!staffManager.isPreTaskOk(taskId, staffTaskMap)) {
            handler.sendErrorMsgToPlayer(GameError.PRE_STAFF_TASK_NOT_ACTIVATED);
            return;
        }

        staffManager.flushTaskMonster(player, taskId);

        // 开启任务
        staffTask.setStatus(StaffTaskState.OPEN);
        OpenStaffTaskRs.Builder builder = OpenStaffTaskRs.newBuilder();
        builder.setTask(staffTask.wrap());
        handler.sendMsgToPlayer(OpenStaffTaskRs.ext, builder.build());

    }

    public void getStaffTask(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SimpleData simpleData = player.getSimpleData();
        HashMap<Integer, StaffTask> staffTaskMap = simpleData.getStaffTaskMap();
        staffManager.checkTaskMap(staffTaskMap);
        GetStaffTaskRs.Builder builder = GetStaffTaskRs.newBuilder();
        for (StaffTask staffTask : staffTaskMap.values()) {
            if (staffTask != null) {
                builder.addTask(staffTask.wrap());
            }
        }
        builder.setSoldierNum(countryManager.getSoldierNum(player));
        handler.sendMsgToPlayer(GetStaffTaskRs.ext, builder.build());
    }

    public void activateStaffTask(ActivateTaskRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int taskId = req.getTaskId();
        // 检查任务Id是否合法
        if (!staffManager.isTaskIdOk(taskId)) {
            handler.sendErrorMsgToPlayer(GameError.STAFF_TASK_ID_IS_NOT_INVALID);
            return;
        }

        SimpleData simpleData = player.getSimpleData();
        HashMap<Integer, StaffTask> staffTaskMap = simpleData.getStaffTaskMap();
        // 检查任务是否开启
        StaffTask staffTask = staffTaskMap.get(taskId);
        if (staffTask == null) {
            handler.sendErrorMsgToPlayer(GameError.STAFF_TASK_NOT_EXISTS);
            return;
        }

        // 检查前置任务有没有激活
        if (!staffManager.isPreTaskOk(taskId, staffTaskMap)) {
            handler.sendErrorMsgToPlayer(GameError.PRE_STAFF_TASK_NOT_ACTIVATED);
            return;
        }

        // 检查当前状态是否满足条件
        if (!staffManager.checkCond(player, taskId)) {
            handler.sendErrorMsgToPlayer(GameError.STAFF_ACTIVATE_COND_ERROR);
            return;
        }

        // 激活当前任务
        staffTask.activated();
        ActivateTaskRs.Builder builder = ActivateTaskRs.newBuilder();
        builder.setTask(staffTask.wrap());
        handler.sendMsgToPlayer(ActivateTaskRs.ext, builder.build());
    }
}
