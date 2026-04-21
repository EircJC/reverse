package com.yulink.texas.server.manager;

import com.yulink.texas.common.InitCards;
import com.yulink.texas.common.card.Card;
import com.yulink.texas.core.domain.SkillDictionary;
import com.yulink.texas.server.common.entity.PlayerOpt;
import com.yulink.texas.server.common.entity.PlayerVO;
import com.yulink.texas.server.common.entity.PrivateRoom;
import com.yulink.texas.server.common.entity.RetMsg;
import com.yulink.texas.server.common.entity.SkillDictionaryVO;
import com.yulink.texas.server.common.room.Room;
import com.yulink.texas.server.common.utils.BetPool;
import com.yulink.texas.server.common.utils.CardCalculator;
import com.yulink.texas.server.common.utils.DateUtil;
import com.yulink.texas.server.common.utils.HandPower;
import com.yulink.texas.server.common.utils.JsonUtils;
import com.yulink.texas.server.common.utils.SkillCardsUtil;
import com.yulink.texas.server.common.utils.SpringUtil;
import com.yulink.texas.server.ws.TexasUtil;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/8
 * @Copyright (c) bitmain.com All Rights Reserved
 */
@Component
@Slf4j
public class RoomManager {

    @Autowired
    public TexasUtil texasUtil;

    @Autowired
    public SkillManager skillManager;

    public void inRoom(Channel channel, String message) {
        texasUtil.inRoom(channel, message);
    }

    public void getRoomLevelStats(Channel channel, String message) {
        texasUtil.getRoomLevelStats(channel, message);
    }

    public void getRoomList(Channel channel, String message) {
        texasUtil.getRoomList(channel, message);
    }

    public void inRoomByRoomNo(Channel channel, String message) {
        texasUtil.inRoomByRoomNo(channel, message);
    }

    public void createRoomAndIn(Channel channel, String message) {
        texasUtil.createRoomAndIn(channel, message);
    }

    public void outRoom(Channel channel, String message, boolean sendOrNot) {
        texasUtil.outRoom(channel, message, sendOrNot);
    }

    public void outRoom(Channel channel, String message) {
        texasUtil.outRoom(channel, message, true);
    }

    /**
     * 判断游戏是否可以开始
     *
     */
    public void checkStart(Room room,int milsecond) {
        try {
            Thread.sleep(milsecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 不在等待开始，则返回
        if (!(room.getGamestate().get() == 0)) {
            return;
        }
        // 有效玩家数大于等于最小开始游戏玩家则开始（筹码大于0的玩家为有效玩家）
        int activePlayerCount = 0;
        if (room.getWaitPlayers().size() >= room.getMinPlayers()) {
            for(PlayerVO player:room.getWaitPlayers()) {
                if(player.getBodyChips() > 0) {
                    activePlayerCount++;

                    if(activePlayerCount >= room.getMinPlayers()) {
                        startGame(room);
                        break;
                    }
                }
            }
//			startGame();
        }
    }
    /**
     *
     * 开始游戏
     *
     * <pre>
     * 游戏状态变更为游戏中，等待中的玩家移动到游戏中列表
     *
     * @param
     */
    public void startGame(Room room) {
        if (room.getGamestate().compareAndSet(0, 1)) {
            // 游戏日志-玩家操作信息
            room.opts.clear();

            // 由于需要通知在结束阶段进入的玩家牌局信息
            // 因此延迟到下局开始清除上局信息
            room.getFinalCardsMap().clear();
            room.handCardsMap.clear();
            room.getWinPlayersMap().clear();
            room.setCurrentRound("P"); // 翻牌前
            room.setSkillTrapCount(0);
            room.getSkillCards().clear();
            room.getSkillTrapCards().clear();

            if(room.getType() == 1) {
                List<SkillDictionary> skillDictionaryList = skillManager.getListByRedis();
                for(SkillDictionary skillDictionary: skillDictionaryList) {
                    SkillDictionaryVO skillDictionaryVO = new SkillDictionaryVO();
                    BeanUtils.copyProperties(skillDictionary, skillDictionaryVO);
                    skillDictionaryVO.setCount(-1);
                    room.getSkillCards().add(skillDictionaryVO);
                }
            }

            // 重新补筹码
            for (PlayerVO p : room.getWaitPlayers()) {
                if (p.getBodyChips() == 0) {
                    assignChipsForInRoom(room, p);
                }
            }
            // TODO 筹码不足需要提示前端补足筹码然后上桌
            // 总筹码不足一个大盲注的不能进行游戏，踢出房间
            long nowTime = System.currentTimeMillis();
            room.getWaitPlayers().parallelStream().filter(p -> p.getBodyChips() < room.getBigBet()
                    && p.getExitRoomCountDownStartTime() > 0 && nowTime > p.getExitRoomCountDownStartTime()+p.getExitRoomCountDownInterval())
                .forEach(p -> texasUtil.outRoom(p));

            // 转移等待列表的玩家进入游戏中玩家列表
            TexasUtil.movePlayers(room.getWaitPlayers(), room.getIngamePlayers());
            // 记录玩家座位号
            for (PlayerVO p : room.getIngamePlayers()) {
                p.setFold(false);// 设定为未弃牌
                p.setSkillCards(null);
                p.setPlayerSkillCards(null);
                p.setSkillDefenseCards(null);
                p.setPower(0);
                p.setPlayerSkillDefenseCount(0);
                p.setPlayerSkillCardsCount(0);
                p.setStopUseSkillCardRound(0); // 默认开局都可以使用技能卡
                p.setNextRoundSkillAction("");
                p.setThisRoundSkillAction("");
                p.setAvoidTrapCount(0);
            }
            // 更新下一个dealer
            TexasUtil.updateNextDealer(room);
            // 得到一副洗好的牌（随机卡组）
            room.setCardList(new InitCards().cards);

            // 复制一份洗好的牌组放到完整牌组记录中，用于追溯回查
            room.setCardListWhole(room.getCardList().stream().collect(Collectors.toList()));
            // 确定大小盲主，并分配筹码到奖池
            // 最佳位置
            int dealer = room.getDealer();
            // 小盲注
            int smallBet = room.getSmallBet();
            // 大盲注
            int bigBet = room.getBigBet();
            // 小盲位置
            int smallBetSeat = TexasUtil.getNextSeatNum(dealer, room);
            // 当只有2个玩家时，dealer才是小盲
            if (room.getIngamePlayers().size() == 2) {
                smallBetSeat = dealer;
            }
            room.setSmallBetSeatNum(smallBetSeat);
            // 大盲位置
            int bigBetSeat = TexasUtil.getNextSeatNum(smallBetSeat, room);
            room.setBigBetSeatNum(bigBetSeat);
            // 小盲玩家
            PlayerVO smallBetPlayer = TexasUtil.getPlayerBySeatNum(smallBetSeat, room.getIngamePlayers());
            // 大盲玩家
            PlayerVO bigBetPlayer = TexasUtil.getPlayerBySeatNum(bigBetSeat, room.getIngamePlayers());
            // 小盲玩家下小盲注
            betchipIn(smallBetPlayer, smallBet, false);
            // 大盲玩家下大盲注
            betchipIn(bigBetPlayer, bigBet, false);
            // 更新下一个该操作的玩家
            room.setNextturn(TexasUtil.getNextSeatNum(bigBetSeat, room));
            // 更新下一轮该操作的玩家为小盲位置
            room.roundturn = smallBetSeat;
            // 当只有2个玩家时，第一轮Dealer（同时也是小盲）先操作
            // 第二轮开始大盲先操作
            if (room.getIngamePlayers().size() == 2) {
                room.roundturn = bigBetSeat;
            }
            // 分发手牌
            TexasUtil.assignHandPokerByRoom(room);
            SkillCardsUtil.assignSkillCardByRoom(room, 2, false);
            // 通知房间中在游戏中的玩家此刻的房间(包含私有信息【公共牌+手牌】的房间)状态信息

            for (PlayerVO p : room.getIngamePlayers()) {
                PrivateRoom pRoom = new PrivateRoom();
                pRoom.setRoom(room);
                // 私有房间信息（手牌）
                List<String> handPokers= new ArrayList<>();
                for(Card card: p.getHandPokers()) {
                    handPokers.add(card.toString());
                }
                pRoom.setPlayerSkillCards(p.getPlayerSkillCards());
                pRoom.setPower(p.getPower());
                pRoom.setHandPokers(handPokers);
                pRoom.setSkillUsed(true);
                RetMsg retMsg = new RetMsg();
                retMsg.setAction("onGameStart");
                retMsg.setState(1);
                retMsg.setMessage(JsonUtils.toJson(pRoom, PrivateRoom.class));

                TexasUtil.sendMsgToOne(p, JsonUtils.toJson(retMsg, RetMsg.class));
            }
            startTimer(room);// 开始计时
            // 游戏日志
//			gameLog.setStartTime(DateUtil.nowDatetime());
//			String initInfo = JsonUtils.toJson(this.getIngamePlayers(), this.getIngamePlayers().getClass());
//			gameLog.setPlayersInitInfo(initInfo);
//			gameLog.setRoomLevel(this.getLevel());
//			gameLog.setRoomType("普通场");//
//			gameLog.setBigBet(JsonUtils.toJson(bigBetPlayer, Player.class));
//			gameLog.setSmallBet(JsonUtils.toJson(smallBetPlayer, Player.class));
//			gameLog.setDealer(
//					JsonUtils.toJson(TexasUtil.getPlayerBySeatNum(dealer, this.getIngamePlayers()), Player.class));
//			GameLogService gameLogService = (GameLogService) SpringUtil.getBean("gameLogService");
//			gameLogService.insertGameLog(gameLog);

        }
    }

    /**
     * 结束游戏
     *
     * <pre>
     * 游戏状态变更为等待，游戏中的玩家移动到等待列表
     *
     * @param
     * @param
     */
    public void endGame(Room room) {
        Date now = new Date();
        // 牌型计算工具
        CardCalculator cardCalculator = new CardCalculator();
        // 尝试更新游戏状态为2：结算中
        if (room.getGamestate().compareAndSet(1, 2)) {
            long cut = 0;// 本局游戏的系统抽成筹码
            log.info("endGame begin");
            int allinCount = 0;
            // 统计allin玩家数
            for (PlayerVO p : room.getIngamePlayers()) {
                if (p.getBodyChips() == 0) {
                    allinCount++;
                }
            }
            // 如果公共牌没有发完，且allin人数大于等于2,则先发完公共牌
            if (room.getCalcCommunityCards().size() < 5 && allinCount >= 2) {
                log.info("communityCards.size() < 5 && allinCount >= 2");
                // 发公共牌
                int assignCardCount = 5 - room.getCalcCommunityCards().size();
                TexasUtil.assignCommonCardByNum(room, assignCardCount);
            }
            int timeBetween = room.getRestBetweenGame();

            if (room.getCalcCommunityCards().size() == 5) {
                // 成手牌列表
                for (PlayerVO p : room.getIngamePlayers()) {
                    List<String> hankPoker = new ArrayList<String>();
                    List<Card> hankPokerAndCommonCard = new ArrayList<Card>();
                    hankPokerAndCommonCard.addAll(room.getCalcCommunityCards());
                    for (int i = 0; i < p.getHandPokers().length; i++) {
                        hankPokerAndCommonCard.add(p.getHandPokers()[i]);
                        hankPoker.add(p.getHandPokers()[i].toString());
                    }

//					需要调整
                    // 判断牌型及计算得分
                    HandPower handPower = cardCalculator.rank(hankPokerAndCommonCard);
//					List<Card> maxCardsGroup = CardUtil.getMaxCardsGroup(hankPokerAndCommonCard);
                    // 加入成手牌列表
                    room.getFinalCardsMap().put(p.getSeatNum(), handPower.getCardList());
                    room.getCalcFinalCardsMap().put(p.getSeatNum(), handPower);
                    // 加入互相可见的手牌列表
                    room.handCardsMap.put(p.getSeatNum(), hankPoker);

                }
            }
            // 奖池列表
            List<BetPool> betPoolList = new ArrayList<BetPool>();
            // 计算betpool
            log.info("sumBetPoolList begin");
            sumBetPoolList(betPoolList, room.betMap, room.ingamePlayers);
            // 对每个分池结算
            for (BetPool betpool : betPoolList) {
                // 单个分池中的获胜玩家列表
                List<PlayerVO> winPlayerList = new ArrayList<>();
                // 本分池的玩家列表
                List<PlayerVO> poolPlayers = betpool.getBetPlayerList();
                if (room.getCalcFinalCardsMap().size() > 0) {
                    // 获取本分池获胜玩家
                    log.info("compareCardsToWinList begin");
                    winPlayerList = compareCardsToWinList(poolPlayers, room.getCalcFinalCardsMap());
                }
                // 没有则认为第一个获胜,若公共牌未发完结束游戏，存在该情况
                if (winPlayerList.size() == 0) {
                    for (PlayerVO p : poolPlayers) {
                        if (p != null && !p.isFold()) {
                            winPlayerList.add(p);
                            break;
                        }
                    }
                }
                Long win = 0l;
                if (winPlayerList.size() != 0) {
                    // 本次分池获胜的玩家分筹码
                    win = (Long) (betpool.getBetSum() / winPlayerList.size());
                }
                for (PlayerVO p : winPlayerList) {
                    TexasUtil.changePlayerChips(p, win);
                    // 在上个分池中已经赢的筹码，需要合并计算，加入winPlayersMap
                    Long lastPoolWin = room.getWinPlayersMap().get(p.getSeatNum());
                    if (lastPoolWin != null) {
                        win = win + lastPoolWin;
                    }
                    room.getWinPlayersMap().put(p.getSeatNum(), win);
//					LobbyServiceImpl.updateRankList(p);
                    log.info("winPlayersMap.put :" + p.getSeatNum() + " thisPoolWin:" + win + " poolplayerssize:"
                        + betpool.getBetPlayerList().size());
                }
            }

            // 发送结算消息给玩家
            String msg = JsonUtils.toJson(room, Room.class);
            RetMsg retMsg = new RetMsg();
            retMsg.setAction("onGameEnd");
            retMsg.setState(1);
            retMsg.setMessage(msg);
            TexasUtil.sendMsgToPlayerByRoom(room, JsonUtils.toJson(retMsg, RetMsg.class));
            // 清除本局状态信息
            room.getBetMap().clear();
            // 清除betRoundMap
            room.getBetRoundMap().clear();
            // 每局开始最大下注为一个大盲
            room.setRoundMaxBet(room.getBigBet());

            // 清除已经操作的玩家列表
            room.donePlayerList.clear();
            // 清除总下注
            room.setBetAmount(0);
            // 清除公共牌
            room.getCommunityCards().clear();
            room.getCalcCommunityCards().clear();
            // 清除牌堆
            room.getCardList().clear();

            Date costEnd = new Date();
            long cost = costEnd.getTime() - now.getTime();
            if (cost > 500) {
                log.error("endGame:" + " cost Millisecond" + cost);
            }
            // 尝试更新游戏状态为0：等待开始
            room.getGamestate().compareAndSet(2, 0);
            // 清除手牌
            for (PlayerVO p : room.getIngamePlayers()) {
                p.setHandPokers(null);
                p.setSkillCards(null);
                p.setPlayerSkillCards(null);
                p.setSkillDefenseCards(null);
                p.setPower(0);
                p.setStopGetSkillCardRound(0);
                p.setSkillUsed(true);
                if(p.getBodyChips() == 0) {
                    p.setExitRoomCountDownStartTime(System.currentTimeMillis());
                    assignChipsForLoser(p);
                    timeBetween = 11000; // 如果有需要补码的等待11s，10秒会把未补码玩家移除，1s作为前后端通信延迟缓冲
                }
            }
            // 将玩家都移入等待列表
            TexasUtil.movePlayers(room.getIngamePlayers(), room.getWaitPlayers());

            // 更新玩家筹码数到数据库
//			PlayerService pservice = (PlayerService) SpringUtil.getBean("playerService");
//			log.info("updatePlayer ingamePlayers begin size:" + ingamePlayers.size());
//			for (PlayerVO p : getWaitPlayers()) {
//				PlayerVO playerData = new PlayerVO();
//				playerData.setId(p.getId());
//				synchronized (p) {
//					// 当前筹码数等于桌上筹码+身上筹码
//					playerData.setChips(p.getChips() + p.getBodyChips());
////					pservice.updatePlayer(playerData);
//					if(playerData.getChips() == 0) {
//						assignChipsForInRoom(playerData);
//					}
//				}
//				log.info(
//						"updatePlayer ingamePlayers begin p:" + p.getUserName() + " chips:" + playerData.getChips());
//			}

            // 判断是否可以开始下一局
            checkStart(room, timeBetween);
        }
    }

    /**
     * 根据单个奖池玩家列表，最终成牌列表，计算获胜玩家列表
     *
     * @param poolPlayers
     * @param finalCardsMap
     * @return
     */
    public List<PlayerVO> compareCardsToWinList(List<PlayerVO> poolPlayers, Map<Integer, HandPower> finalCardsMap) {
        List<PlayerVO> winPlayerList = new ArrayList<>();
        HandPower oldHandPower = null;
        if (finalCardsMap.size() == 0) {
            log.info("finalCardsMap.size()==0 can not compareCardsToWinList");
            return null;
        }
        for (Entry<Integer, HandPower> e : finalCardsMap.entrySet()) {
            // 判断是否在本分池内
            boolean inThisPool = false;
            for (PlayerVO p : poolPlayers) {
                if (p != null && !p.isFold() && e.getKey() == p.getSeatNum()) {
                    inThisPool = true;
                    break;
                }
            }
            // 不在分池内
            if (!inThisPool) {
                continue;
            }
            log.info("compareCardsToWinList inThisPool:" + inThisPool);
            // 旧卡组为空，则加入
            if (oldHandPower == null) {
                oldHandPower = e.getValue();
                log.info("getPlayerBySeatNum begin seatNum:" + e.getKey() + " poolPlayers:"
                    + JsonUtils.toJson(poolPlayers, poolPlayers.getClass()));
                PlayerVO wp = TexasUtil.getPlayerBySeatNum(e.getKey(), poolPlayers);
                if (wp != null) {
                    winPlayerList.add(wp);
                } else {
                    log.info("winPlayerList.add e.getKey():" + e.getKey() + "wp not in poolPlayers");
                }
                log.info("winPlayerList.add e.getKey():" + e.getKey());
            } else {
                HandPower newHandPower = e.getValue();
                // 比较新旧卡组，大或相等则清空winPlayerList，加入新卡组的玩家
                log.info("compareCardsToWinList CardUtil.compareValue listNew:" + newHandPower + " listold" + oldHandPower);
                int result = oldHandPower.compareTo(newHandPower);
                log.info("compareCardsToWinList CardUtil.compareValue result:" + result);
                if (result == -1) {
                    winPlayerList.clear();
                    winPlayerList.add(TexasUtil.getPlayerBySeatNum(e.getKey(), poolPlayers));
                    oldHandPower = newHandPower;
                } else if (result == 0) {
                    winPlayerList.add(TexasUtil.getPlayerBySeatNum(e.getKey(), poolPlayers));
                }
            }
        }
        return winPlayerList;
    }

    /**
     * 开始计时
     */
    public void startTimer(Room room) {
        if (room.getTimerTask() != null) {
            room.getTimerTask().cancel();
            room.setTimerTask(null);
        }
        room.getTimer().purge();
        room.setTimerTask(new TimerTask() {
            @Override
            public void run() {
                try {
                    // 弃牌or check
                    PlayerVO p = TexasUtil.getPlayerBySeatNum(room.getNextturn(), room.getIngamePlayers());
                    if (p != null) {
                        // 帮其执行弃牌操作
                        log.info(p.getUserName() + " time is up,fold");
                        fold(p);
                    } else {
                        if (room.getGamestate().get() == 1) {
                            // 更新本应操作的玩家
                            TexasUtil.updateNextTurn(room);
                            // 发送轮到某玩家操作的消息
                            sendNextTurnMessage(room);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//					SystemLogService syslogService = (SystemLogService) SpringUtil.getBean("SystemLogServiceImpl");
//					SystemLogEntity entity = new SystemLogEntity();
//					entity.setType("roomTimer");
//					entity.setOperation("roomTimer");
//					entity.setContent(e.getCause() + e.getStackTrace().toString());
//					entity.setDatetime(yuelj.utils.dateTime.DateUtil.nowDatetime());
//					syslogService.insertSystemLog(entity);
                }
            }
        });
        // 考虑到网络传输延时，后台计时器多给与一个500毫秒的缓冲时间
        room.getTimer().schedule(room.getTimerTask(), room.getOptTimeout() + 500);
    }

    /**
     * 玩家弃牌
     *
     * @param player
     */
    public void fold(PlayerVO player) {
        Room room = player.getRoom();
        // 房间状态游戏中
        if (player.getRoom().getGamestate().get() != 1) {
            return;
        }
        if (player.getSeatNum() != room.getNextturn()) {
            return;
        }
        try {
            // 检测到玩家操作，计时取消
            cancelTimer(room);
            synchronized (player) {
                // 弃牌
                player.setFold(true);
            }
            // 发送弃牌消息给玩家
            String msg = JsonUtils.toJson(player, PlayerVO.class);
            RetMsg retMsg = new RetMsg();
            retMsg.setAction("onPlayerFold");
            retMsg.setState(1);
            retMsg.setMessage(msg);
            TexasUtil.sendMsgToPlayerByRoom(room, JsonUtils.toJson(retMsg, RetMsg.class));
            // 将玩家移动到等待列表
            TexasUtil.removeIngamePlayer(player);
            int index = room.donePlayerList.indexOf(player.getSeatNum());
            // 玩家在donePlayerList，则移除
            if (index != -1) {
                room.donePlayerList.remove(index);
            }
            player.setNextRoundSkillAction("");
            player.setThisRoundSkillAction("");
            room.getWaitPlayers().add(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 判断下一步是否round结束，endgame或下个玩家操作nextturn
        endRoundOrNextTurn(room);
        // 记录日志
        PlayerOpt opt = new PlayerOpt();
        opt.setOptTime(DateUtil.nowDatetime());
        opt.setOptType("fold");// 操作类型（跟注、加注、弃牌、全下）
        opt.setRemark("");// 备注
        opt.setPlayerId(player.getId());
        opt.setSeatNum(player.getSeatNum());
        room.opts.add(opt);
    }

    /**
     * 判断下一步是否round结束，endgame或下个玩家操作nextturn
     */
    public void endRoundOrNextTurn(Room room) {
        // 判断是否可以结束本轮
        boolean roundEnd = checkRoundEnd(room);
        if (!roundEnd) {
            // 更新nextturn
            TexasUtil.updateNextTurn(room);
            // 发送轮到某玩家操作的消息
            sendNextTurnMessage(room);
        }
    }

    /**
     * 取消计时
     */
    public boolean cancelTimer(Room room) {
        boolean result = false;
        if (room.getTimerTask() != null) {
            result = room.getTimerTask().cancel();
            room.setTimerTask(null);
        }
        return result;
    }

    /**
     * 为进入房间的用户分配筹码
     *
     * @param
     * @param player
     */
    public void assignChipsForInRoom(Room room, PlayerVO player) {
        long takeChip = room.getMaxChips();
        // 如果玩家的所剩筹码不超过房间规定的最大带入筹码，则该玩家筹码全部带入
        if (player.getChips() < takeChip) {
            takeChip = player.getChips();
        }
        synchronized (player) {
            player.setChips(player.getChips() - takeChip);
            player.setBodyChips(takeChip);
        }
    }

    /**
     * 失去筹码玩家补充筹码
     * @param player
     */
    public void assignChipsForLoser(PlayerVO player) {

        RetMsg retMsg = new RetMsg();
        retMsg.setAction("onAssignChips");
        retMsg.setState(1);
        retMsg.setMessage(player.getRoom().getMaxChips());

        TexasUtil.sendMsgToOne(player, JsonUtils.toJson(retMsg, RetMsg.class));
    }

    /**
     * 为用户初始化为房间最大带入
     *
     * @param
     * @param player
     */
    public void assignChipsToRoomMax(PlayerVO player) {
        // 玩家的钱多退少补，使其等于房间最大带入
        long takeChip = player.getRoom().getMaxChips() - player.getBodyChips();
        // 如果玩家的所剩筹码不超过需要补足的，则带入所有
        if (player.getChips() < takeChip) {
            takeChip = player.getChips();
        }
        synchronized (player) {
            player.setChips(player.getChips() - takeChip);
            player.setBodyChips(takeChip + player.getBodyChips());
        }
    }

    /**
     * 为退出房间的用户分配筹码,并入库
     *
     * @param
     * @param player
     */
    public void assignChipsForOutRoom(PlayerVO player) {
        synchronized (player) {
            // 当前筹码数等于桌上筹码+身上筹码
            player.setChips(player.getChips() + player.getBodyChips());
            player.setBodyChips(0);
            // 玩家筹码信息入库
//			PlayerService playerService = (PlayerService) SpringUtil.getBean("playerService");
//			playerService.updatePlayer(player);
        }
    }



    public void check(PlayerVO player, boolean isAllin) {
        Room room = player.getRoom();
        // 房间状态游戏中
        if (room.getGamestate().get() != 1) {
            return;
        }
        // 是否轮到该玩家操作
        if (player.getSeatNum() != room.getNextturn()) {
            return;
        }
        if(!isAllin) {
            // 玩家此次操作之前的本轮下注额
            long oldBetThisRound = 0;
            if (room.getBetRoundMap().get(player.getSeatNum()) != null) {
                oldBetThisRound = room.getBetRoundMap().get(player.getSeatNum());
            }
            // 小于最大下注，不能check
            if (oldBetThisRound < room.getRoundMaxBet()) {
                log.info("can not check, bet:" + oldBetThisRound + " getRoundMaxBet:" + room.getRoundMaxBet());
                return;
            }
        }

        try {
            // 检测到玩家操作，计时取消
            cancelTimer(room);
            // 记录当前轮已经操作过的玩家
            if (!room.donePlayerList.contains(player.getSeatNum())) {
                player.setNextRoundSkillAction("");
                player.setThisRoundSkillAction("");
                room.donePlayerList.add(player.getSeatNum());
            }
            // 如果不需要通知其他玩家则直接判断下一轮 （场景：当前此玩家已All in）
            if(!isAllin) {
                endRoundOrNextTurn(room);
            }
            // 发送过牌消息给玩家
            String msg = JsonUtils.toJson(player, PlayerVO.class);
            RetMsg retMsg = new RetMsg();
            retMsg.setAction("onPlayerCheck");
            retMsg.setState(1);
            retMsg.setMessage(msg);
            TexasUtil.sendMsgToPlayerByRoom(room, JsonUtils.toJson(retMsg, RetMsg.class));

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 判断下一步是否round结束，endgame或下个玩家操作nextturn
        endRoundOrNextTurn(room);
    }

    /**
     * 下注
     *
     * @param player    下注的玩家
     * @param chip      下注的筹码
     * @param playerOpt 认为玩家操作过true
     */
    public boolean betchipIn(PlayerVO player, int chip, boolean playerOpt) {
        if (player == null) {
            return false;
        }
        Room room  = player.getRoom();
        if (playerOpt && player.getSeatNum() != room.getNextturn()) {
            return false;
        }
        Room thisRoom = player.getRoom();

        PlayerOpt opt = new PlayerOpt();
        opt.setOptChips(chip);
        opt.setOptTime(DateUtil.nowDatetime());
        opt.setOptType("");// 操作类型（跟注、加注、弃牌、全下）
        // 第几轮
        int rd = Math.abs(thisRoom.getCommunityCards().size() - 1);
        opt.setRound(rd);
        opt.setRemark("");// 备注
        opt.setPlayerId(player.getId());
        opt.setSeatNum(player.getSeatNum());
        room.opts.add(opt);

        // 玩家此次操作之前的本轮下注额
        long oldBetThisRound = 0;
        if (thisRoom.getBetRoundMap().get(player.getSeatNum()) != null) {
            oldBetThisRound = thisRoom.getBetRoundMap().get(player.getSeatNum());
        }
        // 无效下注额,1筹码不足
        if (chip <= 0 || chip > player.getBodyChips()) {
            log.error("betchipIn error not enough chips:" + chip + " getBodyChips():" + player.getBodyChips());
            return false;
        }
        if (playerOpt) {
            // 2.在没有allin的情况下，如果不是跟注，则下注必须是大盲的整数倍
            if (chip < player.getBodyChips()) {
                //2.1跟注-- 不能小于之前下注,否则强制增加到跟注筹码，不够则allin
                if ((chip + oldBetThisRound) < thisRoom.getRoundMaxBet()) {
                    log.error("betchipIn error < getRoundMaxBet() chip:" + chip + "oldBetThisRound:"
                        + oldBetThisRound + " max:" + thisRoom.getRoundMaxBet());
                    if (thisRoom.getRoundMaxBet() - oldBetThisRound < player.getBodyChips()) {
                        chip = (int) (thisRoom.getRoundMaxBet() - oldBetThisRound);
                    } else {
                        chip = (int) player.getBodyChips();
                    }
                }
                //2.2加注，反加--
                if ((chip + oldBetThisRound) > thisRoom.getRoundMaxBet() && (chip + oldBetThisRound) < thisRoom.getRoundMaxBet()*2) {
//					// 本轮已经下注+当前加注-本轮最大下注，必须=大盲注的整数倍
//					if ((chip + oldBetThisRound - thisRoom.getRoundMaxBet()) >= thisRoom.getBigBet() != 0) {
//						log.error("betchipIn error % bigbet != 0:" + chip + "oldBetThisRound:" + oldBetThisRound
//								+ ",max:" + thisRoom.getRoundMaxBet());
//						return false;
//					}
                    // 加注必须大于2倍之前最大下注（增量加注规则暂时不考虑）
                    log.error("betchipIn error (totalBet > totalBet && totalBet < 2*RoundMaxBet) :" + chip + "oldBetThisRound:" + oldBetThisRound
                        + ",max:" + thisRoom.getRoundMaxBet());
                    return false;
                }
            }

        }
        try {
            if (playerOpt) {
                // 检测到玩家操作，计时取消
                cancelTimer(room);
            }
            // 设置本轮最大加注
            if ((int) (chip + oldBetThisRound) > thisRoom.getRoundMaxBet()) {
                thisRoom.setRoundMaxBet((int) (chip + oldBetThisRound));
                // 加注额大于之前，则所有玩家重新加注
                room.donePlayerList.clear();
            }
            // 总奖池
            thisRoom.setBetAmount(room.getBetAmount() + chip);
            // 记录当前轮已经操作过的玩家
            if (!thisRoom.donePlayerList.contains(player.getSeatNum()) && playerOpt) {
                thisRoom.donePlayerList.add(player.getSeatNum());
            }
            // 刷新下注列表
            Long beforeBet = 0l;
            if (thisRoom.getBetMap().get(player.getSeatNum()) != null) {
                beforeBet = thisRoom.getBetMap().get(player.getSeatNum());
            }
            // 加入玩家总下注map
            thisRoom.getBetMap().put(player.getSeatNum(), beforeBet + chip);
            // 加入玩家本轮下注
            thisRoom.getBetRoundMap().put(player.getSeatNum(), chip + oldBetThisRound);
            // 筹码入池，所带筹码扣除
            player.setBodyChips(player.getBodyChips() - chip);
            player.setNextRoundSkillAction("");
            player.setThisRoundSkillAction("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (playerOpt) {
            try {
                PlayerVO bp = new PlayerVO();
                bp.setBodyChips(player.getBodyChips());
                bp.setUserName(player.getUserName());
                bp.setInChips(chip);
                bp.setSeatNum(player.getSeatNum());
                String message = JsonUtils.toJson(bp, PlayerVO.class);
                RetMsg retMsg = new RetMsg();
                retMsg.setAction("onPlayerBet");// 告诉前台有玩家下注了
                retMsg.setState(1);
                retMsg.setMessage(message);
                String msg = JsonUtils.toJson(retMsg, RetMsg.class);
                // 通知房间中玩家有人下注了
                TexasUtil.sendMsgToPlayerByRoom(thisRoom, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 判断下一步是否round结束，endgame或下个玩家操作nextturn
            endRoundOrNextTurn(room);
        }
        return true;
    }

    /**
     * 判断本轮是否可以结束
     */
    public boolean checkRoundEnd(Room room) {
        // 判断本轮是否可以结束
        boolean canEndRound = true;

        // 当前还有筹码的玩家数量
        int haveBodyChipsCount = 0;

        if (room.getIngamePlayers().size() == 1) {
            // 结算游戏
            log.info("only one IngamePlayers endgame start");
            endGame(room);
            return true;
        }
        // 将已经allin的玩家加入已操作列表
        for (PlayerVO p : room.getIngamePlayers()) {
            if (p.getBodyChips() == 0) {
                room.donePlayerList.add(p.getSeatNum());
            }
        }
        // 所有人都已经操作过
        if (room.donePlayerList.size() >= room.getIngamePlayers().size()) {
            long betMax = 0l;// 以最大下注作为参照筹码
            for (int i = 0; i < room.getIngamePlayers().size(); i++) {
                PlayerVO p = room.getIngamePlayers().get(i);
                // 为betMax赋初始值
                if (room.getBetMap().get(p.getSeatNum()) != null && room.getBetMap().get(p.getSeatNum()) > betMax) {
                    betMax = room.getBetMap().get(p.getSeatNum());
                }
            }
            log.info("checkRoundEnd betMax:" + betMax);
            for (int i = 0; i < room.getIngamePlayers().size(); i++) {
                PlayerVO p = room.getIngamePlayers().get(i);
                // 已经弃牌的排除在外
                if (p == null || p.isFold()) {
                    continue;
                }
                if (room.getBetMap().get(p.getSeatNum()) == null) {
                    // 存在没有下注的玩家
                    log.info("checkRoundEnd no bet seatNum:" + p.getSeatNum());
                    canEndRound = false;
                    break;
                }
                // 没有allin的玩家中
                if (p.getBodyChips() > 0) {
                    haveBodyChipsCount ++;
                    // 没有弃牌的玩家中，存在下注额度小于betMax，则本轮不能结束
                    if (betMax > room.getBetMap().get(p.getSeatNum())) {
                        log.info("not allin bet<maxbet seatNum:" + p.getSeatNum() + " bet "
                            + room.getBetMap().get(p.getSeatNum()) + " betMax:" + betMax);
                        canEndRound = false;
                        break;
                    }
                }
            }
        } else {
            log.info("checkRoundEnd getDonePlayerList().size() < getIngamePlayers().size()");
            canEndRound = false;
        }

        // 如果下一个可以操作的玩家无法更新，游戏结束
        int turn = TexasUtil.getNextSeatNum(room.getNextturn(), room);
        if (turn == room.getNextturn()) {
            canEndRound = true;
        }
        if (canEndRound) {
            log.info("canEndRound = true");
            // 第二轮最低加注设为0
            room.setRoundMaxBet(0);
            // 开始新的一轮
            // 清除玩家本轮下注
            room.betRoundMap.clear();
            // 清空操作过的玩家列表
            room.donePlayerList.clear();
            // 已经无法操作的玩家加入DonePlayerList
            for (int i = 0; i < room.getIngamePlayers().size(); i++) {
                PlayerVO p = room.getIngamePlayers().get(i);
                if (p != null && p.getBodyChips() <= 0) {
                    room.donePlayerList.add(p.getSeatNum());
                }
            }
            // 如果公共牌等于5张,且可操作玩家数小于总玩家数
            if (room.getCommunityCards().size() < 5 && room.donePlayerList.size() < room.getIngamePlayers().size()) {
                // 更新nextturn为每轮第一个人
                PlayerVO roundturnp = TexasUtil.getPlayerBySeatNum(room.roundturn, room.getIngamePlayers());
                if (roundturnp == null || roundturnp.isFold() || roundturnp.getBodyChips() == 0) {
                    room.roundturn = TexasUtil.getNextSeatNum(room.roundturn, room);
                }
                room.setNextturn(room.roundturn);

                // 如果只有一个玩家还有筹码则下一轮自动过牌（全场ALL IN情况下）
                if(haveBodyChipsCount <= 1) {
                    sendNextTurnAutoCheckMessage(room);
                } else {
                    // 发送轮到某玩家操作的消息
                    sendNextTurnMessage(room);
                }

                // 发公共牌
                int assignCardCount = room.getCommunityCards().size() == 0 ? 3 : 1;
                TexasUtil.assignCommonCardByNum(room, assignCardCount);

            } else {
                // 结算游戏
                endGame(room);
            }
        }
        return canEndRound;
    }

    /**
     * 计算奖池分池
     *
     * @param betPoolList
     * @param betMap
     * @param ingamePlayers
     */
    public void sumBetPoolList(List<BetPool> betPoolList, Map<Integer, Long> betMap, List<PlayerVO> ingamePlayers) {
        // 对map按照值排序
        betMap = TexasUtil.sortMapByValue(betMap);
        boolean complete = false;
        while (!complete) {
            complete = true;
            // 该分池总金额
            Long betSum = 0l;
            // 该分池单个金额
            Long thisBet = 0l;
            BetPool pool = new BetPool();
            for (Entry<Integer, Long> e : betMap.entrySet()) {
                // 发现不为0的下注，则继续新的分池
                if (e.getValue() != 0) {
                    complete = false;
                    if (thisBet == 0) {
                        thisBet = e.getValue();
                    }
                    betSum = betSum + thisBet;
                    if (e.getValue() - thisBet < 0) {
                        System.out.println("betMap计算错误！下注排序从小到大");
                    }
                    // 减去本轮分池单个金额
                    e.setValue(e.getValue() - thisBet);
                    // 加入betpool
                    PlayerVO p = TexasUtil.getPlayerBySeatNum(e.getKey(), ingamePlayers);
                    if (p != null) {
                        pool.getBetPlayerList().add(p);
                    }
                }
            }
            pool.setBetSum(betSum);
            if (pool.getBetSum() != 0) {
                betPoolList.add(pool);
            }
        }
        betMap.clear();
    }

    /**
     * 发送轮到某玩家操作的消息
     */
    public void sendNextTurnMessage(Room room) {
//		String message = getNextturn() + "";
        RetMsg retMsg = new RetMsg();
        // 告诉前台轮到某个玩家操作了
        retMsg.setAction("onPlayerTurn");
        retMsg.setState(1);
        retMsg.setMessage(room.getNextturn() + "");
        String msg = JsonUtils.toJson(retMsg, RetMsg.class);
        // 轮到下一家操作，并开始计时
        startTimer(room);
        TexasUtil.sendMsgToPlayerByRoom(room, msg);
    }

    /**
     * 发送轮到某玩家操作的消息,自动过牌
     */
    public void sendNextTurnAutoCheckMessage(Room room) {
//		String message = getNextturn() + "";
        RetMsg retMsg = new RetMsg();
        // 告诉前台轮到某个玩家操作了
        retMsg.setAction("onPlayerTurnAutoCheck");
        retMsg.setState(1);
        retMsg.setMessage(room.getNextturn() + "");
        String msg = JsonUtils.toJson(retMsg, RetMsg.class);
        // 轮到下一家操作，并开始计时
        startTimer(room);
        TexasUtil.sendMsgToPlayerByRoom(room, msg);
    }

    /**
     * 判断是否可以结束游戏
     */
    public boolean checkEnd(Room room) {
        int playerCount = 0;
        for (PlayerVO p : room.getIngamePlayers()) {
            if (!p.isFold() && p.getBodyChips() != 0) {
                playerCount++;
            }
        }
        if (playerCount < 2) {
            this.endGame(room);
            return true;
        }
        log.info(playerCount + "断线后人数大于1");
        return false;
    }
}
