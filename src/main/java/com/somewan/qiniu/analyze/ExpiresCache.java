package com.somewan.qiniu.analyze;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * 模拟一个具有过期时间的缓存
 * Created by wan on 2017/3/1.
 */
public class ExpiresCache {
    private static final Logger logger = LogManager.getLogger();
    private static final int LOG_DEBUG_SIZE = 50000;
    private static final int CONSLE_DEBUG_SIZE = 500000;

    private static final int INTERVAL = 5;// 5ms
    private static final int FIX_TIME = 1;
    private Deque<String> ruList;// 访问顺序链表：按使用顺序保存map中key，维护淘汰顺序。类似于先进先出。
    private Map<String, Long> keyExpireMap;// 维护近期请求的 key -> Expire 过期时间，用于搜索。

    // 当前的状态
    private int get;// get 的次数。向 BD 请求的次数。
    private int hit;// hit 的次数。请求归并的次数。
    private int rget;// real get,实际向 BD 请求的次数
    private int invalid;// 无效的请求。
    private Map<Long, Integer> passTimeMap;// 统计 hit 的 key 时间间隔分布情况。


    public ExpiresCache() {
        this.get = 0;
        this.hit = 0;
        ruList = new LinkedList<>();
        keyExpireMap = new HashMap<>();
        passTimeMap = new HashMap<>(8);
    }


    /**
     *
     * @param key
     * @param time 时间戳,ms 为单位
     */
    public void get(String key, long time) {
        get++;
        logMilestone();

        removeExpiredKey(time);
        // 命中缓存
        if (keyExpireMap.containsKey(key)) {
            long passTime = time - (keyExpireMap.get(key) - INTERVAL);
            // 如果 passTime < 0 那么说明先请求的数据被后写入数据库。
            // 满足 PassTime <=5;
            if(passTimeMap.containsKey(passTime)) {
                int count = passTimeMap.get(passTime);
                passTimeMap.put(passTime, count + 1);
            } else {
                passTimeMap.put(passTime, 1);
            }
            hit++;
            return;
        }

        // 没有命中缓存
        // 修复顺序问题
        if(ruList.size() != 0) {
            String firstKey = ruList.peekFirst();
            long firstTime = keyExpireMap.get(firstKey);
            if (firstTime - INTERVAL > time + FIX_TIME) {
                // 请求时间太长，导致日志打印过晚。
                invalid++;
                return;
            }
        }

        keyExpireMap.put(key, time + INTERVAL);
        ruList.addFirst(key);
        rget++;

    }

    /**
     * 移除过期的数据
     * 数据库中数据是按处理结束的时间来排序的，所以这里可能会稍微有些误差。
     */
    private void removeExpiredKey(long current) {
        while (ruList.size() != 0) {
              String lastKey = ruList.peekLast();// empty 返回 null
              long lastTime = keyExpireMap.get(lastKey);
              if(lastTime >= current) {
                  break;
              }
              ruList.removeLast();
              keyExpireMap.remove(lastKey);
        }
//        if(ruList.size() > 1) {
//            logger.info("rulist.Size{}", ruList.size());
//        }
    }

    public String getStat() {
        StringBuilder stat = new StringBuilder();
        stat.append("get: ").append(get).append(";");
        stat.append("hit: ").append(hit).append(";");
        stat.append("rget: ").append(rget).append(";");
        stat.append("invalid: ").append(invalid).append(";");
        stat.append("cacheSize: ").append(keyExpireMap.size()).append(";");
        double hitRate = (double)hit / (get - invalid);
        stat.append("请求归并率：").append(hitRate).append(";");
//        stat.append("hit 间隔统计：").append("\n");
//        for(long passTime: passTimeMap.keySet()) {
//            int count = passTimeMap.get(passTime);
//            stat.append("<").append(passTime);
//            stat.append("->").append(count).append(">;");
//        }
        return stat.toString();
    }

    private void logMilestone() {
        if (get % LOG_DEBUG_SIZE == 0) {
            logger.info(getStat());
            //logger.info("cache {}: {}", key, count);
        }
        if(get % CONSLE_DEBUG_SIZE == 0) {
            System.out.println(getStat());
        }
    }

}
