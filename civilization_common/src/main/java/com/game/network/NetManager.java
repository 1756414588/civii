package com.game.network;

import com.game.define.App;
import com.game.packet.Packet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author 陈奎
 * @Description 服务器内部连接管理器
 * @Date 2022/9/9 11:30
 **/

public class NetManager {

	private static NetManager inst = new NetManager();

	public static NetManager getInst() {
		return inst;
	}

	private Map<String, INet> netMap = new ConcurrentHashMap<>();
	private Map<App, List<INet>> appNetMap = new ConcurrentHashMap<>();

	// TODO 后续做分组
	private AtomicInteger seq = new AtomicInteger(0);

	private Map<Integer, ICallback> callbackMap = new ConcurrentHashMap<>();

	public INet get(String id) {
		return this.netMap.get(id);
	}

	public void put(String id, INet net) {
		this.netMap.put(id, net);
	}

	public void remove(INet net) {
		INet remove = this.netMap.remove(net);
		if (remove != null) {
			List<INet> list = appNetMap.get(remove.getApp());
			if (list != null) {
				list.remove(remove);
			}
		}
	}

	public List<INet> getByApp(App app) {
		return appNetMap.get(app);
	}

	public void putAppNet(App app, INet net) {
		List<INet> list = appNetMap.get(app);
		if (list == null) {
			list = new ArrayList<>();
			appNetMap.put(app, list);
		}
		list.add(net);
	}

	public void putCallback(Packet packet, ICallback callback) {
		packet.setSeq(seq.incrementAndGet());
		packet.setCallBack((byte) 1);
		callbackMap.put(packet.getSeq(), callback);
	}

	public void doCallback(Packet packet) {
		if (!callbackMap.containsKey(packet.getSeq())) {
			return;
		}
		ICallback callback = callbackMap.get(packet.getSeq());
		callback.onResult(packet);
		callbackMap.remove(callback);
	}

}
