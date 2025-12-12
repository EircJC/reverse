package com.yulink.texas.core.service;

import com.yulink.texas.core.domain.Player;
import com.yulink.texas.core.mapper.PlayerMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/6
 * @Copyright (c) bitmain.com All Rights Reserved
 */
@Service("playerService")
public class PlayerService {

    @Resource
    private PlayerMapper playerMapper;


    public Player getPlayerByNameAndPwd(String userName, String pwd) {
        return playerMapper.getPlayerByNameAndPwd(userName, pwd);
    }

    public int insertPlayer() {
        Player p = new Player();
        p.setUserName("张三");
        p.setUserpwd("123");
        return playerMapper.insert(p);
    }
}
