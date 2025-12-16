package com.yulink.texas.server.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.yulink.texas.common.card.Card;
import com.yulink.texas.server.common.entity.BetPlayer;
import com.yulink.texas.server.common.entity.PlayerVO;
import com.yulink.texas.server.common.entity.PrivateRoom;
import com.yulink.texas.server.common.entity.RetMsg;
import com.yulink.texas.server.common.entity.SkillDictionaryVO;
import com.yulink.texas.server.common.entity.UseSkill;
import com.yulink.texas.server.common.room.Room;
import com.yulink.texas.server.constants.SkillResultEnum;
import com.yulink.texas.server.constants.SkillTypeEnum;
import com.yulink.texas.server.manager.RoomManager;
import com.yulink.texas.server.ws.TexasUtil;
import io.netty.channel.Channel;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/16
 * @Copyright (c) bitmain.com All Rights Reserved
 */

@Component
public class SkillCardsUtil {
    private static Logger logger = LogManager.getLogger(SkillCardsUtil.class);

    private static int skillAwardCount = 1;

    private static int skillInitCount = 2;

    private static int powerAwardCount = 2;

    private static int powerInitCount = 15;

    // 第三方承受技能玩家 例如：SDN_18等
    private static PlayerVO thirdDestPlayer = null;

    @Autowired
    public RoomManager roomManager;
    /**
     * 为房间中正在游戏的玩家分配技能卡
     * @param room
     * @param count
     * @param isSkill 是否为技能卡主动触发，true则无视当前用户身上的停发技能卡debuff影响
     */
    public static void assignSkillCardByRoom(Room room, int count, boolean isSkill) {
        if(count == 0) {
            return;
        }
        // 系统卡牌版
        if(room.getType() == 1) {
            for (PlayerVO p : room.getIngamePlayers()) {
                // 如果是刚开局，初始化玩家信息和技能卡顺序
                if(room.getCalcCommunityCards().size() == 0) {
                    // 针对每个玩家初始化技能卡组，注意这里要值传递，千万别搞成引用传递
                    List<SkillDictionaryVO> skillCards = new CopyOnWriteArrayList<SkillDictionaryVO>
                        (Arrays.asList(new SkillDictionaryVO[room.getSkillCards().size()]));
                    Collections.copy(skillCards, room.getSkillCards());

                    p.setPower(powerInitCount); // 每个玩家初始6个能量, 有用户可以通过道具提升初始能量数
                    Collections.shuffle(skillCards); // 技能卡洗牌
                    p.setSkillCards(skillCards.subList(0,skillCards.size() > 30 ? 30 : skillCards.size())); // 取30张加入卡组
                } else {
                    if(!isSkill && p.getStopGetSkillCardRound() > 0) {
                        // 如果当前玩家有N个回合停发技能卡状态，则持续回合数 -1并继续执行下一个玩家
                        // isSkill=true 技能主动领卡除外（技能卡 更多选择：从牌库抽2张技能卡；流放：放弃一张技能卡再重新抽一张； 等）
                        p.setStopGetSkillCardRound(p.getStopGetSkillCardRound() -1);
                        continue;
                    }
                    // TODO 后期加上每个用户道具规则 能量额外提升数量 && 卡牌额外提升数量
                    int powerRaise = powerAwardCount + 0;
                    p.setPower(p.getPower()+powerRaise);
                }
                List<SkillDictionaryVO> tmp = new ArrayList<>();
                for(int i = 0; i < count; i++) {
                    tmp.add(p.getSkillCards().get(i));
                    p.getSkillCards().remove(i);
                }
                if(p.getPlayerSkillCards() == null) {
                    p.setPlayerSkillCards(tmp);
                } else {
                    p.getPlayerSkillCards().addAll(tmp);
                }

                /**
                 * 判断玩家本回合是否可以使用技能卡；StopUseSkillCardRound如果不为0，则数量-1，当前使用开关设置为false
                 * 本method参数 isSkill=false 表示正常PFTR回合随着公共牌发放时调用，每次触发相当于到了下一回合
                 *
                 * 例如：
                 * P回合：玩家A对玩家B使用冻结，B玩家StopUseSkillCardRound=1、isSkillUsed=false，此时当前回合还未结束B玩家依然可以使用技能；
                 * F回合：本方法调用时进行公共牌发放和技能卡下发，此时相当于B玩家冻结生效、停用次数-1：StopUseSkillCardRound=0、isSkillUsed=true
                 *
                 */
                if(p.getStopUseSkillCardRound() > 0) {
                    p.setStopUseSkillCardRound(p.getStopUseSkillCardRound() -1);
                    p.setSkillUsed(false);
                } else {
                    p.setSkillUsed(true);
                }
                p.setPlayerSkillCardsCount(p.getPlayerSkillCards() != null? p.getPlayerSkillCards().size() : 0);
            }
        }
    }

    /**
     * 为房间中正在游戏的玩家分配技能卡
     * @param room
     * @param player
     * @param count
     */
    public static void assignSkillCardByPlayer(Room room, PlayerVO player, int count) {
        if(count == 0) {
            return;
        }
        // 系统卡牌版
        if(room.getType() == 1) {
            List<SkillDictionaryVO> tmp = new ArrayList<>();
            for(int i = 0; i < count; i++) {
                tmp.add(player.getSkillCards().get(i));
                player.getSkillCards().remove(i);
            }
            if(player.getPlayerSkillCards() == null) {
                player.setPlayerSkillCards(tmp);
            } else {
                player.getPlayerSkillCards().addAll(tmp);
            }

            player.setPlayerSkillCardsCount(player.getPlayerSkillCards() != null? player.getPlayerSkillCards().size() : 0);
        }
    }

    /**
     * 使用技能卡
     * @param session
     * @param message
     */
    public void useSkill(Channel channel, String message) {
        UseSkill useSkill = JSONObject.parseObject(message, UseSkill.class);
        PlayerVO player = TexasUtil.getPlayerByChannelId(channel.id().asShortText());
        Room room = player.getRoom();

        thirdDestPlayer = null;
        // 普通德扑房间技能卡无效
        if(room.getType() == 0) {
            return;
        }

        // 玩家自定义卡组房间需要判断卡使用次数是否够用
        if(room.getType() == 2) {
            //TODO 判断次数业务逻辑
            return;
        }
        // 是否轮到该玩家操作
        if (player.getSeatNum() != room.getNextturn()) {
            TexasUtil.sendErrorMsg("onUseSkill", "傻逼不该你操作，等你回合再动！！", player);
            return;
        }

        SkillDictionaryVO skillDictionaryVO = null;
        for(SkillDictionaryVO tmp: player.getPlayerSkillCards()) {
            if(tmp.getSkillDictionaryNo().equals(useSkill.getSkillDictionaryNo())) {
                skillDictionaryVO = tmp;
                break;
            }
        }
        if(skillDictionaryVO == null) {
            TexasUtil.sendErrorMsg("onUseSkill", "技能不存在", player);
            return;
        }
        if(player.getPower() < skillDictionaryVO.getPower()) {
            TexasUtil.sendErrorMsg("onUseSkill", "能量不够", player);
            return;
        }

        if(skillDictionaryVO.getUseRound().indexOf(room.getCurrentRound()) == -1) {
            TexasUtil.sendErrorMsg("onUseSkill", "当前回合无法使用技能 :"+skillDictionaryVO.getSkillNameZh(), player);
            return;
        }

        // 主动指向技需要判断目标是否在游戏中
        PlayerVO destPlayer = null;
        if(skillDictionaryVO.getPointTo().equals("1")) {
            if(StringUtils.isBlank(useSkill.getDestPlayerId())) {
                TexasUtil.sendErrorMsg("onUseSkill", "请选择目标玩家", player);
                return;
            }
            for(PlayerVO p: room.getIngamePlayers()) {
                if(p.getId().equals(useSkill.getDestPlayerId())) {
                    destPlayer = p;
                    break;
                }
            }
            if(destPlayer == null) {
                TexasUtil.sendErrorMsg("onUseSkill", "目标玩家不在游戏中", player);
                return;
            }
        }

        //具体技能业务处理
        try {
            // 执行结果
            SkillResultEnum result = SkillResultEnum.NONE;
            SkillDictionaryVO skillDefenseCard = null;

            while(true) {
                // 判断是否会触发陷阱
                trapSkillVerify(skillDictionaryVO, player, "S", message);
                // 执行主动技能
                if(SkillTypeEnum.ACTIVE.getCode().equals(skillDictionaryVO.getType())) {
                    Class clazz = SkillCardsUtil.class;
                    String methodName;
                    SkillResultEnum defenseResult = SkillResultEnum.NONE; // 是否有防御技能对本指向技有影响
                    // 判断是否为指向技能 & 目标玩家是否有防御技能
                    if("1".equals(skillDictionaryVO.getPointTo())) {
                        List<SkillDictionaryVO> skillDefenseCards = destPlayer.getSkillDefenseCards();
                        if(skillDefenseCards != null && skillDefenseCards.size() > 0) {
                            for(SkillDictionaryVO skillDefense: skillDefenseCards) {
                                methodName = skillDefense.getSkillDictionaryNo();
                                Method method = clazz.getMethod(methodName, SkillDictionaryVO.class, Room.class, UseSkill.class, PlayerVO.class, PlayerVO.class);
                                defenseResult = (SkillResultEnum) method.invoke(null, new Object[]{skillDefense, room, useSkill, player, destPlayer});
                                if(SkillResultEnum.DONE.getCode().equals(defenseResult.getCode())) {
                                    skillDefenseCard = skillDefense;
                                    // 如果防御技能为抵挡类型，则直接跳过并且不触发改主动技能实际效果
                                    if(methodName.equals("SDN_DEFENSE_1") || methodName.equals("SDN_DEFENSE_3")) {
                                        result = SkillResultEnum.DONE;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    if(SkillResultEnum.DONE.getCode().equals(result.getCode())) {
                        break;
                    }
                    methodName = skillDictionaryVO.getSkillDictionaryNo();
                    Method method = clazz.getMethod(methodName, SkillDictionaryVO.class, Room.class, UseSkill.class, PlayerVO.class, PlayerVO.class);
                    result = (SkillResultEnum) method.invoke(null, new Object[]{skillDictionaryVO, room, useSkill, player, thirdDestPlayer != null? thirdDestPlayer: destPlayer});
                }

                // 如果是防御技能，直接放入对应玩家防御列表
                if(SkillTypeEnum.DEFENSE.getCode().equals(skillDictionaryVO.getType())) {
                    result = doSkillDefenseCards(skillDictionaryVO, player);
                }

                // 如果是陷阱技能，直接放入对应房间陷阱列表
                if(SkillTypeEnum.TRAP.getCode().equals(skillDictionaryVO.getType())) {
                    result = doSkillTrapCards(skillDictionaryVO, player, room);
                }
                break;
            }

            if(SkillResultEnum.DONE.getCode().equals(result.getCode())) {
                // 消耗能量扣减
                player.setPower(player.getPower() - skillDictionaryVO.getPower() - player.getExtPower());
                if(player.getPower() < 0) {
                    player.setPower(0);
                }
                player.setExtPower(0);
                // 移除本次消耗技能卡
                removeSkillCard(player, skillDictionaryVO.getSkillDictionaryNo());
                // 通知所有玩家
                sendSkillMessage("onUseSkill", player, room.getIngamePlayers(), destPlayer, thirdDestPlayer, skillDictionaryVO, skillDefenseCard, null);
                sendSkillMessage("onUseSkill", player, room.getWaitPlayers(), destPlayer, thirdDestPlayer, skillDictionaryVO, skillDefenseCard, null);
            } else {
                TexasUtil.sendErrorMsg("onUseSkill", "技能释放失败，请检查网络参数或联系管理员", player);
            }
        } catch (Exception e) {
            TexasUtil.sendErrorMsg("onUseSkill", "技能释放失败，请联系管理员", player);
            e.printStackTrace();
        }
    }

    private static void sendSkillMessage(String action, PlayerVO player, List<PlayerVO> playerList, PlayerVO destPlayer, PlayerVO thirdDestPlayer, SkillDictionaryVO skillDictionaryVO, SkillDictionaryVO skillDefenseCard, SkillDictionaryVO skillTrapCard) {
        Room room = player.getRoom();
        for(PlayerVO p : playerList) {
            PrivateRoom pRoom = new PrivateRoom();
            pRoom.setRoom(room);
            pRoom.setDestPlayerName(destPlayer != null?destPlayer.getUserName():"");
            pRoom.setSrcPlayerName(player.getUserName());
            pRoom.setThirdDestPlayerName(thirdDestPlayer != null?thirdDestPlayer.getUserName():"");
            // 如果不是本人，则陷阱和防御技能只提示类别不显示具体名称
            if(skillDictionaryVO != null) {
                String skillDictionaryName = skillDictionaryVO.getSkillNameZh();
                String skillDescription = skillDictionaryVO.getDescription();
                String skillConstrains = skillDictionaryVO.getConstrains();
                if(!p.getId().equals(player.getId()) && SkillTypeEnum.DEFENSE.getCode().equals(skillDictionaryVO.getType())) {
                    skillDictionaryName = "某"+SkillTypeEnum.DEFENSE.getSkillTypeNameZh();
                    skillDescription = "";
                    skillConstrains = "";
                } else if(!p.getId().equals(player.getId()) && SkillTypeEnum.TRAP.getCode().equals(skillDictionaryVO.getType())) {
                    skillDictionaryName = "某"+SkillTypeEnum.TRAP.getSkillTypeNameZh();
                    skillDescription = "";
                    skillConstrains = "";
                }
                pRoom.setSkillDictionaryName(skillDictionaryName);
                pRoom.setSkillDescription(skillDescription);
                pRoom.setSkillConstrains(skillConstrains);
            }

            // 私有房间信息（手牌）
            List<String> handPokers= new ArrayList<>();
            if(p.getHandPokers() !=null && p.getHandPokers().length > 0) {
                for(Card card: p.getHandPokers()) {
                    handPokers.add(card.toString());
                }
            }

            pRoom.setHandPokers(handPokers);
            // 私有房间信息（技能卡）
            pRoom.setPlayerSkillCards(p.getPlayerSkillCards());
            pRoom.setPower(p.getPower());
            pRoom.setPlayerSkillDefenseCount(p.getPlayerSkillDefenseCount());
            pRoom.setSkillUsed(p.isSkillUsed());
            pRoom.setNextRoundSkillAction(p.getNextRoundSkillAction());
            pRoom.setThisRoundSkillAction(p.getThisRoundSkillAction());
            pRoom.setCommunityCards(room.getCommunityCards());
            pRoom.setSkillDefenseCard(skillDefenseCard);
            pRoom.setSkillTrapCard(skillTrapCard);
            RetMsg retMsg = new RetMsg();
            retMsg.setAction(action);
            retMsg.setState(1);
            retMsg.setMessage(JsonUtils.toJson(pRoom, PrivateRoom.class));
            TexasUtil.sendMsgToOne(p, JsonUtils.toJson(retMsg, RetMsg.class));
        }
    }

    private static void removeSkillCard(PlayerVO playerVO, String skillDictionaryNo) {
        if(playerVO.getPlayerSkillCards() == null || playerVO.getPlayerSkillCards().size() == 0) {
            return;
        }
        for(int i = 0; i< playerVO.getPlayerSkillCards().size(); i++) {
            SkillDictionaryVO skillDictionaryVO = playerVO.getPlayerSkillCards().get(i);
            if(skillDictionaryNo.equals(skillDictionaryVO.getSkillDictionaryNo())) {
                playerVO.getPlayerSkillCards().remove(i);
                playerVO.setPlayerSkillCardsCount(playerVO.getPlayerSkillCardsCount() - 1);
                return;
            }
        }
    }

    /**
     * 陷阱技能置入
     * @param skillDictionaryVO
     * @param srcPlayer
     * @return
     */
    public static SkillResultEnum doSkillTrapCards(SkillDictionaryVO skillDictionaryVO, PlayerVO srcPlayer, Room room) {
        List<SkillDictionaryVO> skillTrapList = room.getSkillTrapCards();
        if(skillTrapList == null) {
            skillTrapList = new CopyOnWriteArrayList<>();
        }
        skillDictionaryVO.setUsedPlayer(srcPlayer);
        skillTrapList.add(skillDictionaryVO);
        room.setSkillTrapCards(skillTrapList);
        return SkillResultEnum.DONE;
    }

    /**
     * 防御技能置入
     * @param skillDictionaryVO
     * @param srcPlayer
     * @return
     */
    public static SkillResultEnum doSkillDefenseCards(SkillDictionaryVO skillDictionaryVO, PlayerVO srcPlayer) {
        List<SkillDictionaryVO> skillDictionaryList = srcPlayer.getSkillDefenseCards();
        if(skillDictionaryList == null) {
            skillDictionaryList = new CopyOnWriteArrayList<>();
        }

        // 如果此防御技能为 "敏捷"，则躲避陷阱次数+1，玩家防御卡数量=躲避次数+防御卡列表size
        if("SDN_DEFENSE_4".equals(skillDictionaryVO.getSkillDictionaryNo())) {
            srcPlayer.setAvoidTrapCount(srcPlayer.getAvoidTrapCount() +1);
        } else {
            skillDictionaryList.add(skillDictionaryVO);
            srcPlayer.setSkillDefenseCards(skillDictionaryList);
        }
        srcPlayer.setPlayerSkillDefenseCount(skillDictionaryList.size()+srcPlayer.getAvoidTrapCount());

        return SkillResultEnum.DONE;
    }

    /**
     * 冻结
     * power: 2
     * use_round: PFT
     * type: 1
     * point_to: 1
     * level: N
     * description: 指定玩家下回合无法使用技能
     * constrains: 无
     */
    public static SkillResultEnum SDN_ACTIVE_1(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        destPlayer.setStopUseSkillCardRound(1);
        return SkillResultEnum.DONE;
    }

    /**
     * 全场静默
     * power: 4
     * use_round: PFT
     * type: 1
     * point_to: 0
     * level: SR
     * description: 所有玩家下回合无法使用技能
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_2(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        for(PlayerVO p: room.getIngamePlayers()) {
            p.setStopUseSkillCardRound(1);
        }
        return SkillResultEnum.DONE;
    }

    /**
     * 穿甲弹
     * power: 7
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: SSR
     * description: 指定一个玩家消除身上所有防御
     * constrains: 指向性抵抗法术可以防御

     */
    public static SkillResultEnum SDN_ACTIVE_3(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        destPlayer.setSkillDefenseCards(null);
        destPlayer.setPlayerSkillDefenseCount(0);
        destPlayer.setAvoidTrapCount(0);
        return SkillResultEnum.DONE;
    }

    /**
     * 跟随
     * power: 6
     * use_round: PFT
     * type: 1
     * point_to: 1
     * level: SR
     * description: 指定玩家下次操作为跟注或过牌
     * constrains: 只限本回合未弃牌玩家

     */
    public static SkillResultEnum SDN_ACTIVE_4(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        if(!destPlayer.isFold() && destPlayer.getBodyChips() > 0) {
            destPlayer.setNextRoundSkillAction("checkOrCall");
        }
        return SkillResultEnum.DONE;
    }

    /**
     * 孤注一掷
     * power: 9
     * use_round: PF
     * type: 1
     * point_to: 0
     * level: UR
     * description: 全部玩家All In，然后触发死亡沉默
     * constrains: 只能翻牌和翻牌前使用

     */
    public static SkillResultEnum SDN_ACTIVE_5(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        for(PlayerVO p: room.getIngamePlayers()) {
            if(!p.isFold()) {
                p.setNextRoundSkillAction("allin");
                p.setStopUseSkillCardRound(4);
                p.setSkillUsed(false);
            }
        }
        return SkillResultEnum.DONE;

    }

    /**
     * 更多选择
     * power: 4
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: R
     * description: 从牌库抽2张技能卡
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_6(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        if(!srcPlayer.isFold()) {
            assignSkillCardByPlayer(room, srcPlayer, 2);
        }
        return SkillResultEnum.DONE;

    }

    /**
     * 背刺
     * power: 10
     * use_round: PFTR
     * type: 1
     * point_to: 1
     * level: SSR
     * description: 指定一个玩家重发其手牌
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_7(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        TexasUtil.assignHandPokerByPlayer(room, destPlayer);
        return SkillResultEnum.DONE;
    }

    /**
     * 一呼百应
     * power: 7
     * use_round: PFT
     * type: 1
     * point_to: 0
     * level: SSR
     * description: 本回合未弃牌玩家全部跟注或过牌
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_8(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        for(PlayerVO player: room.getIngamePlayers()) {
            if(player.isFold()) {
                continue;
            }
            player.setNextRoundSkillAction("checkOrCall");
        }

        return SkillResultEnum.DONE;
    }

    /**
     * 死亡沉默
     * power: 7
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: SSR
     * description: 所有玩家无法使用技能直到本局游戏结束，本回合生效
     * constrains: 无
     */
    public static SkillResultEnum SDN_ACTIVE_9(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        for(PlayerVO p: room.getIngamePlayers()) {
            p.setStopUseSkillCardRound(4);
            p.setSkillUsed(false);
        }
        return SkillResultEnum.DONE;
    }

    /**
     * 狂妄
     * power: 5
     * use_round: PFT
     * type: 1
     * point_to: 1
     * level: SR
     * description: 指定玩家下一次操作为All In
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_10(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        if(!destPlayer.isFold() && destPlayer.getBodyChips() > 0) {
            destPlayer.setNextRoundSkillAction("allin");
        }
        return SkillResultEnum.DONE;

    }

    /**
     * 倒转乾坤
     * power: 13
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: UR
     * description: 重发任意一条街公共牌
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_11(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        Integer reverseRound = useSkill.getReverseRound();
        if(reverseRound == null) {
            return SkillResultEnum.NONE;
        }
        int currentRound = 0;
        if("F".equals(room.getCurrentRound())) {
            currentRound = 1;
        }
        if("T".equals(room.getCurrentRound())) {
            currentRound = 2;
        }
        if("R".equals(room.getCurrentRound())) {
            currentRound = 3;
        }
        // 重发的公共牌回合必须小于等于当前回合，例如：翻牌圈F 不能选择重发河牌R的公共牌
        if(reverseRound > currentRound) {
            return SkillResultEnum.NONE;
        }
        List<Card> cardList = room.getCardList();
        if(reverseRound == 1) { // 翻牌
            for (int i = 0; i < 3; i++) {
                room.getCommunityCards().set(i,cardList.get(0).toString());
                room.getCalcCommunityCards().set(i,cardList.get(0));
                cardList.remove(0);
            }
        }
        if(reverseRound == 2) { // 转牌
            room.getCommunityCards().set(3,cardList.get(0).toString());
            room.getCalcCommunityCards().set(3,cardList.get(0));
            cardList.remove(0);
        }
        if(reverseRound == 3) { // 河牌
            room.getCommunityCards().set(4,cardList.get(0).toString());
            room.getCalcCommunityCards().set(4,cardList.get(0));
            cardList.remove(0);
        }

        return SkillResultEnum.DONE;
    }

    /**
     * 解脱
     * power: 1
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: R
     * description: 随机放弃手里2张技能卡获得4个能量
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_12(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        if(!srcPlayer.isFold()) {
            abandonSkillCard(room, srcPlayer, skillDictionaryVO, 2, 0, true);
            srcPlayer.setPower(srcPlayer.getPower() +4);
        }
        return SkillResultEnum.DONE;
    }

    /**
     * 减负
     * power: 0
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: R
     * description: 随机放弃手里1张技能卡获得2个能量
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_13(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        if(!srcPlayer.isFold()) {
            abandonSkillCard(room, srcPlayer, skillDictionaryVO, 1, 0, true);
            srcPlayer.setPower(srcPlayer.getPower() + 2);
        }
        return SkillResultEnum.DONE;
    }

    /**
     * 恩赐
     * power: 0
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: N
     * description: 获得1个能量
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_14(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        srcPlayer.setPower(srcPlayer.getPower() +1);
        return SkillResultEnum.DONE;
    }

    /**
     * 吸收
     * power: 1
     * use_round: PFTR
     * type: 1
     * point_to: 1
     * level: SR
     * description: 吸收指定玩家2个能量
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_15(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        if(!srcPlayer.isFold() && !destPlayer.isFold()) {
            srcPlayer.setPower(srcPlayer.getPower() + 2);
            if(destPlayer.getPower() > 2) {
                destPlayer.setPower(destPlayer.getPower() -2);
            } else {
                destPlayer.setPower(0);
            }
        }

        return SkillResultEnum.DONE;
    }

    /**
     * 高级吸收
     * power: 2
     * use_round: PFTR
     * type: 1
     * point_to: 1
     * level: SSR
     * description: 吸收指定玩家3个能量
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_16(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        if(!srcPlayer.isFold() && !destPlayer.isFold()) {
            srcPlayer.setPower(srcPlayer.getPower() + 3);
            if (destPlayer.getPower() > 3) {
                destPlayer.setPower(destPlayer.getPower() - 3);
            } else {
                destPlayer.setPower(0);
            }
        }
        return SkillResultEnum.DONE;
    }

    /**
     * 损人利己
     * power: 6
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: SR
     * description: 除自己外全场玩家随机弃掉手中2个技能卡
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_17(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        for(PlayerVO player: room.getIngamePlayers()) {
            if(player.isFold()) {
                continue;
            }
            if(player.getId().equals(srcPlayer.getId())) {
                continue;
            }
            abandonSkillCard(room, player, skillDictionaryVO, 2, 0, false);
        }

        return SkillResultEnum.DONE;
    }

    /**
     * 流放
     * power: 0
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: N
     * description: 放弃一张技能卡再重新抽一张
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_18(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        abandonSkillCard(room, srcPlayer, skillDictionaryVO, 1, 1, true);
        return SkillResultEnum.DONE;
    }

    /**
     * 幸运
     * power: 3
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: SSR
     * description: 抽一张技能卡并且所有技能卡减少1个能量消耗
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_19(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        assignSkillCardByPlayer(room, srcPlayer, 1);
        List<SkillDictionaryVO> playerSkillCards = srcPlayer.getPlayerSkillCards();
        for(SkillDictionaryVO skillDictionaryTmp: playerSkillCards) {
            // 消耗为0的和当前使用的卡不进行能量减少
            if(skillDictionaryTmp.getPower() > 0 && !skillDictionaryTmp.getSkillDictionaryNo().equals(skillDictionaryVO.getSkillDictionaryNo())) {
                skillDictionaryTmp.setPower(skillDictionaryTmp.getPower() -1);
            }
        }
        return SkillResultEnum.DONE;
    }

    /**
     * 宁有种乎
     * power: 9
     * use_round: P
     * type: 1
     * point_to: 0
     * level: UR
     * description: 全部玩家重新发手牌，然后触发死亡沉默
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_20(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        for(PlayerVO player: room.getIngamePlayers()) {
            if(player.isFold()) {
                continue;
            }
            TexasUtil.assignHandPokerByPlayer(room, player);
            player.setStopUseSkillCardRound(4);
            player.setSkillUsed(false);
        }
        return SkillResultEnum.DONE;
    }

    /**
     * 放弃
     * power: 12
     * use_round: PFT
     * type: 1
     * point_to: 1
     * level: SSR
     * description: 指定玩家下回合弃牌
     * constrains: 场上玩家数量少于3位或选择已经Allin玩家时技能失效

     */
    public static SkillResultEnum SDN_ACTIVE_21(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        if(destPlayer.getBodyChips() == 0 || room.getIngamePlayers().size() < 3) {
            return SkillResultEnum.DONE;
        }
        destPlayer.setNextRoundSkillAction("fold");
        return SkillResultEnum.DONE;
    }


    /**
     * 卸甲归田
     * power: 10
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: SSR
     * description: 取消场上所有防御
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_22(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        for(PlayerVO player: room.getIngamePlayers()) {
            player.setSkillDefenseCards(null);
            player.setPlayerSkillDefenseCount(0);
            player.setAvoidTrapCount(0);
        }
        return SkillResultEnum.DONE;
    }

    /**
     * 正气凛然
     * power: 12
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: UR
     * description: 取消场上所有陷阱和防御
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_23(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        for(PlayerVO player: room.getIngamePlayers()) {
            player.setSkillDefenseCards(null);
            player.setAvoidTrapCount(0);
            player.setPlayerSkillDefenseCount(0);
        }
        room.setSkillTrapCards(null);
        return SkillResultEnum.DONE;
    }

    /**
     * 群体逆转
     * power: 8
     * use_round: PFT
     * type: 1
     * point_to: 0
     * level: SSR
     * description: 全部玩家重新更换一张手牌
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_24(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        for(PlayerVO player: room.getIngamePlayers()) {
            if(player.isFold()) {
                continue;
            }
            TexasUtil.assignHandPokerByPlayerOneCard(room, player);
        }
        return SkillResultEnum.DONE;
    }

    /**
     * 单向逆转
     * power: 6
     * use_round: PFTR
     * type: 1
     * point_to: 1
     * level: SR
     * description: 指定玩家随机更换一张手牌
     * constrains: 可以对自己使用

     */
    public static SkillResultEnum SDN_ACTIVE_25(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        if(!destPlayer.isFold()) {
            TexasUtil.assignHandPokerByPlayerOneCard(room, destPlayer);
        }
        return SkillResultEnum.DONE;
    }

    /**
     * 照明弹
     * power: 10
     * use_round: PFTR
     * type: 1
     * point_to: 0
     * level: SSR
     * description: 取消场上所有陷阱
     * constrains: 无

     */
    public static SkillResultEnum SDN_ACTIVE_26(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        room.setSkillTrapCards(null);
        return SkillResultEnum.DONE;
    }

    /**
     * 无懈可击
     * power: 4
     * use_round: PFTR
     * type: 2
     * point_to: 0
     * level: SSR
     * description: 抵抗一次指向性技能攻击
     * constrains: 无

     */
    public static SkillResultEnum SDN_DEFENSE_1(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        int length = destPlayer.getSkillDefenseCards() != null?destPlayer.getSkillDefenseCards().size():0;
        for(int i = 0; i< length; i++) {
            SkillDictionaryVO SkillDefense = destPlayer.getSkillDefenseCards().get(i);
            if(skillDictionaryVO.getSkillDictionaryNo().equals(SkillDefense.getSkillDictionaryNo())) {
                destPlayer.getSkillDefenseCards().remove(i);
//                destPlayer.setPlayerSkillDefenseCount(destPlayer.getPlayerSkillDefenseCount() -1);
                return SkillResultEnum.DONE;
            }
        }
        return SkillResultEnum.NONE;
    }

    /**
     * 寻找替身
     * power: 5
     * use_round: PFTR
     * type: 2
     * point_to: 0
     * level: SR
     * description: 由其他玩家替代法术攻击
     * constrains: 如果场内无其他第三方玩家则本技能失效
     */
    public static SkillResultEnum SDN_DEFENSE_2(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        // 穿甲弹直接跳过
        if("SDN_ACTIVE_3".equals(useSkill.getSkillDictionaryNo())) {
            return SkillResultEnum.DONE;
        }
        int length = destPlayer.getSkillDefenseCards() != null?destPlayer.getSkillDefenseCards().size():0;
        for(int i = 0; i< length; i++) {
            SkillDictionaryVO SkillDefense = destPlayer.getSkillDefenseCards().get(i);
            if(skillDictionaryVO.getSkillDictionaryNo().equals(SkillDefense.getSkillDictionaryNo())) {
                destPlayer.getSkillDefenseCards().remove(i);
//                destPlayer.setPlayerSkillDefenseCount(destPlayer.getPlayerSkillDefenseCount() -1);
                if(room.getIngamePlayers().size() >= 3) {
                    List<PlayerVO> skillPlayers = new CopyOnWriteArrayList<>();
                    for(PlayerVO player: room.getIngamePlayers()) {
                        if(!player.getId().equals(srcPlayer.getId()) && !player.getId().equals(destPlayer.getId())) {
                            skillPlayers.add(player);
                        }
                    }
                    Random random = new Random();
                    int nextInt = random.nextInt(skillPlayers.size());
                    System.out.println("nextInt:"+nextInt+"  skillPlayers.size"+skillPlayers.size());
                    thirdDestPlayer = skillPlayers.get(nextInt);
                }
                return SkillResultEnum.DONE;
            }
        }
        return SkillResultEnum.NONE;
    }

    /**
     * 铜墙铁壁
     * power: 5
     * use_round: PFTR
     * type: 2
     * point_to: 0
     * level: SSR
     * description: 本局游戏不受任何指向性技能攻击，持续到结束
     * constrains: 无

     */
    public static SkillResultEnum SDN_DEFENSE_3(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        // 上面的逻辑判断一下真的有这张牌就可以了，不用消耗和数量扣减
        // 本方法啥都不用做直接返回true
        return SkillResultEnum.DONE;
    }

    /**
     * 敏捷
     * power: 3
     * use_round: PFTR
     * type: 2
     * point_to: 0
     * level: SSR
     * description: 躲避一次陷阱
     * constrains: 无

     */
    public static SkillResultEnum SDN_DEFENSE_4(SkillDictionaryVO skillDictionaryVO, Room room, UseSkill useSkill, PlayerVO srcPlayer, PlayerVO destPlayer) {
        // 啥也不用做
        return SkillResultEnum.DONE;
    }

    /**
     * 反抗失败
     * power: 3
     * use_round: PFT
     * type: 3
     * point_to: 0
     * level: SR
     * description: 加注无效
     * constrains: 改为跟注或过牌,自己除外

     */
    public static SkillResultEnum SDN_TRAP_1(SkillDictionaryVO srcSkill, SkillDictionaryVO trapSkillDictionary, Room room, PlayerVO srcPlayer, PlayerVO trapPlayer, String message) {
        // 如果玩家使用的主动技能优先级高则直接跳过 （照明弹、正气凛然等取消陷阱的技能）
        if(srcSkill != null && srcSkill.getPriority().intValue() > trapSkillDictionary.getPriority().intValue()) {
            return SkillResultEnum.DONE;
        }
        if(srcPlayer.getId().equals(trapPlayer.getId())) {
            return SkillResultEnum.NONE;
        }

        // 判断玩家是否有加注行为
        if(!raiseVerify(room, srcPlayer, message)) {
            return SkillResultEnum.NONE;
        }
        if(avoidTrapCheck(room, trapSkillDictionary, srcPlayer)) {
            return SkillResultEnum.CONTINUE;
        }

        return SkillResultEnum.CHECKORCALL;
    }

    /**
     * 背叛
     * power: 9
     * use_round: PFT
     * type: 3
     * point_to: 0
     * level: UR
     * description: 加注者弃牌
     * constrains: 改为跟注或过牌,自己除外

     */
    public static SkillResultEnum SDN_TRAP_2(SkillDictionaryVO srcSkill, SkillDictionaryVO trapSkillDictionary, Room room, PlayerVO srcPlayer, PlayerVO trapPlayer, String message) {
        // 如果玩家使用的主动技能优先级高则直接跳过 （照明弹、正气凛然等取消陷阱的技能）
        if(srcSkill != null && srcSkill.getPriority().intValue() > trapSkillDictionary.getPriority().intValue()) {
            return SkillResultEnum.DONE;
        }
        if(srcPlayer.getId().equals(trapPlayer.getId())) {
            return SkillResultEnum.NONE;
        }
        // 判断玩家是否有加注行为
        if(!raiseVerify(room, srcPlayer, message)) {
            return SkillResultEnum.NONE;
        }
        if(avoidTrapCheck(room, trapSkillDictionary, srcPlayer)) {
            return SkillResultEnum.CONTINUE;
        }

        return SkillResultEnum.FOLD;
    }

    /**
     * 负担加重
     * power: 1
     * use_round: PFTR
     * type: 3
     * point_to: 0
     * level: R
     * description: 下一个使用技能的玩家释放技能后额外消耗2个能量
     * constrains: 额外消耗能量不够扣减则对方能量剩余为0,自己除外

     */
    public static SkillResultEnum SDN_TRAP_3(SkillDictionaryVO srcSkill, SkillDictionaryVO trapSkillDictionary, Room room, PlayerVO srcPlayer, PlayerVO trapPlayer, String message) {
        // 如果玩家使用的主动技能优先级高则直接跳过 （照明弹、正气凛然等取消陷阱的技能）
        if(srcSkill != null && srcSkill.getPriority().intValue() > trapSkillDictionary.getPriority().intValue()) {
            return SkillResultEnum.DONE;
        }
        if(srcPlayer.getId().equals(trapPlayer.getId())) {
            return SkillResultEnum.NONE;
        }
        if(avoidTrapCheck(room, trapSkillDictionary, srcPlayer)) {
            return SkillResultEnum.CONTINUE;
        }
        srcPlayer.setExtPower(2);
        return SkillResultEnum.DONE;
    }

    /**
     * 措手不及
     * power: 4
     * use_round: PFT
     * type: 3
     * point_to: 0
     * level: SSR
     * description: 加注者重发手牌
     * constrains: 自己也会触发

     */
    public static SkillResultEnum SDN_TRAP_4(SkillDictionaryVO srcSkill, SkillDictionaryVO trapSkillDictionary, Room room, PlayerVO srcPlayer, PlayerVO trapPlayer, String message) {
        // 如果玩家使用的主动技能优先级高则直接跳过 （照明弹、正气凛然等取消陷阱的技能）
        if(srcSkill != null && srcSkill.getPriority().intValue() > trapSkillDictionary.getPriority().intValue()) {
            return SkillResultEnum.DONE;
        }
        // 判断玩家是否有加注行为
        if(!raiseVerify(room, srcPlayer, message)) {
            return SkillResultEnum.NONE;
        }
        if(avoidTrapCheck(room, trapSkillDictionary, srcPlayer)) {
            return SkillResultEnum.CONTINUE;
        }
        TexasUtil.assignHandPokerByPlayer(room, srcPlayer);
        return SkillResultEnum.DONE;
    }

    /**
     * 复制
     * power: 2
     * use_round: PFT
     * type: 3
     * point_to: 0
     * level: SR
     * description: 复制加注者1张技能卡并获得1个能量
     * constrains: 自己除外
     */
    public static SkillResultEnum SDN_TRAP_5(SkillDictionaryVO srcSkill, SkillDictionaryVO trapSkillDictionary, Room room, PlayerVO srcPlayer, PlayerVO trapPlayer, String message) {
        // 如果玩家使用的主动技能优先级高则直接跳过 （照明弹、正气凛然等取消陷阱的技能）
        if(srcSkill != null && srcSkill.getPriority().intValue() > trapSkillDictionary.getPriority().intValue()) {
            return SkillResultEnum.DONE;
        }
        if(srcPlayer.getId().equals(trapPlayer.getId())) {
            return SkillResultEnum.NONE;
        }
        // 判断玩家是否有加注行为
        if(!raiseVerify(room, srcPlayer, message)) {
            return SkillResultEnum.NONE;
        }
        if(avoidTrapCheck(room, trapSkillDictionary, srcPlayer)) {
            return SkillResultEnum.CONTINUE;
        }
        // 获得一个能量
        trapPlayer.setPower(trapPlayer.getPower()+1);

        // 复制对方随机一张技能卡
        if(srcPlayer.getPlayerSkillCards() != null && srcPlayer.getPlayerSkillCards().size() > 0) {
            List<SkillDictionaryVO> srcPlayerSkillCards = new CopyOnWriteArrayList<SkillDictionaryVO>
                (Arrays.asList(new SkillDictionaryVO[srcPlayer.getPlayerSkillCards().size()]));
            Collections.copy(srcPlayerSkillCards, srcPlayer.getPlayerSkillCards());
            Random random = new Random();
            if(srcPlayerSkillCards != null && srcPlayerSkillCards.size() > 0) {
                SkillDictionaryVO skillDictionary = srcPlayerSkillCards.get(random.nextInt(srcPlayerSkillCards.size()));
                List<SkillDictionaryVO> trapPlayerSkillCards = trapPlayer.getPlayerSkillCards();
                if(trapPlayerSkillCards == null) {
                    trapPlayerSkillCards = new CopyOnWriteArrayList<SkillDictionaryVO>();
                }
                trapPlayerSkillCards.add(skillDictionary);
                trapPlayer.setPlayerSkillCards(trapPlayerSkillCards);
            }
        }
        return SkillResultEnum.CONTINUE;
    }



    /**
     * 迟缓
     * power: 2
     * use_round: PFT
     * type: 3
     * point_to: 0
     * level: SR
     * description: 下一个使用技能的玩家下回合不发放技能卡
     * constrains: 自己除外
     */
    public static SkillResultEnum SDN_TRAP_6(SkillDictionaryVO srcSkill, SkillDictionaryVO trapSkillDictionary, Room room, PlayerVO srcPlayer, PlayerVO trapPlayer, String message) {
        // 如果玩家使用的主动技能优先级高则直接跳过 （照明弹、正气凛然等取消陷阱的技能）
        if(srcSkill != null && srcSkill.getPriority().intValue() > trapSkillDictionary.getPriority().intValue()) {
            return SkillResultEnum.DONE;
        }
        if(srcPlayer.getId().equals(trapPlayer.getId())) {
            return SkillResultEnum.NONE;
        }
        if(avoidTrapCheck(room, trapSkillDictionary, srcPlayer)) {
            return SkillResultEnum.CONTINUE;
        }
        srcPlayer.setStopGetSkillCardRound(srcPlayer.getStopGetSkillCardRound() +1);
        return SkillResultEnum.DONE;
    }

    /**
     * 高级迟缓
     * power: 5
     * use_round: PFT
     * type: 3
     * point_to: 0
     * level: SSR
     * description: 下一个使用技能的玩家本局不发放技能卡
     * constrains: 自己除外

     */
    public static SkillResultEnum SDN_TRAP_7(SkillDictionaryVO srcSkill, SkillDictionaryVO trapSkillDictionary, Room room, PlayerVO srcPlayer, PlayerVO trapPlayer, String message) {
        // 如果玩家使用的主动技能优先级高则直接跳过 （照明弹、正气凛然等取消陷阱的技能）
        if(srcSkill != null && srcSkill.getPriority().intValue() > trapSkillDictionary.getPriority().intValue()) {
            return SkillResultEnum.DONE;
        }
        if(srcPlayer.getId().equals(trapPlayer.getId())) {
            return SkillResultEnum.NONE;
        }
        if(avoidTrapCheck(room, trapSkillDictionary, srcPlayer)) {
            return SkillResultEnum.CONTINUE;
        }
        srcPlayer.setStopGetSkillCardRound(4);
        return SkillResultEnum.DONE;
    }

    /**
     * 自信
     * power: 4
     * use_round: PFT
     * type: 3
     * point_to: 0
     * level: SR
     * description: 下一个弃牌玩家改为过牌或跟注
     * constrains: 自己除外

     */
    public static SkillResultEnum SDN_TRAP_8(SkillDictionaryVO srcSkill, SkillDictionaryVO trapSkillDictionary, Room room, PlayerVO srcPlayer, PlayerVO trapPlayer, String message) {
        // 如果玩家使用的主动技能优先级高则直接跳过 （照明弹、正气凛然等取消陷阱的技能）
        if(srcSkill != null && srcSkill.getPriority().intValue() > trapSkillDictionary.getPriority().intValue()) {
            return SkillResultEnum.DONE;
        }
        if(srcPlayer.getId().equals(trapPlayer.getId())) {
            return SkillResultEnum.NONE;
        }
        if(avoidTrapCheck(room, trapSkillDictionary, srcPlayer)) {
            return SkillResultEnum.CONTINUE;
        }

        return SkillResultEnum.CHECKORCALL;
    }

    /**
     * 失算
     * power: 1
     * use_round: PFTR
     * type: 3
     * point_to: 0
     * level: R
     * description: 下一个过牌玩家失去3个能量
     * constrains: 自己除外
     */
    public static SkillResultEnum SDN_TRAP_9(SkillDictionaryVO srcSkill, SkillDictionaryVO trapSkillDictionary, Room room, PlayerVO srcPlayer, PlayerVO trapPlayer, String message) {
        // 如果玩家使用的主动技能优先级高则直接跳过 （照明弹、正气凛然等取消陷阱的技能）
        if(srcSkill != null && srcSkill.getPriority().intValue() > trapSkillDictionary.getPriority().intValue()) {
            return SkillResultEnum.DONE;
        }
        if(srcPlayer.getId().equals(trapPlayer.getId())) {
            return SkillResultEnum.NONE;
        }

        if(avoidTrapCheck(room, trapSkillDictionary, srcPlayer)) {
            return SkillResultEnum.CONTINUE;
        }
        srcPlayer.setPower(srcPlayer.getPower() >= 3 ? srcPlayer.getPower() - 3: 0);
        return SkillResultEnum.DONE;
    }

    /**
     * 判断玩家是否触发陷阱闪避类防御（敏捷技能）
     * 原因：单独进行前端提示，否则数据模型过于复杂，后期前后端接口需要重新梳理
     * @param room
     * @param trapSkillCard
     * @param srcPlayer
     * @return
     */
    private static boolean avoidTrapCheck(Room room, SkillDictionaryVO trapSkillCard, PlayerVO srcPlayer) {
        if(trapSkillCard == null || trapSkillCard.getUsedPlayer() == null) {
            return false;
        }

        // 对自己使用时，躲避不生效
        if(srcPlayer.getAvoidTrapCount() > 0 && !srcPlayer.getId().equals(trapSkillCard.getUsedPlayer().getId()) ) {
            srcPlayer.setAvoidTrapCount(srcPlayer.getAvoidTrapCount() -1);
            srcPlayer.setPlayerSkillDefenseCount(srcPlayer.getSkillDefenseCards()!=null?srcPlayer.getSkillDefenseCards().size():0+srcPlayer.getAvoidTrapCount());
            SkillDictionaryVO skillDefenseCard = new SkillDictionaryVO();
            skillDefenseCard.setSkillDictionaryNo("SDN_DEFENSE_4");
            skillDefenseCard.setSkillNameZh("敏捷");
            skillDefenseCard.setDescription("躲避一次陷阱");
            skillDefenseCard.setConstrains("");
            // 通知所有玩家
            sendSkillMessage("onAvoidTrap", srcPlayer, room.getIngamePlayers(), null, null, null, skillDefenseCard, trapSkillCard);
            sendSkillMessage("onAvoidTrap", srcPlayer, room.getWaitPlayers(), null, null, null, skillDefenseCard, trapSkillCard);
            return true;
        }
        return false;
    }

    /**
     * 判断玩家应该完成check操作还是call操作
     * @param room
     * @param player
     * @return 返回chip值； 0：check；chip >0 :call
     */
    private static int getCheckOrCall(Room room, PlayerVO player) {
        // 玩家此次操作之前的本轮下注额
        long oldBetThisRound = 0;
        if (room.getBetRoundMap().get(player.getSeatNum()) != null) {
            oldBetThisRound = room.getBetRoundMap().get(player.getSeatNum());
        }

        return (int) (room.getRoundMaxBet()-oldBetThisRound);
    }

    /**
     * 随机放弃 removeNum 个技能卡并获得 getNum 个新技能卡
     * @param room
     * @param player
     * @param skillDictionaryVO 当前执行的卡牌
     * @param removeNum
     * @param getNum
     * @param isSrc 是否操作的是本人卡组，用于判断移除时是否保留本技能卡
     * （场景：A对B使用移除，需要判断B如果也有此卡时要保留；反之如果A对自己使用则移除时需要跳过此卡）
     */
    public static void abandonSkillCard(Room room, PlayerVO player, SkillDictionaryVO skillDictionaryVO, int removeNum, int getNum, boolean isSrc) {
        List<SkillDictionaryVO> playerSkillCards = null; // 玩家手持技能卡
        if(player == null || player.getPlayerSkillCards() == null) {
            return;
        }
        playerSkillCards = player.getPlayerSkillCards();
        // 移除数量如果 >= 当前技能数量(消耗的技能还未移除牌组)-1,则把当前手中卡牌全部移除
        if(removeNum >= playerSkillCards.size() -1) {
            playerSkillCards.clear();
        } else {
            for(int i = removeNum ; i > 0; i--) {
                Random random = new Random();
                int nextInt = random.nextInt(i+1);

                // 如果移除对象正好是当前使用的技能则继续，本次移除失效需要重新移
                if(isSrc && skillDictionaryVO.getSkillDictionaryNo().equals(playerSkillCards.get(i).getSkillDictionaryNo())) {
                    i++;
                    continue;
                }
                playerSkillCards.remove(nextInt);
            }
        }
        player.setPlayerSkillCardsCount(playerSkillCards.size());
        assignSkillCardByPlayer(room, player, getNum);
    }

    /**
     * 判断是否会触发陷阱
     * @param skillDictionary 不为空表示当前玩家使用了技能，此参数为使用的技能卡
     * @param player
     * @param type
     * @param message
     * @return
     */
    public SkillResultEnum trapSkillVerify(SkillDictionaryVO skillDictionary, PlayerVO player, String type, String message) {
        Room room = player.getRoom();
        if(room == null) {
            return SkillResultEnum.NONE;
        }
        Class clazz = SkillCardsUtil.class;
        try {
            if(room.getSkillTrapCards() == null || room.getSkillTrapCards().size() == 0) {
                return SkillResultEnum.CONTINUE;
            }
            for(int i = 0; i < room.getSkillTrapCards().size(); i++) {
                SkillDictionaryVO skillTrap = room.getSkillTrapCards().get(i);
                SkillResultEnum trapResult;
                // 达到触发条件，进入具体业务逻辑进行判断处理
                if(skillTrap.getTrapAction().indexOf(type) != -1) {
                    Method method = clazz.getMethod(skillTrap.getSkillDictionaryNo(), SkillDictionaryVO.class, SkillDictionaryVO.class, Room.class, PlayerVO.class, PlayerVO.class, String.class);
                    trapResult = (SkillResultEnum) method.invoke(null, new Object[]{skillDictionary, skillTrap, room, player, skillTrap.getUsedPlayer(), message});
                    if(SkillResultEnum.DONE.getCode().equals(trapResult.getCode()) || SkillResultEnum.CONTINUE.getCode().equals(trapResult.getCode())
                        || SkillResultEnum.CHECKORCALL.getCode().equals(trapResult.getCode())
                        || SkillResultEnum.FOLD.getCode().equals(trapResult.getCode())
                        || SkillResultEnum.ALLIN.getCode().equals(trapResult.getCode())) {
                        // 触发成功后移除该陷阱
                        room.getSkillTrapCards().remove(i);

                        // 如果使用的主动技能优先级高于陷阱，则陷阱失效（优先级具有覆盖陷阱的效果，陷阱不生效并且需要被移除）
                        if(skillDictionary != null && skillDictionary.getPriority() > skillTrap.getPriority()) {
                            skillTrap = null;
                        }
                        // 通知所有玩家
                        sendSkillMessage("onTrapSkill", player, room.getIngamePlayers(), null, thirdDestPlayer, null, null, skillTrap);
                        sendSkillMessage("onTrapSkill", player, room.getWaitPlayers(), null, thirdDestPlayer, null, null, skillTrap);

                        if(SkillResultEnum.CHECKORCALL.getCode().equals(trapResult.getCode())) {
                            // 判断玩家执行check、还是call
                            int chip = getCheckOrCall(room, player);
                            if(chip == 0) {
                                roomManager.check(player, true);
                            } else {
                                roomManager.betchipIn(player, chip, true);
                            }
                        }
                        if(SkillResultEnum.FOLD.getCode().equals(trapResult.getCode())) {
                            roomManager.fold(player);
                        }
                        if(SkillResultEnum.ALLIN.getCode().equals(trapResult.getCode())) {

                        }
                        return trapResult;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return SkillResultEnum.NONE;
        }

        return SkillResultEnum.CONTINUE;
    }

    /**
     * 判断玩家行为是否为加注
     * @param player
     * @param message
     * @return
     */
    private static boolean raiseVerify(Room room, PlayerVO player, String message) {
        // 玩家此次操作之前的本轮下注额
        long oldBetThisRound = 0;
        if (room.getBetRoundMap().get(player.getSeatNum()) != null) {
            oldBetThisRound = room.getBetRoundMap().get(player.getSeatNum());
        }
        BetPlayer bp = JsonUtils.fromJson(message, BetPlayer.class);
        // 玩家本次操作所下的筹码
        int chip = bp.getInChips();
        // 无效下注额,1筹码不足
        if (chip <= 0 || chip > player.getBodyChips()) {
            logger.error("SkillCardsUtil raiseVerify error not enough chips:" + chip + " getBodyChips():" + player.getBodyChips());
            return false;
        }
        // 判断是否为加注行为
        //2.2加注，反加--
        if ((chip + oldBetThisRound) > room.getRoundMaxBet() && (chip + oldBetThisRound) < room.getRoundMaxBet()*2) {
            // 加注必须大于2倍之前最大下注（增量加注规则暂时不考虑）
            logger.error("SkillCardsUtil SDN_TRAP_1 (totalBet > totalBet && totalBet < 2*RoundMaxBet) :" + chip + "oldBetThisRound:" + oldBetThisRound
                + ",max:" + room.getRoundMaxBet());
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        for(int i = 1 ; i > 0; i--) {
            Random random = new Random();
            int nextInt = random.nextInt(i+1);
            System.out.println(nextInt);
        }

    }
}
