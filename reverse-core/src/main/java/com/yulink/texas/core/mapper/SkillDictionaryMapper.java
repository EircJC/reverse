package com.yulink.texas.core.mapper;

import com.yulink.texas.core.domain.SkillDictionary;
import com.yulink.texas.core.domain.SkillDictionaryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SkillDictionaryMapper {
    long countByExample(SkillDictionaryExample example);

    int deleteByExample(SkillDictionaryExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SkillDictionary record);

    int insertSelective(@Param("record") SkillDictionary record, @Param("selective") SkillDictionary.Column ... selective);

    SkillDictionary selectOneByExample(SkillDictionaryExample example);

    SkillDictionary selectOneByExampleSelective(@Param("example") SkillDictionaryExample example, @Param("selective") SkillDictionary.Column ... selective);

    List<SkillDictionary> selectByExampleSelective(@Param("example") SkillDictionaryExample example, @Param("selective") SkillDictionary.Column ... selective);

    List<SkillDictionary> selectByExample(SkillDictionaryExample example);

    SkillDictionary selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") SkillDictionary.Column ... selective);

    SkillDictionary selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SkillDictionary record, @Param("example") SkillDictionaryExample example, @Param("selective") SkillDictionary.Column ... selective);

    int updateByExample(@Param("record") SkillDictionary record, @Param("example") SkillDictionaryExample example);

    int updateByPrimaryKeySelective(@Param("record") SkillDictionary record, @Param("selective") SkillDictionary.Column ... selective);

    int updateByPrimaryKey(SkillDictionary record);

    int batchInsert(@Param("list") List<SkillDictionary> list);

    int batchInsertSelective(@Param("list") List<SkillDictionary> list, @Param("selective") SkillDictionary.Column ... selective);

    int upsert(SkillDictionary record);

    int upsertSelective(@Param("record") SkillDictionary record, @Param("selective") SkillDictionary.Column ... selective);

    List<SkillDictionary> getAllList();
}