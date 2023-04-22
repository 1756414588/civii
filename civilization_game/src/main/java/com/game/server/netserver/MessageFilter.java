package com.game.server.netserver;

import com.game.pb.InnerPb.HeartBeatRq;
import com.game.pb.RolePb.GetTimeRq;
import com.game.pb.RolePb.GetTimeRs;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @Description 消息过滤
 * @Date 2023/3/29 16:58
 **/

public class MessageFilter {

	public static Set<Integer> printFilterMap = new HashSet<>();

	public static Set<Integer> printExecMap = new HashSet<>();

	static {
		registerPrintFilter();
		registerExecFilter();
	}


	private static void registerPrintFilter() {
		filterPrintCmd(HeartBeatRq.EXT_FIELD_NUMBER);
		filterPrintCmd(GetTimeRq.EXT_FIELD_NUMBER);
		filterPrintCmd(GetTimeRs.EXT_FIELD_NUMBER);
	}

	private static void registerExecFilter() {
		filterExecCmd(HeartBeatRq.EXT_FIELD_NUMBER);
	}


	/**
	 * 是否过滤掉打印
	 *
	 * @param cmd
	 * @return
	 */
	public static boolean isFilterPrint(int cmd) {
		return printFilterMap.contains(cmd);
	}

	/**
	 * 是否过滤掉执行(只需要客户端有发送即可)
	 *
	 * @param cmd
	 */
	public static boolean isFilterExec(int cmd) {
		return printExecMap.contains(cmd);
	}


	public static void filterPrintCmd(int cmd) {
		if (!printFilterMap.contains(cmd)) {
			printFilterMap.add(cmd);
		}
	}

	public static void filterExecCmd(int cmd) {
		if (!printExecMap.contains(cmd)) {
			printExecMap.add(cmd);
		}
	}

}
