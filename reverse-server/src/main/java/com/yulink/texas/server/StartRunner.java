package com.yulink.texas.server;

import com.yulink.texas.server.manager.SkillManager;
import javax.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/16
 * @Copyright (c) bitmain.com All Rights Reserved
 */

@Component
@Order(value = 1)
public class StartRunner implements ApplicationRunner {

    @Resource
    private SkillManager skillManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        skillManager.getListByRedis();
    }

}
