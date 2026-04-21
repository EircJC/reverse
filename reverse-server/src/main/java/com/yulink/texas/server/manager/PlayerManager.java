package com.yulink.texas.server.manager;

import com.alibaba.fastjson.JSONObject;
import com.yulink.texas.common.utils.md5encrypt.Md5;
import com.yulink.texas.core.domain.Player;
import com.yulink.texas.core.service.PlayerService;
import com.yulink.texas.server.common.entity.BetPlayer;
import com.yulink.texas.server.common.entity.PlayerVO;
import com.yulink.texas.server.common.entity.RetMsg;
import com.yulink.texas.server.common.utils.JsonUtils;
import com.yulink.texas.server.common.utils.SkillCardsUtil;
import com.yulink.texas.server.common.utils.TexasStatic;
import com.yulink.texas.server.constants.SkillResultEnum;
import com.yulink.texas.server.ws.TexasUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/6
 * @Copyright (c) bitmain.com All Rights Reserved
 */

@Component
@Slf4j
public class PlayerManager {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private RoomManager roomManager;
    @Autowired
    private SkillCardsUtil skillCardsUtil;


    public int insertPlayer() {
        return playerService.insertPlayer();
    }

    public void register(Channel channel, String message) {
//        RetMsg rm = new RetMsg();
//        rm.setAction("onRegister");
//        Player player = new Player();
//        player = JsonUtils.fromJson(message, Player.class);
//        player.setUserpwd(Md5.GetMD5Code(player.getUserpwd()));
//        Player playerTemp = new Player();
//        playerTemp.setUsername(player.getUserName());
//        if (playerService.selectPlayer(playerTemp) == null) {
//            playerService.insertPlayer(player);
//            rm.setState(1);
//            rm.setMessage("注册成功");
//        } else {
//            rm.setState(0);
//            rm.setMessage("用户已存在");
//        }
//        player.setSession(session);
//        String retMsg = JsonUtils.toJson(rm, RetMsg.class);
//        TexasUtil.sendMsgToOne(player, retMsg);
    }

    public void login(Channel channel, String message) {
        RetMsg rm = new RetMsg();
        rm.setAction("onLogin");
        PlayerVO player = new PlayerVO();
        player = JsonUtils.fromJson(message, PlayerVO.class);
        Player currPlayer = playerService.getPlayerByNameAndPwd(player.getUserName(), Md5.GetMD5Code(player.getUserpwd()));
        if (currPlayer == null) {
            rm.setState(0);
            rm.setMessage("登录失败");
        } else {
            rm.setState(1);
            BeanUtils.copyProperties(currPlayer, player);
            player.setId(currPlayer.getId()+"");
            player.setChannel(channel);
            rm.setMessage(JsonUtils.toJson(player, PlayerVO.class));
            TexasStatic.loginPlayerMap.put(channel.id().asShortText(), player);
            TexasStatic.playerSessionMap.put(player.getId(), channel.id().asShortText()); // 玩家窗口多开是否需要重新设计数据模型
        }

        String retMsg = JsonUtils.toJson(rm, RetMsg.class);
        TexasUtil.sendMsgToOne(channel, retMsg);

    }

    public void betChips(Channel channel, String message) {

        BetPlayer bp = JsonUtils.fromJson(message, BetPlayer.class);
        PlayerVO p = TexasUtil.getPlayerByChannelId(channel.id().asShortText());
        // 玩家本次操作所下的筹码
        int chip = bp.getInChips();

        if (p != null && p.getRoom() != null) {
            if(SkillResultEnum.CONTINUE.getCode().equals(skillCardsUtil.trapSkillVerify(null, p, "B", message).getCode())) {
                roomManager.betchipIn(p, chip, true);
            } else {
                p.setThisRoundSkillAction("");
            }
        }
    }


    public void fold(Channel channel, String message) {
        // 弃牌 Fold，放弃本局游戏，并放弃所有已下的筹码。
        PlayerVO p = TexasUtil.getPlayerByChannelId(channel.id().asShortText());
        if (p != null && p.getRoom() != null) {
            if(SkillResultEnum.CONTINUE.getCode().equals(skillCardsUtil.trapSkillVerify(null, p, "F", message).getCode())) {
                roomManager.fold(p);
            } else {
                p.setThisRoundSkillAction("");
            }

        }
    }


    public void check(Channel channel, String message) {
        // 过牌
        // Check，不做任何操作过牌到下一个人，并保留下注的权利。过牌必须是在无需跟注的情况下使用，比如前面所有玩家都过牌或弃牌的情况。若前面的玩家一旦有人有下则不允许使用过牌。
        PlayerVO p = TexasUtil.getPlayerByChannelId(channel.id().asShortText());
        if (p != null && p.getRoom() != null) {
            if(SkillResultEnum.CONTINUE.getCode().equals(skillCardsUtil.trapSkillVerify(null, p, "C", message).getCode())) {
                roomManager.check(p, true);
            } else {
                p.setThisRoundSkillAction("");
            }
        }
    }


    public void standUp(Channel channel, String message) {
        // 站起

    }


    public void sitDown(Channel channel, String message) {
        // 坐下

    }

    public void assignChipsNum(Channel channel, String message) {
        PlayerVO currPlayer = TexasUtil.getPlayerByChannelId(channel.id().asShortText());
        RetMsg rm = new RetMsg();
        if (currPlayer == null) {
            rm.setState(0);
            rm.setMessage("请先登录");
            String retMsg = JsonUtils.toJson(rm, RetMsg.class);
            TexasUtil.sendMsgToOne(currPlayer, retMsg);
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(message);
		long takeChip = 5000;
		try {
            takeChip = jsonObject.getLong("assignChipsNum");
        } catch (Exception e) {
		    e.printStackTrace();
        }
		if(takeChip > 0) {
            synchronized (currPlayer) {
                currPlayer.setChips(takeChip);
                currPlayer.setBodyChips(takeChip);
                currPlayer.setExitRoomCountDownStartTime(0);
//            currPlayer.getRoom().checkStart(currPlayer.getRoom().getRestBetweenGame());
                boolean isCheckStart = true;
                for(PlayerVO player:currPlayer.getRoom().getWaitPlayers()) {
                    if(player.getBodyChips() == 0) {
                        isCheckStart = false;
                        break;
                    }
                }
                if(isCheckStart) {
                    roomManager.checkStart(currPlayer.getRoom(), 800);
                }

            }
        }

    }
}
