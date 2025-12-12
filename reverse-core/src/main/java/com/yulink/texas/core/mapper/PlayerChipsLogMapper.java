package com.yulink.texas.core.mapper;

import com.yulink.texas.core.domain.PlayerChipsLog;
import com.yulink.texas.core.domain.PlayerChipsLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PlayerChipsLogMapper {
    long countByExample(PlayerChipsLogExample example);

    int deleteByExample(PlayerChipsLogExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(PlayerChipsLog record);

    int insertSelective(@Param("record") PlayerChipsLog record, @Param("selective") PlayerChipsLog.Column ... selective);

    PlayerChipsLog selectOneByExample(PlayerChipsLogExample example);

    PlayerChipsLog selectOneByExampleSelective(@Param("example") PlayerChipsLogExample example, @Param("selective") PlayerChipsLog.Column ... selective);

    List<PlayerChipsLog> selectByExampleSelective(@Param("example") PlayerChipsLogExample example, @Param("selective") PlayerChipsLog.Column ... selective);

    List<PlayerChipsLog> selectByExample(PlayerChipsLogExample example);

    PlayerChipsLog selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") PlayerChipsLog.Column ... selective);

    PlayerChipsLog selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") PlayerChipsLog record, @Param("example") PlayerChipsLogExample example, @Param("selective") PlayerChipsLog.Column ... selective);

    int updateByExample(@Param("record") PlayerChipsLog record, @Param("example") PlayerChipsLogExample example);

    int updateByPrimaryKeySelective(@Param("record") PlayerChipsLog record, @Param("selective") PlayerChipsLog.Column ... selective);

    int updateByPrimaryKey(PlayerChipsLog record);

    int batchInsert(@Param("list") List<PlayerChipsLog> list);

    int batchInsertSelective(@Param("list") List<PlayerChipsLog> list, @Param("selective") PlayerChipsLog.Column ... selective);

    int upsert(PlayerChipsLog record);

    int upsertSelective(@Param("record") PlayerChipsLog record, @Param("selective") PlayerChipsLog.Column ... selective);
}