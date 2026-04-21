package com.yulink.texas.core.service;

import com.yulink.texas.core.domain.Player;
import com.yulink.texas.core.domain.PlayerExample;
import com.yulink.texas.core.mapper.PlayerMapper;
import java.util.Date;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/6
 * @Copyright (c) bitmain.com All Rights Reserved
 */
@Service("playerService")
public class PlayerService {

    private static final long DEFAULT_INIT_CHIPS = 100000L;

    @Resource
    private PlayerMapper playerMapper;


    public Player getPlayerByNameAndPwd(String userName, String pwd) {
        return playerMapper.getPlayerByNameAndPwd(userName, pwd);
    }

    public Player getPlayerByUserName(String userName) {
        PlayerExample example = new PlayerExample();
        example.createCriteria().andUsernameEqualTo(userName);
        return playerMapper.selectOneByExample(example);
    }

    public Player getPlayerByEmail(String email) {
        PlayerExample example = new PlayerExample();
        example.createCriteria().andEmailEqualTo(email);
        return playerMapper.selectOneByExample(example);
    }

    public Player createPlayer(String userName, String encryptedPwd, String email) {
        Player player = new Player();
        player.setUserName(userName);
        player.setNickName(userName);
        player.setUserpwd(encryptedPwd);
        player.setEmail(email);
        player.setChips(DEFAULT_INIT_CHIPS);
        player.setRegdate(new Date());
        player.setStatus("1");
        player.setIsrobot("0");
        player.setType("normal");
        playerMapper.insertRegisterPlayer(player);
        return player;
    }

    public int insertPlayer() {
        Player p = new Player();
        p.setUserName("张三");
        p.setUserpwd("123");
        return playerMapper.insert(p);
    }
}
