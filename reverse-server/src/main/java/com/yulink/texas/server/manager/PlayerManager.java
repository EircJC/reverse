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

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/6
 * @Copyright (c) bitmain.com All Rights Reserved
 */

@Component
@Slf4j
public class PlayerManager {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{4,20}$");

    @Autowired
    private PlayerService playerService;
    @Autowired
    private RoomManager roomManager;
    @Autowired
    private SkillCardsUtil skillCardsUtil;
    @Autowired
    private RegisterVerificationManager registerVerificationManager;


    public int insertPlayer() {
        return playerService.insertPlayer();
    }

    public void register(Channel channel, String message) {
        RetMsg rm = new RetMsg();
        rm.setAction("onRegister");
        try {
            JSONObject jsonObject = JSONObject.parseObject(message);
            String userName = safeTrim(jsonObject.getString("userName"));
            String userpwd = safeTrim(jsonObject.getString("userpwd"));
            String confirmPwd = safeTrim(jsonObject.getString("confirmPwd"));
            String email = normalizeEmail(jsonObject.getString("email"));
            String emailCode = safeTrim(jsonObject.getString("emailCode"));
            validateRegisterPayload(userName, userpwd, confirmPwd, email, emailCode);
            if (playerService.getPlayerByUserName(userName) != null) {
                throw new IllegalStateException("账号已存在，请更换用户名");
            }
            if (playerService.getPlayerByEmail(email) != null) {
                throw new IllegalStateException("邮箱已被注册");
            }
            registerVerificationManager.verifyAndConsume(email, emailCode);
            playerService.createPlayer(userName, Md5.GetMD5Code(userpwd), email);
            rm.setState(1);
            rm.setMessage("注册成功");
        } catch (Exception e) {
            rm.setState(0);
            rm.setMessage(e.getMessage() == null ? "注册失败" : e.getMessage());
        }
        TexasUtil.sendMsgToOne(channel, JsonUtils.toJson(rm, RetMsg.class));
    }

    public void sendRegisterCode(Channel channel, String message) {
        RetMsg rm = new RetMsg();
        rm.setAction("onSendRegisterCode");
        try {
            JSONObject jsonObject = JSONObject.parseObject(message);
            String email = normalizeEmail(jsonObject.getString("email"));
            String userName = safeTrim(jsonObject.getString("userName"));
            if (!USERNAME_PATTERN.matcher(userName).matches()) {
                throw new IllegalStateException("用户名需为 4-20 位字母、数字或下划线");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalStateException("邮箱格式不正确");
            }
            if (playerService.getPlayerByUserName(userName) != null) {
                throw new IllegalStateException("账号已存在，请更换用户名");
            }
            if (playerService.getPlayerByEmail(email) != null) {
                throw new IllegalStateException("邮箱已被注册");
            }
            registerVerificationManager.sendRegisterCode(email);
            rm.setState(1);
            rm.setMessage("验证码已发送，请查收邮箱");
        } catch (Exception e) {
            rm.setState(0);
            rm.setMessage(e.getMessage() == null ? "验证码发送失败" : e.getMessage());
        }
        TexasUtil.sendMsgToOne(channel, JsonUtils.toJson(rm, RetMsg.class));
    }

    public void logout(Channel channel, String message) {
        RetMsg rm = new RetMsg();
        rm.setAction("onLogout");
        PlayerVO currPlayer = TexasUtil.getPlayerByChannelId(channel.id().asShortText());
        if (currPlayer == null) {
            rm.setState(1);
            rm.setMessage("当前未登录");
            TexasUtil.sendMsgToOne(channel, JsonUtils.toJson(rm, RetMsg.class));
            return;
        }
        if (currPlayer.getRoom() != null) {
            roomManager.outRoom(channel, "", false);
        }
        TexasStatic.loginPlayerMap.remove(channel.id().asShortText());
        TexasStatic.playerSessionMap.remove(currPlayer.getId(), channel.id().asShortText());
        rm.setState(1);
        rm.setMessage("已退出登录");
        TexasUtil.sendMsgToOne(channel, JsonUtils.toJson(rm, RetMsg.class));
    }

    public void login(Channel channel, String message) {
        RetMsg rm = new RetMsg();
        rm.setAction("onLogin");
        PlayerVO player = JsonUtils.fromJson(message, PlayerVO.class);
        String userName = safeTrim(player.getUserName());
        String userpwd = safeTrim(player.getUserpwd());
        Player currPlayer = null;
        if (userName.length() == 0) {
            rm.setState(0);
            rm.setMessage("请输入用户名");
        } else if (userpwd.length() == 0) {
            rm.setState(0);
            rm.setMessage("请输入密码");
        } else {
            Player existPlayer = playerService.getPlayerByUserName(userName);
            if (existPlayer == null) {
                rm.setState(0);
                rm.setMessage("账号不存在");
                TexasUtil.sendMsgToOne(channel, JsonUtils.toJson(rm, RetMsg.class));
                return;
            }
            currPlayer = playerService.getPlayerByNameAndPwd(userName, Md5.GetMD5Code(userpwd));
        }
        if (rm.getState() == 0 && rm.getMessage() != null) {
            TexasUtil.sendMsgToOne(channel, JsonUtils.toJson(rm, RetMsg.class));
            return;
        }
        if (currPlayer == null) {
            rm.setState(0);
            rm.setMessage("密码错误");
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

    private void validateRegisterPayload(String userName, String userpwd, String confirmPwd, String email, String emailCode) {
        if (!USERNAME_PATTERN.matcher(userName).matches()) {
            throw new IllegalStateException("用户名需为 4-20 位字母、数字或下划线");
        }
        if (userpwd == null || userpwd.length() < 6) {
            throw new IllegalStateException("密码至少需要 6 位");
        }
        if (!userpwd.equals(confirmPwd)) {
            throw new IllegalStateException("两次输入的密码不一致");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalStateException("邮箱格式不正确");
        }
        if (emailCode == null || emailCode.length() != 6) {
            throw new IllegalStateException("请输入 6 位邮箱验证码");
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeEmail(String value) {
        return safeTrim(value).toLowerCase(Locale.ROOT);
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
