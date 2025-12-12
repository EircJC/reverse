package com.yulink.texas.core.mapper;

import com.yulink.texas.core.domain.SkillCard;
import com.yulink.texas.core.domain.SkillCardExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SkillCardMapper {
    long countByExample(SkillCardExample example);

    int deleteByExample(SkillCardExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SkillCard record);

    int insertSelective(@Param("record") SkillCard record, @Param("selective") SkillCard.Column ... selective);

    SkillCard selectOneByExample(SkillCardExample example);

    SkillCard selectOneByExampleSelective(@Param("example") SkillCardExample example, @Param("selective") SkillCard.Column ... selective);

    List<SkillCard> selectByExampleSelective(@Param("example") SkillCardExample example, @Param("selective") SkillCard.Column ... selective);

    List<SkillCard> selectByExample(SkillCardExample example);

    SkillCard selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") SkillCard.Column ... selective);

    SkillCard selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SkillCard record, @Param("example") SkillCardExample example, @Param("selective") SkillCard.Column ... selective);

    int updateByExample(@Param("record") SkillCard record, @Param("example") SkillCardExample example);

    int updateByPrimaryKeySelective(@Param("record") SkillCard record, @Param("selective") SkillCard.Column ... selective);

    int updateByPrimaryKey(SkillCard record);

    int batchInsert(@Param("list") List<SkillCard> list);

    int batchInsertSelective(@Param("list") List<SkillCard> list, @Param("selective") SkillCard.Column ... selective);

    int upsert(SkillCard record);

    int upsertSelective(@Param("record") SkillCard record, @Param("selective") SkillCard.Column ... selective);
}