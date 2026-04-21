package com.yulink.texas.core.mapper;

import com.yulink.texas.core.domain.Player;
import com.yulink.texas.core.domain.PlayerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PlayerMapper {
    long countByExample(PlayerExample example);

    int deleteByExample(PlayerExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Player record);

    int insertSelective(@Param("record") Player record, @Param("selective") Player.Column ... selective);

    Player selectOneByExample(PlayerExample example);

    Player selectOneByExampleSelective(@Param("example") PlayerExample example, @Param("selective") Player.Column ... selective);

    List<Player> selectByExampleSelective(@Param("example") PlayerExample example, @Param("selective") Player.Column ... selective);

    List<Player> selectByExample(PlayerExample example);

    Player selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") Player.Column ... selective);

    Player selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Player record, @Param("example") PlayerExample example, @Param("selective") Player.Column ... selective);

    int updateByExample(@Param("record") Player record, @Param("example") PlayerExample example);

    int updateByPrimaryKeySelective(@Param("record") Player record, @Param("selective") Player.Column ... selective);

    int updateByPrimaryKey(Player record);

    int batchInsert(@Param("list") List<Player> list);

    int batchInsertSelective(@Param("list") List<Player> list, @Param("selective") Player.Column ... selective);

    int upsert(Player record);

    int upsertSelective(@Param("record") Player record, @Param("selective") Player.Column ... selective);

    int insertRegisterPlayer(Player record);

    Player getPlayerByNameAndPwd(@Param("userName") String userName, @Param("pwd") String pwd);
}
