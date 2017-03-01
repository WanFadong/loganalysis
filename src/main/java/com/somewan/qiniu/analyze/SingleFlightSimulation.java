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
 * 模拟请求归并的效果
 * Created by wan on 2017/3/1.
 */
public class SingleFlightSimulation {
    private static final Logger logger = LogManager.getLogger();
    private static final long ID_BEGIN = 1L;//
    private static final long ID_END = 11000000L;//
    private static final int INTERVAL = 3000;//

    private DcLogDao dcLogDao;
    private ExpiresCache expiresCache;

    public SingleFlightSimulation() {
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
    public String simulate(String machine, String dc) {
        expiresCache = new ExpiresCache();
        long begin = ID_BEGIN;
        long end = ID_BEGIN;

        // 需要这个等于号。因为取出的数据不包括 end。
        while(end <= ID_END) {
            end = begin + INTERVAL;
            List<DcLog> logList = dcLogDao.select404Log(machine, dc, begin, end);
            simulatePartLog(logList);
            begin = end;
        }
        return expiresCache.getStat();
    }

    private void simulatePartLog(List<DcLog> logList){
        //System.out.println("log list size: " + logList.size());
        for(DcLog log: logList) {
            String key = log.getRequest_key();
            long time = log.getTime_stamp() / 10000L;
            int method = log.getMethod();
            if (method == 0) {
                expiresCache.get(key, time);
            }
        }
        logList = null;// 加速内存回收
    }

    public static void main(String[] args) {

        System.out.println("start");
        logger.info("start");
        SingleFlightSimulation simulation = new SingleFlightSimulation();
        String[] machines = {"nb252", "xs300"};
        String[] dcs = {"dc3", "dc9"};
        for(int i = 0; i < machines.length; i++) {
            for(int j = 0; j < dcs.length; j++) {
                if(i != 0 || j != 0) {
                    return;
                }
                String stat = simulation.simulate(machines[i], dcs[j]);
                System.out.println(stat);
                logger.info("{}", stat);
            }
        }

    }
}
