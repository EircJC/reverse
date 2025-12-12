package com.yulink.texas.core.mapper;

import com.yulink.texas.core.domain.SkillCombination;
import com.yulink.texas.core.domain.SkillCombinationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SkillCombinationMapper {
    long countByExample(SkillCombinationExample example);

    int deleteByExample(SkillCombinationExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SkillCombination record);

    int insertSelective(@Param("record") SkillCombination record, @Param("selective") SkillCombination.Column ... selective);

    SkillCombination selectOneByExample(SkillCombinationExample example);

    SkillCombination selectOneByExampleSelective(@Param("example") SkillCombinationExample example, @Param("selective") SkillCombination.Column ... selective);

    SkillCombination selectOneByExampleWithBLOBs(SkillCombinationExample example);

    List<SkillCombination> selectByExampleSelective(@Param("example") SkillCombinationExample example, @Param("selective") SkillCombination.Column ... selective);

    List<SkillCombination> selectByExampleWithBLOBs(SkillCombinationExample example);

    List<SkillCombination> selectByExample(SkillCombinationExample example);

    SkillCombination selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") SkillCombination.Column ... selective);

    SkillCombination selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SkillCombination record, @Param("example") SkillCombinationExample example, @Param("selective") SkillCombination.Column ... selective);

    int updateByExampleWithBLOBs(@Param("record") SkillCombination record, @Param("example") SkillCombinationExample example);

    int updateByExample(@Param("record") SkillCombination record, @Param("example") SkillCombinationExample example);

    int updateByPrimaryKeySelective(@Param("record") SkillCombination record, @Param("selective") SkillCombination.Column ... selective);

    int updateByPrimaryKeyWithBLOBs(SkillCombination record);

    int updateByPrimaryKey(SkillCombination record);

    int batchInsert(@Param("list") List<SkillCombination> list);

    int batchInsertSelective(@Param("list") List<SkillCombination> list, @Param("selective") SkillCombination.Column ... selective);

    int upsert(SkillCombination record);

    int upsertSelective(@Param("record") SkillCombination record, @Param("selective") SkillCombination.Column ... selective);

    int upsertWithBLOBs(SkillCombination record);
}