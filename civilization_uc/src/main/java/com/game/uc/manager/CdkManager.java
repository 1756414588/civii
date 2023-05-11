package com.game.uc.manager;

import com.game.uc.Cdkey;
import com.game.uc.CdkeyItem;
import com.game.uc.CdkeyType;
import com.game.uc.dao.ifs.p.CdkeyDao;
import com.game.uc.dao.ifs.p.CdkeyItemDao;
import com.game.uc.dao.ifs.p.CdkeyTypeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @date 2020/6/1 15:14
 * @description
 */
@Service
public class CdkManager {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private Lock lock = new ReentrantLock();

  @Autowired private CdkeyTypeDao cdKeyTypeDao;

  @Autowired private CdkeyDao cdKeyDao;

  @Autowired private CdkeyItemDao cdKeyItemDao;
  /** 已经激活的cdk的信息 */
  private Map<String, Cdkey> cdks = new ConcurrentHashMap<>();

  /** cdk的物品集合 */
  private Map<Integer, List<CdkeyItem>> cdKeyItems = new ConcurrentHashMap<>();

  /** 当前cd的礼包 */
  private Map<Integer, CdkeyType> cdKeyTypeMap = new ConcurrentHashMap<>();

//  @PostConstruct
  public void init() {
    lock.lock();
    // 避免多次发放造成多次奖励
    this.cdKeyItems.clear();
    // 查询所有的cdk奖励
    List<CdkeyItem> cdKeyItems = cdKeyItemDao.selectAll();
    cdKeyItems.forEach(
        cdKeyItem -> {
          List<CdkeyItem> result = this.cdKeyItems.get(cdKeyItem.getGiftbagid());
          if (result == null) {
            result = new ArrayList<>();
            this.cdKeyItems.put(cdKeyItem.getGiftbagid(), result);
          }
          result.add(cdKeyItem);
        });
    try {
      List<CdkeyType> cdKeyTypes = cdKeyTypeDao.selectAll();
      for (CdkeyType cdKeyType : cdKeyTypes) {
        List<Cdkey> cdKeys = cdKeyDao.selectCdkActivity(cdKeyType.getGiftbagid());
        cdKeyTypeMap.put(cdKeyType.getGiftbagid(), cdKeyType);
        cdKeys.forEach(cdKey -> this.cdks.put(cdKey.getKeychar(), cdKey));
      }

    } finally {
      lock.unlock();
    }
  }

  public List<CdkeyItem> getCdKeyItems(int giftbagid) {
    return cdKeyItems.get(giftbagid);
  }

  public Cdkey getCdk(String cdk) {
    return cdks.get(cdk);
  }

  /**
   * 拿到通一个类型的cd，奖励id 为同一个类型的
   *
   * @param keyType
   * @return
   */
  public List<Cdkey> getCdKeys(int keyType) {
    List<Cdkey> cdKeys = new ArrayList<>();
    for (Map.Entry<String, Cdkey> entry : this.cdks.entrySet()) {
      Cdkey cdKey = entry.getValue();
      if (cdKey.getKeytype() == keyType) {
        cdKeys.add(cdKey);
      }
    }
    return cdKeys;
  }

  public Lock getLock() {
    return lock;
  }

  public boolean updateCdk(Integer autoid, String oldKeyChar) {
    Cdkey cdkey = cdKeyDao.selectByPrimaryKey(autoid);
    if (cdkey == null) {
      return false;
    }
    if (!this.cdks.containsKey(oldKeyChar)) {
      return false;
    }
    this.cdks.remove(oldKeyChar);
    this.cdks.put(cdkey.getKeychar(), cdkey);
    return true;
  }
}
