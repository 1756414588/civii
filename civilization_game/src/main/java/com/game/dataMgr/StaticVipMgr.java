package com.game.dataMgr;

import com.game.define.LoadData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.domain.s.StaticPayPoint;
import com.google.common.collect.HashBasedTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticPay;
import com.game.domain.s.StaticVip;
import com.game.domain.s.StaticVipBuy;

@Component
@LoadData(name = "VIP模块")
public class StaticVipMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    // vip
    private Map<Integer, StaticVip> vipMap;

    private List<StaticVip> vipList;

    //购买钻石计费点
    private List<StaticPay> payList;
    private Map<Integer, StaticPay> PayMap = new HashMap<>();

    private Map<Integer, List<StaticVipBuy>> vipBuyMaps = new HashMap<Integer, List<StaticVipBuy>>();

    private HashBasedTable<Integer,Integer,StaticPayPoint> payPointTable = HashBasedTable.create();

    @Override
    public void load() throws Exception {
        // TODO Auto-generated method stub
        vipMap = new HashMap<>();
        vipBuyMaps = new HashMap<>();
        PayMap.clear();
        payPointTable.clear();
        initVip();
        initVipBuy();
        initPayPoint();
        payList = staticDataDao.selectPay();
        if (payList.size() > 0) {
            for (StaticPay staticPay : payList) {
                PayMap.put(staticPay.getPayId(), staticPay);
            }
        }
    }

    /**
     * Overriding: init
     *
     * @see com.game.dataMgr.BaseDataMgr#init()
     */
    @Override
    public void init() throws Exception{

    }

    private void initVip() {
        vipList = staticDataDao.selectVip();
        setVipMap(new HashMap<Integer, StaticVip>());

        for (StaticVip staticVip : vipList) {
            getVipMap().put(staticVip.getVip(), staticVip);
        }
    }

    private void initVipBuy() {
        List<StaticVipBuy> buys = staticDataDao.selectStaticVipBuy();
        for (StaticVipBuy buy : buys) {
            int vip = buy.getVip();
            if (buy.getType() != 1) {
                continue;
            }
            List<StaticVipBuy> vlist = vipBuyMaps.get(vip);
            if (vlist == null) {
                vlist = new ArrayList<StaticVipBuy>();
                vipBuyMaps.put(vip, vlist);
            }
            vlist.add(buy);
        }
    }

    public StaticVip getStaticVip(int vip) {
        return getVipMap().get(vip);
    }

    public int calcVip(int topup) {
        StaticVip vip = null;
        for (StaticVip staticVip : vipList) {
            if (topup >= staticVip.getTopup()) {
                vip = staticVip;
            } else
                break;
        }
        if (vip != null) {
            return vip.getVip();
        }

        return 0;
    }

    public List<StaticPay> getPayList() {
        return payList;
    }

    public StaticPay getPayStaticPay(int payId) {
        return PayMap.get(payId);
    }

    public List<StaticVip> getVipList() {
        return vipList;
    }

    public List<StaticVipBuy> getVipBuy(int vip) {
        return vipBuyMaps.get(vip);
    }

    public StaticVipBuy getVipBuy(int vip, int propId) {
        if (!vipBuyMaps.containsKey(vip)) {
            return null;
        }
        List<StaticVipBuy> list = vipBuyMaps.get(vip);
        for (StaticVipBuy e : list) {
            if (e.getPropId() == propId) {
                return e;
            }
        }
        return null;
    }

    public Map<Integer, StaticVip> getVipMap() {
        return vipMap;
    }

    public void setVipMap(Map<Integer, StaticVip> vipMap) {
        this.vipMap = vipMap;
    }

    public void initPayPoint() {
        List<StaticPayPoint> staticPayPoints = staticDataDao.selectPayPoint();
        staticPayPoints.forEach(staticPayPoint -> {
            payPointTable.put(staticPayPoint.getProductType(), staticPayPoint.getMoney(), staticPayPoint);
        });
    }

    public StaticPayPoint getStaticPayPoint(int productId, int money) {
        return payPointTable.get(productId, money);
    }
}
