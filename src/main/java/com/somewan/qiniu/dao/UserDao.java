package com.somewan.qiniu.dao;

import com.somewan.qiniu.model.User;
import org.apache.ibatis.annotations.Param;

/**
 * Created by wan on 2017/2/27.
 */

public interface UserDao {
    User selectUser(@Param("id") int id);

    void insertUser(@Param("tableName") String tableName, @Param("user") User user);
}
