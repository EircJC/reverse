package com.yulink.texas.core.service;

import com.yulink.texas.core.domain.SkillDictionary;
import com.yulink.texas.core.mapper.SkillDictionaryMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/6
 * @Copyright (c) bitmain.com All Rights Reserved
 */
@Service("skillDictionaryService")
public class SkillDictionaryService {

    @Resource
    private SkillDictionaryMapper skillDictionaryMapper;

    public List<SkillDictionary> getAllList() {
        return skillDictionaryMapper.getAllList();
    }

}
