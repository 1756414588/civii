package com.game.uc.manager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.game.uc.*;
import com.game.uc.dao.ifs.p.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 *
 * @date 2020/4/9 15:00
 * @description
 */
@Service
public class AccountManager {

    private Cache<Integer, Account> cache = CacheBuilder.newBuilder().expireAfterWrite(7, TimeUnit.DAYS).build();

    /**
     * 设置token过期时间为7天
     *
     * @param account
     */
    public void add(Account account) {
//        redisService.setAccount(account);
        cache.put(account.getKeyId(),account);
    }


    /**
     * 封停的角色
     */
    private Map<Long, CloseRole> closeRoleMap = new ConcurrentHashMap<>();

    /**
     * 封停的角色
     */
    private Map<Integer, CloseAccount> closeAccountMap = new ConcurrentHashMap<>();


    /**
     * 封停IP
     */
    private Map<String, Boolean> closeIpMap = new ConcurrentHashMap<>();
    /**
     * 封停设备
     */
    private Map<String, Boolean> closeUuidMap = new ConcurrentHashMap<>();


    private Map<Long, CloseSpeak> closeSpeakMap = new ConcurrentHashMap<>();

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private CloseRoleDao closeRoleDao;

    @Autowired
    private CloseAccountDao closeAccountDao;

    @Autowired
    private CloseSpeakDao closeSpeakDao;
    @Autowired
    private CloseIpDao closeIpDao;
    @Autowired
    private CloseUuidDao closeUuidDao;


    @PostConstruct
    public void init() {
        initCloseRole();
        initCloseAccount();
        initCloseSpeak();
    }

    public void init(int type) {
        if (type == 1) {
            initCloseRole();
        } else if (type == 2) {

            initCloseAccount();
        } else if (type == 3) {
            initCloseSpeak();
        }
    }

    private void initCloseRole() {
        closeRoleMap.clear();
        List<CloseRole> closeRoles = closeRoleDao.selectAll();
        closeRoles.forEach(closeRole -> closeRoleMap.put(closeRole.getRoleid(), closeRole));
    }


    private void initCloseAccount() {
        closeAccountMap.clear();
        List<CloseAccount> closeAccounts = closeAccountDao.selectAll();
        closeAccounts.forEach(closeAccount -> closeAccountMap.put(closeAccount.getAccountKey(), closeAccount));
    }

    private void initCloseSpeak() {
        closeSpeakMap.clear();
        List<CloseSpeak> closeSpeaks = closeSpeakDao.selectAll();
        closeSpeaks.forEach(closeSpeak -> closeSpeakMap.put(closeSpeak.getRoleid(), closeSpeak));
    }

    public void initCloseIp() {
        List<CloseIp> closes = closeIpDao.selectAll();
        Map<String, Boolean> tmpMap = new ConcurrentHashMap<>();
        closes.forEach(closeSpeak -> tmpMap.put(closeSpeak.getIp(), true));
        closeIpMap = tmpMap;
    }

    public void initCloseUuid() {
        List<CloseUuid> closes = closeUuidDao.selectAll();
        Map<String, Boolean> tmpMap = new ConcurrentHashMap<>();
        closes.forEach(closeSpeak -> tmpMap.put(closeSpeak.getUuid(), true));
        closeUuidMap = tmpMap;
    }

    public void recordRecentServer(Account account, int serverId) {
        int[] record = {account.getFirstSvr(), account.getSecondSvr(), account.getThirdSvr()};
        int temp = 0;
        if (record[0] != 0) {
            if (record[0] != serverId) {
                temp = record[2];
                record[2] = record[1];
                record[1] = record[0];
            }
        }

        record[0] = serverId;
        if (record[2] == serverId) {
            record[2] = temp;
        }
        account.setFirstSvr(record[0]);
        account.setSecondSvr(record[1]);
        account.setThirdSvr(record[2]);
        account.setGameDate(new Date());
        try {
            ArrayList<Integer> parse = new ArrayList<Integer>();
            JSONArray jsonArray = JSONObject.parseArray(account.getLoggedServer());
            if (jsonArray != null) {
                for (Object value : jsonArray) {
                    int object1 = (int) value;
                    parse.add(object1);
                }
            }
            Iterator<Integer> iterator = parse.iterator();
            while (iterator.hasNext()) {
                Integer next = iterator.next();
                if (next.intValue() == serverId) {
                    iterator.remove();
                }
            }
            parse.add(0, serverId);
            if (parse.size() < 3) {
                for (int i : record) {
                    if (!parse.contains(i) && i != 0) {
                        parse.add(i);
                    }
                }
            }
            account.setLoggedServer(JSONObject.toJSONString(parse));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 先直接去数据库拿
     *
     * @param accountId
     * @return
     */
    public Account get(String accountId) {
        return accountDao.selectByAccount(accountId);
    }

    public Account getByKey(int keyId) {
        Account account =cache.getIfPresent(keyId);
        if (account != null) {
            return account;
        }
        return accountDao.selectByKey(keyId);
    }


    public List<Integer> getRecentServers(Account account) {
        List<Integer> list = new ArrayList<Integer>();
        list.add(account.getFirstSvr());
        list.add(account.getSecondSvr());
        list.add(account.getThirdSvr());
        ArrayList<Integer> parse = new ArrayList<Integer>();
        try {
            JSONArray jsonArray = JSONObject.parseArray(account.getLoggedServer());
            if (jsonArray != null && jsonArray.size() >= 3) {
                for (Object value : jsonArray) {
                    int object1 = (int) value;
                    parse.add(object1);
                }
            } else {
                parse.addAll(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parse;
    }

    public boolean isAccountCanLogin(int accountKey, int serverId) {
        Date date = new Date();
        CloseAccount closeAccount = closeAccountMap.get(accountKey);
        if (closeAccountMap.get(accountKey) != null && date.before(closeAccount.getEndtime())) {
            return false;
        }
        return true;
    }

    public boolean isCloseRole(int accountKey, int serverId) {
        for (Map.Entry<Long, CloseRole> entry : closeRoleMap.entrySet()) {
            CloseRole closeRole = entry.getValue();
            if (closeRole.getAccountKey().intValue() == accountKey && closeRole.getServerId().intValue() == serverId) {
                if (new Date().before(closeRole.getEndtime())) {
                    return false;
                }

            }
        }
        return true;
    }

    public long getCloseSpeak(int accountKey, int serverId) {
        Date now = new Date();
        for (Map.Entry<Long, CloseSpeak> entry : closeSpeakMap.entrySet()) {
            CloseSpeak closeSpeak = entry.getValue();
            if (closeSpeak.getAccountKey().intValue() == accountKey && closeSpeak.getServerId().intValue() == serverId) {
                if (closeSpeak.getEndTime() != null && now.before(closeSpeak.getEndTime())) {
                    return closeSpeak.getEndTime().getTime();
                }
            }
        }
        return 0;
    }


	public boolean isCloseIp(String ip) {
		if (ip == null) {
			return false;
		}
		return closeIpMap.containsKey(ip);
	}

	public boolean isCloseUuid(String ip) {
		if (ip == null) {
			return false;
		}
		return closeUuidMap.containsKey(ip);
	}
}
