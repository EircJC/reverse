package com.yulink.texas.core.mapper;

import com.yulink.texas.core.domain.SystemLog;
import com.yulink.texas.core.domain.SystemLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SystemLogMapper {
    long countByExample(SystemLogExample example);

    int deleteByExample(SystemLogExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SystemLog record);

    int insertSelective(@Param("record") SystemLog record, @Param("selective") SystemLog.Column ... selective);

    SystemLog selectOneByExample(SystemLogExample example);

    SystemLog selectOneByExampleSelective(@Param("example") SystemLogExample example, @Param("selective") SystemLog.Column ... selective);

    SystemLog selectOneByExampleWithBLOBs(SystemLogExample example);

    List<SystemLog> selectByExampleSelective(@Param("example") SystemLogExample example, @Param("selective") SystemLog.Column ... selective);

    List<SystemLog> selectByExampleWithBLOBs(SystemLogExample example);

    List<SystemLog> selectByExample(SystemLogExample example);

    SystemLog selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") SystemLog.Column ... selective);

    SystemLog selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SystemLog record, @Param("example") SystemLogExample example, @Param("selective") SystemLog.Column ... selective);

    int updateByExampleWithBLOBs(@Param("record") SystemLog record, @Param("example") SystemLogExample example);

    int updateByExample(@Param("record") SystemLog record, @Param("example") SystemLogExample example);

    int updateByPrimaryKeySelective(@Param("record") SystemLog record, @Param("selective") SystemLog.Column ... selective);

    int updateByPrimaryKeyWithBLOBs(SystemLog record);

    int updateByPrimaryKey(SystemLog record);

    int batchInsert(@Param("list") List<SystemLog> list);

    int batchInsertSelective(@Param("list") List<SystemLog> list, @Param("selective") SystemLog.Column ... selective);

    int upsert(SystemLog record);

    int upsertSelective(@Param("record") SystemLog record, @Param("selective") SystemLog.Column ... selective);

    int upsertWithBLOBs(SystemLog record);
}