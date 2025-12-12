package com.yulink.texas.server.controller;

import com.yulink.texas.server.manager.PlayerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/9
 * @Copyright (c) bitmain.com All Rights Reserved
 */
@Controller
@Slf4j
public class Hello {

    @Autowired
    private PlayerManager playerManager;

    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
//        List<String> pss = StreamSupport
//            .stream(environment.getPropertySources().spliterator(), false).filter(propertySource -> propertySource instanceof OriginTrackedMapPropertySource).map(propertySource -> propertySource.getName()).collect(
//                Collectors.toList());
//        log.info("加载的配置文件有：" + pss.toString());
//        List<Player> playerList = playerManager.getList();
        return "hello";
    }
}
