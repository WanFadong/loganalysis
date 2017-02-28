package com.somewan.qiniu.analyze;

import com.somewan.qiniu.dao.DcLogDao;
import com.somewan.qiniu.model.DcLog;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by wan on 2017/2/28.
 */
public class Simulation {
    private static final Logger logger = LogManager.getLogger();
    private static final long LOG_BEGIN = 14879277589146833L;// 2017/2/24 17:15:58
    private static final long LOG_END = 14881920605004773L;// 2017/2/27 18:41:00
    private static final int INTERVAL = 600000000;// 1min
    private static final long MAX_MEM = 2684354560L;// 2.5G,= 256K * 10240条

    private DcLogDao dcLogDao;
    private LRUCache lru;

    public Simulation() {
        lru = new LRUCache(MAX_MEM);
        try {
            String resource = "mybatis_config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            SqlSession session = sqlSessionFactory.openSession(true);
            dcLogDao = session.getMapper(DcLogDao.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对 DC 行为进行模拟
     * @return 缓存转台：缓存 get/hit/set 多少次
     */
    public String simulate() {
        String machine = "nb252";
        String dc = "dc3";
        long begin = LOG_BEGIN;
        long end = LOG_BEGIN;

        // 需要这个等于号。因为取出的数据不包括 end。
        while(end <= LOG_END) {
            end = begin + INTERVAL;
            List<DcLog> logList = dcLogDao.selectLog(machine, dc, begin, end);
            simulatePartLog(logList);
            begin = end;
        }
        return lru.getStat();
    }

    private void simulatePartLog(List<DcLog> logList){
        System.out.println("log list size: " + logList.size());
        for(DcLog log: logList) {
            String key = log.getRequest_key();
            int length = log.getLength();
            lru.getKey(key, length);
        }
    }

    public static void main(String[] args) {
        System.out.println("start");
        logger.info("start");
        Simulation simulation = new Simulation();
        String stat = simulation.simulate();
        System.out.println(stat);
    }
}
