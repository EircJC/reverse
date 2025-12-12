package com.yulink.texas.server.manager;

import com.alibaba.fastjson.JSONObject;
import com.yulink.texas.core.domain.SkillDictionary;
import com.yulink.texas.core.service.SkillDictionaryService;
import com.yulink.texas.server.common.utils.JsonUtils;
import com.yulink.texas.server.common.utils.RedisManager;
import com.yulink.texas.server.common.utils.SkillCardsUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/6
 * @Copyright (c) bitmain.com All Rights Reserved
 */

@Component
@Slf4j
public class SkillManager {

    @Resource
    private RedisManager rdisManager;

    @Resource
    private SkillDictionaryService skillDictionaryService;

    public List<SkillDictionary> getListByDB() {
        return skillDictionaryService.getAllList();
    }

    public List<SkillDictionary> getListByRedis() {
        try {
            List<SkillDictionary> list = null;
//            String value = rdisManager.getCacheValue("SkillDictionaryList");
            String value = "";
            if(StringUtils.isBlank(value)) {
                // 把技能卡字典放入redis
                list = getListByDB();
                String json = JsonUtils.toJsonAll(list, ArrayList.class);
                rdisManager.setCacheValueForTime("SkillDictionaryList", json, 600);
                return list;
            }
            list = JSONObject.parseArray(value, SkillDictionary.class);
            if(list == null && list.size() == 0) {
                return getListByDB();
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return getListByDB();
        }

    }

    public void useSkill(Session session, String message) {
        SkillCardsUtil.useSkill(session, message);
    }
}
