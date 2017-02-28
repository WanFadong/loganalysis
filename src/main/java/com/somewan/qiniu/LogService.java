package com.somewan.qiniu;

import com.alibaba.fastjson.JSONObject;
import com.somewan.qiniu.dao.DcLogDao;
import com.somewan.qiniu.model.DcLog;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wan on 2017/2/27.
 */
public class LogService {
    private static final Logger logger = LogManager.getLogger();
    private static final String BASE_PATH = "/Users/wan/workspace/data/log/";
    private FileService fileService = new FileService();
    private DcLogDao dcLogDao;

    public LogService() {
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
     * 对某台机器的某个 dc 的日志进行处理。
     * @param machine
     * @param dc
     */
    public int record(String machine, String dc) {
        ExecutorService es = Executors.newFixedThreadPool(4);


        //DcLogDao dcLogDao = session.getMapper(DcLogDao.class);
        File[] files = getFile(machine, dc);
        System.out.println("文件数量：" + files.length);
        int count = 0;
        for(File file: files) {
            // 去掉 DS_Store 文件。
            if (file.getName().compareTo(".DS_Store") != 0) {
                count += recordOneFile(machine, dc, file);
            }
        }
        return count;
    }



    /**
     * 记录一个日志文件
     * @param file
     * @return 写入数据库的条目。
     */
    private int recordOneFile(String machine, String dc, File file) {
        System.out.println("正在处理： "+ file.getPath());
        int count = 0;
        try {
            fileService.initFileReader(file);
            String content;
            int line = 0;
            while((content = fileService.readLine()) != null) {
                line ++;
                DcLog dcLog = buildLog(file, content, line);
                if(dcLog == null) {
                    // 记录错误。
                } else {
                    record(machine, dc, dcLog);
                    count ++;
                    if (count % 100000 == 0) {
                        logger.info("file:{}; count:{}", file.getName(), count);
                    }
                }
            }
            fileService.close();
        } catch (Exception e) {
            logger.error("", e);
        }
        return count;
    }

    private File[] getFile(String machine, String dc) {
        String dirStr = BASE_PATH + machine + "/" + dc;
        File dir = new File(dirStr);
        File[] files = dir.listFiles();
        return files;
    }

    /**
     * 由 string 解析为 Object。
     * @param raw
     * @return 异常返回 null。
     */
    private DcLog buildLog(File file, String raw, int line) {
        try {
            DcLog log = new DcLog();
            String[] parts = raw.split("\t");
            if (parts.length != 12) {
                for(int i = 0; i < parts.length; i ++) {
                    logger.warn("第{}部分：{}", i, parts[i]);
                }
                throw new Exception("长度：" + parts.length);
            }

            long timeStamp = Long.valueOf(parts[2]);
            Timestamp time = new Timestamp(timeStamp / 10000L);

            // url
            String url = parts[4];
            String[] urlParts = url.split("/");
            int method;
            switch (urlParts[1]) {
                case "rangeGet":
                    method = 0;
                    break;
                case "get":
                    method = 1;
                    break;
                case "set":
                    method = 2;
                    break;
                case "stats":
                    method = 3;
                    break;
                default:
                    throw new Exception("method = " + urlParts[1]);
            }

            String key = null;
            if (method != 3) {
                key = urlParts[2];
            }
            Integer from = null;
            Integer to = null;
            if (method == 0) {
                from = Integer.valueOf(urlParts[3]);
                to = Integer.valueOf(urlParts[4]);
            }
            // length
            String header;
            if (method != 2) {
                // rangeGet || get || stats
                header = parts[8];
            } else {
                header = parts[5];
            }
            JSONObject jsonObject = JSONObject.parseObject(header);
            int length = jsonObject.getInteger("Content-Length");

            int responseCode = Integer.valueOf(parts[7]);
            String message = parts[9];
            int take = Integer.valueOf(parts[11]);

            // set
            log.setTime(time);
            log.setTime_stamp(timeStamp);
            log.setMethod(method);
            log.setUrl(url);
            log.setRequest_key(key);
            log.setRequest_from(from);
            log.setRequest_to(to);
            log.setLength(length);
            log.setResponse_code(responseCode);
            log.setMessage(message);
            log.setTake(take);

            return log;
        } catch (Exception e) {
            logger.warn("file: " + file.getPath() + " line: " + line + "content: " + raw);
            logger.error("", e);
            return null;
        }
    }

    private void record(String machine, String dc, DcLog log) {
        dcLogDao.insertLog(machine, dc, log);
    }

}
