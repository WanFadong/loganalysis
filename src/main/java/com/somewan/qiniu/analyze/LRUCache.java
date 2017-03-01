package com.somewan.qiniu.analyze;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by wan on 2017/1/25.
 */
public class LRUCache {

    class Item {
        int length;
        long effectiveTime;

        public Item(int length, long effectiveTime) {
            this.length = length;
            this.effectiveTime = effectiveTime;
        }
    }

    private static final Logger logger = LogManager.getLogger();
    private static final int LOG_DEBUG_SIZE = 10000;
    private static final int CONSLE_DEBUG_SIZE = 100000;

    private Deque<String> ruList;// 访问顺序链表：按使用顺序保存map中key，维护淘汰顺序。类似于先进先出。
    private Map<String, Item> memoryCache;// 缓存数据容器

    // 当前的状态
    private long leftSize;// 剩余空间
    private int get;// dc get 的次数 get = hit + ybc
    private int hit;// mc hit 的次数
    private int setting;// mc 缓存正在设置的次数。这时会从 ybc 读取
    private int ybc;// dc 读磁盘（ybc）的次数。ybc = setting + miss
    private int eliminate;// mc 淘汰次数


    public LRUCache(long maxSize) {
        this.leftSize = maxSize;
        this.get = 0;
        this.ybc = 0;
        this.hit = 0;
        ruList = new LinkedList<String>();
        memoryCache = new HashMap<String, Item>();
    }


    public void getKey(String key, int length, long requestTime, long take) {
        get++;
        logMilestone();
        // 命中缓存
        if (memoryCache.containsKey(key)) {
            Item item = memoryCache.get(key);

            if (item.effectiveTime <= requestTime) {
                hit++;
            } else {
                // 缓存没有设置完毕。但不需要设置缓存了。
                setting++;
                ybc++;
            }
            setFreshKey(key);
            return;
        }

        // 没有命中缓存
        if (setCache(key)) {
            ybc++;
            if (leftSize < length) {
                // 进行淘汰。
                removeOldestKey(length);
            }
            memoryCache.put(key, new Item(length, requestTime + take));
            leftSize -= length;
            setFreshKey(key);
        }
    }

    /**
     * 缓存策略
     *
     * @param key
     * @return
     */
    private boolean setCache(String key) {
        return true;
    }

    /**
     * 把最新访问(get/add)的key放到链表最前面。
     *
     * @param key
     */
    private void setFreshKey(String key) {
        if (!key.equals(ruList.peekFirst())) {// 如果list为空，那么peekFirst返回null。
            ruList.remove(key);// 如果key不存在，那么rulist不会变动。
            ruList.addFirst(key);
        }
    }

    /**
     * 淘汰数据,腾出空间
     */
    private void removeOldestKey(int needSize) {
        while (leftSize < needSize) {
            String key = ruList.pollLast();
            Integer length = memoryCache.remove(key).length;
            eliminate++;
            leftSize += length;
        }
    }

    public String getStat() {
        StringBuilder stat = new StringBuilder();
        stat.append("get: ").append(get).append(";");
        stat.append("hit: ").append(hit).append(";");
        stat.append("setting: ").append(setting).append(";");
        stat.append("ybc: ").append(ybc).append(";");
        stat.append("eliminate: ").append(eliminate).append(";");
        stat.append("cacheSize: ").append(memoryCache.size()).append(";");
        double hitRate = (double)hit / get;
        stat.append("hitRage：").append(hitRate).append(";");
        return stat.toString();
    }

    private void logMilestone() {
        if (get % LOG_DEBUG_SIZE == 0) {
            logger.info(getStat());
        }
        if(get % CONSLE_DEBUG_SIZE == 0) {
            System.out.println(getStat());
        }
    }

}

