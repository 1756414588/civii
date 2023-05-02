package com.game.uc.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @date 2021/3/22 20:44
 *
 */
@Service
@Slf4j
public class RedisService {
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    private String get(String key) {
//        try {
//            return (String) redisTemplate.opsForValue().get(key);
//        } catch (Exception e) {
//            log.error("获取key error->[{}]", e.getMessage());
//        }
//        return null;
//    }
//
//    /**
//     * 获取account
//     *
//     * @param keyId
//     * @return
//     */
//    public Account getAccount(int keyId) {
//        try {
//            String result = (String) redisTemplate.opsForValue().get(RedisKey.ACCOUNT + keyId);
//            if (result != null) {
//                return JSONObject.parseObject(result, Account.class);
//            }
//        } catch (Exception e) {
//            log.error("获取key error->[{}]", e.getMessage());
//        }
//        return null;
//    }
//
//    /**
//     * 设置7天后过期
//     *
//     * @param account
//     */
//    public void setAccount(Account account) {
//        try {
//            redisTemplate.opsForValue().set(RedisKey.ACCOUNT + account.getKeyId(), JSONObject.toJSONString(account), 7, TimeUnit.DAYS);
//        } catch (Exception e) {
//            log.error("获取key error->[{}]", e.getMessage());
//        }
//    }
//
//    /**
//     * @Description 获取当前server人数最少的阵营
//     * @Date 2021/8/31 10:19
//     **/
//    public int getMinCountryId(int serverId) {
//        HashMap<Integer, Integer> map = new HashMap<>();
//        List<Integer> countryList = CountryId.getCountryList();
//        countryList.forEach(e -> {
//            String append = new StringBuffer().append(serverId).append(RedisKey.COUNTRY_KEY).append(e).toString();
//            int countryNum = 0;
//            try {
//                countryNum = (Integer) redisTemplate.opsForValue().get(append);
//            } catch (Exception exception) {
//                log.debug("获取最少国家人数 error  serverId->[{}]  country->[{}]", serverId, e);
//            }
//            map.put(e, countryNum);
//        });
//        int min = map.values().stream().sorted(Comparator.comparing(Integer::intValue)).collect(Collectors.toList()).get(0);
//        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
//            if (entry.getValue().intValue() == min) {
//                return entry.getKey();
//            }
//        }
//        Collections.shuffle(countryList);
//        return countryList.get(0);
//    }
}
