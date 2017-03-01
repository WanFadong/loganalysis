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
    private static final Logger logger = LogManager.getLogger();
    private static final int LOG_DEBUG_SIZE = 10000;
    private static final int CONSLE_DEBUG_SIZE = 100000;

    private Deque<String> ruList;// 访问顺序链表：按使用顺序保存map中key，维护淘汰顺序。类似于先进先出。
    private Map<String, Integer> cache;// 缓存数据容器。key->length
    private long maxSize;

    // 当前的状态
    private long leftSize;// 剩余空间
    private int get;// get 的次数
    private int hit;// hit 的次数
    private int set;// set 的次数
    private int eliminate;// 淘汰次数


    public LRUCache(long maxSize) {
        this.maxSize = maxSize;
        this.leftSize = maxSize;
        this.get = 0;
        this.set = 0;
        this.hit = 0;
        ruList = new LinkedList<String>();
        cache = new HashMap<String, Integer>();
    }


    public void getKey(String key, Integer length) {
        get++;
        logMilestone();
        // 命中缓存
        if (cache.containsKey(key)) {
            hit++;
            setFreshKey(key);
            return;
        }

        // 没有命中缓存
        if (setCache(key)) {
            set++;
            if (leftSize < length) {
                // 进行淘汰。
                removeOldestKey(length);
            }
            cache.put(key, length);
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
            Integer length = cache.remove(key);
            eliminate++;
            leftSize += length;
        }
    }

    public String getStat() {
        StringBuilder stat = new StringBuilder();
        stat.append("get: ").append(get).append(";");
        stat.append("hit: ").append(hit).append(";");
        stat.append("set: ").append(set).append(";");
        stat.append("eliminate: ").append(eliminate).append(";");
        stat.append("ruSize: ").append(ruList.size()).append(";");
        stat.append("cacheSize: ").append(cache.size()).append(";");
        return stat.toString();
    }

    private void logMilestone() {
        if (get % LOG_DEBUG_SIZE == 0) {
            logger.info(getStat());
            //logger.info("cache {}: {}", key, count);
        }
//        if(count % CONSLE_DEBUG_SIZE == 0) {
//            System.out.println("cache " + key + ": " + count);
//        }
    }

}

