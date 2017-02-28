package com.somewan.qiniu;

import com.alibaba.fastjson.JSON;
import com.somewan.qiniu.model.User;
import com.somewan.qiniu.dao.UserDao;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

/**
 * Hello world!
 *
 */
public class UserMain {
    private static final Logger logger = LogManager.getLogger(UserMain.class);
    public static void main( String[] args )
    {
        String resource = "mybatis_config.xml";
        System.out.println(resource);
        InputStream inputStream = null;
        SqlSession session = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            session = sqlSessionFactory.openSession();
            UserDao mapper = session.getMapper(UserDao.class);
            User wang = new User();
            wang.setAge(20);
            wang.setName("wang");
            Timestamp time = new Timestamp(14876585467727038L / 10000L);
            wang.setBirth_time(time);
            mapper.insertUser("user_copy", wang);
            session.commit();
            logger.info(JSON.toJSONString(wang));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(session != null) {
                session.close();
            }
        }
    }
}
