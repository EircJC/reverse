package com.yulink.texas.core.mapper;

import com.yulink.texas.core.domain.GameLog;
import com.yulink.texas.core.domain.GameLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface GameLogMapper {
    long countByExample(GameLogExample example);

    int deleteByExample(GameLogExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(GameLog record);

    int insertSelective(@Param("record") GameLog record, @Param("selective") GameLog.Column ... selective);

    GameLog selectOneByExample(GameLogExample example);

    GameLog selectOneByExampleSelective(@Param("example") GameLogExample example, @Param("selective") GameLog.Column ... selective);

    GameLog selectOneByExampleWithBLOBs(GameLogExample example);

    List<GameLog> selectByExampleSelective(@Param("example") GameLogExample example, @Param("selective") GameLog.Column ... selective);

    List<GameLog> selectByExampleWithBLOBs(GameLogExample example);

    List<GameLog> selectByExample(GameLogExample example);

    GameLog selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") GameLog.Column ... selective);

    GameLog selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") GameLog record, @Param("example") GameLogExample example, @Param("selective") GameLog.Column ... selective);

    int updateByExampleWithBLOBs(@Param("record") GameLog record, @Param("example") GameLogExample example);

    int updateByExample(@Param("record") GameLog record, @Param("example") GameLogExample example);

    int updateByPrimaryKeySelective(@Param("record") GameLog record, @Param("selective") GameLog.Column ... selective);

    int updateByPrimaryKeyWithBLOBs(GameLog record);

    int updateByPrimaryKey(GameLog record);

    int batchInsert(@Param("list") List<GameLog> list);

    int batchInsertSelective(@Param("list") List<GameLog> list, @Param("selective") GameLog.Column ... selective);

    int upsert(GameLog record);

    int upsertSelective(@Param("record") GameLog record, @Param("selective") GameLog.Column ... selective);

    int upsertWithBLOBs(GameLog record);
}