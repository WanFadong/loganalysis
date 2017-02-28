package com.somewan.qiniu.dao;

import com.somewan.qiniu.model.DcLog;
import org.apache.ibatis.annotations.Param;

/**
 * Created by wan on 2017/2/27.
 */
public interface DcLogDao {
    void insertLog(@Param("machine") String machine, @Param("dc") String dc, @Param("l") DcLog log);
}
