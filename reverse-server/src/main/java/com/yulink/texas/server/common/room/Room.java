package com.yulink.texas.server.common.room;

import com.google.gson.annotations.Expose;
import com.yulink.texas.common.InitCards;
import com.yulink.texas.common.card.Card;
import com.yulink.texas.core.domain.SkillDictionary;
import com.yulink.texas.server.common.entity.PlayerOpt;
import com.yulink.texas.server.common.entity.PlayerVO;
import com.yulink.texas.server.common.entity.PrivateRoom;
import com.yulink.texas.server.common.entity.RetMsg;
import com.yulink.texas.server.common.entity.SkillDictionaryVO;
import com.yulink.texas.server.common.utils.BetPool;
import com.yulink.texas.server.common.utils.CardCalculator;
import com.yulink.texas.server.common.utils.DateUtil;
import com.yulink.texas.server.common.utils.HandPower;
import com.yulink.texas.server.common.utils.JsonUtils;
import com.yulink.texas.server.common.utils.SkillCardsUtil;
import com.yulink.texas.server.common.utils.SpringUtil;
import com.yulink.texas.server.manager.SkillManager;
import com.yulink.texas.server.ws.TexasUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 房间实体
 * 
 * @author Ming
 *
 */
@Component
public class Room {
	private static Logger logger = LogManager.getLogger(Room.class);

//	由于Room是new出来的导致自动注入无法使用，需要手动获取bean然后进行操作
//	@Resource
//	private SkillManager skillManager;

	/**
	 * 设置初始化房间数量
	 */
	private static final int INIT_ROOM_COUNT = 20;
	static {
		// 初始化创建20个房间
		for (int i = 0; i < INIT_ROOM_COUNT; i++) {
			TexasUtil.createRoom(0,0);
			TexasUtil.createRoom(1,0);
		}
	}
	/**
	 * 游戏日志
	 */
//	protected GameLog gameLog = new GameLog();
	protected List<PlayerOpt> opts = new ArrayList<PlayerOpt>();
	/**
	 * 房间id
	 */
	@Expose
	private int id;
	/**
	 * 房间级别
	 */
	@Expose
	private int level;
	/**
	 * 房间类型，0、德州，1、系统卡牌版，2、玩家卡组版
	 */
	@Expose
	private int type;

	/**
	 * 允许带入的最大筹码
	 */
	@Expose
	private int maxChips;
	/**
	 * 允许带入的最小筹码
	 */
	@Expose
	private int minChips;
	/**
	 * 大盲下注筹码
	 */
	@Expose
	private int bigBet;
	/**
	 * 小盲下注筹码
	 */
	@Expose
	private int smallBet;
	/**
	 * 最大玩家数
	 */
	@Expose
	private int maxPlayers;
	/**
	 * 最小玩家数
	 */
	@Expose
	private int minPlayers;
	/**
	 * D，最佳座位，庄家（座位号）
	 */
	@Expose
	private int dealer;
	/**
	 * 小盲玩家座位号
	 */
	@Expose
	private int smallBetSeatNum;
	/**
	 * 大盲玩家座位号
	 */
	@Expose
	private int bigBetSeatNum;
	/**
	 * 游戏状态（0，等待；1，游戏，2结算中）
	 */
	@Expose
	private AtomicInteger gamestate = new AtomicInteger(0);
	/**
	 * 房间状态（0，不可加入；1，可加入）
	 */
	private volatile int roomstate = 1;
	/**
	 * 房间中处于等待状态的玩家列表
	 */
	@Expose
	private List<PlayerVO> waitPlayers = new CopyOnWriteArrayList<PlayerVO>();
	/**
	 * 房间中处于游戏状态的玩家列表
	 */
	@Expose
	protected List<PlayerVO> ingamePlayers = new CopyOnWriteArrayList<PlayerVO>();
//	/**
//	 * 一局的牌组，随着分发手牌和公共牌会进行remove操作
//	 */
	protected List<Card> cardList = new CopyOnWriteArrayList<>();

	/**
	 * 完整牌组，初始赋值cardList值，不进行remove操作
	 */
	protected List<Card> cardListWhole = new CopyOnWriteArrayList<>();
	/**
	 * 公共牌
	 */
	@Expose
	protected List<String> communityCards = new CopyOnWriteArrayList<String>();

	/**
	 * 公共牌 用于计算，不给用户展示
	 */
	protected List<Card> calcCommunityCards = new CopyOnWriteArrayList<Card>();

	/**
	 * 当前所处回合
	 * P: 翻牌前
	 * F: 翻牌
	 * T: 转牌
	 * R: 河牌
	 */
	@Expose
	protected String currentRound;

	/**
	 * 奖池,下注总额
	 */
	@Expose
	protected long betAmount;
	/**
	 * 每个玩家下的注，玩家和其本局游戏下注的总额
	 */
	protected Map<Integer, Long> betMap = new LinkedHashMap<>();

	/**
	 * 在一回合中，每个玩家下的注[座位号，本轮下注额]
	 */
	@Expose
	protected Map<Integer, Long> betRoundMap = new LinkedHashMap<>();

	/**
	 * 操作过的玩家列表
	 */
	public List<Integer> donePlayerList = new CopyOnWriteArrayList<Integer>();

	/**
	 * 下一个行动的玩家id
	 */
	@Expose
	protected volatile int nextturn = 0;// next player
	/**
	 * 每轮第一个行动的玩家
	 */
	protected volatile int roundturn = 0;
	/**
	 * 本轮游戏玩家下的最大注倍数，第一轮为1，一共3种，1,2,4
	 */
	protected int roundMaxBet = bigBet;
	/**
	 * 存放座位号的栈(空闲座位)
	 */
	@Expose
	private Stack<Integer> freeSeatStack;
	/**
	 * 两局之间的间隔时间,秒
	 */
	@Expose
	private int restBetweenGame = 5000;

	/**
	 * 操作超时时间，单位毫秒（玩家在规定时间内没有完成操作，则系统自动帮其弃牌）
	 */
	@Expose
	private int optTimeout = 600000;

	/**
	 * 房间中等待操作的计时器(一个房间中不允许同时生成多个计时器)
	 */
	private Timer timer = new Timer();
	/**
	 * 游戏中玩家成手牌列表
	 */
	@Expose
	protected Map<Integer, List<String>> finalCardsMap = new LinkedHashMap<Integer, List<String>>();

	/**
	 * 游戏中玩家成手牌列表(不展示给用户) 用于计算结果
	 */
	protected Map<Integer, HandPower> calcFinalCardsMap = new LinkedHashMap<Integer, HandPower>();
	/**
	 * 最后亮牌玩家手牌列表
	 */
	@Expose
	protected Map<Integer, List<String>> handCardsMap = new LinkedHashMap<Integer, List<String>>();
	/**
	 * 所有获胜玩家列表
	 */
	@Expose
	protected Map<Integer, Long> winPlayersMap = new LinkedHashMap<Integer, Long>();

	/**
	 * 系统卡牌列表（具体到玩家的时候该列表需要单独洗牌并填充）
	 */
	protected List<SkillDictionaryVO> skillCards = new CopyOnWriteArrayList<>();

	/**
	 * 当前场上陷阱数量
	 */
	@Expose
	protected int skillTrapCount = 0;

	/**
	 * 当前场上陷阱列表
	 */
	protected List<SkillDictionaryVO>  skillTrapCards = new CopyOnWriteArrayList<>();
	/**
	 * 
	 * 开始游戏
	 * 
	 * <pre>
	 * 游戏状态变更为游戏中，等待中的玩家移动到游戏中列表
	 * 
	 * @param
	 */
	public void startGame() {
		if (this.getGamestate().compareAndSet(0, 1)) {
			// 游戏日志-玩家操作信息
			opts.clear();

			// 由于需要通知在结束阶段进入的玩家牌局信息
			// 因此延迟到下局开始清除上局信息
			finalCardsMap.clear();
			handCardsMap.clear();
			winPlayersMap.clear();
			currentRound = "P"; // 翻牌前
			skillTrapCount = 0;
			skillCards.clear();
			skillTrapCards.clear();

			if(getType() == 1) {
				SkillManager skillManager = (SkillManager) SpringUtil.getBean("skillManager");
				List<SkillDictionary> skillDictionaryList = skillManager.getListByRedis();
				for(SkillDictionary skillDictionary: skillDictionaryList) {
					SkillDictionaryVO skillDictionaryVO = new SkillDictionaryVO();
					BeanUtils.copyProperties(skillDictionary, skillDictionaryVO);
					skillDictionaryVO.setCount(-1);
					skillCards.add(skillDictionaryVO);
				}
			}


			// 重新补筹码
			for (PlayerVO p : getWaitPlayers()) {
				if (p.getBodyChips() == 0) {
					assignChipsForInRoom(p);
				}
			}
			// TODO 筹码不足需要提示前端补足筹码然后上桌
			// 总筹码不足一个大盲注的不能进行游戏，踢出房间
			long nowTime = System.currentTimeMillis();
			getWaitPlayers().parallelStream().filter(p -> p.getBodyChips() < this.getBigBet()
				&& p.getExitRoomCountDownStartTime() > 0 && nowTime > p.getExitRoomCountDownStartTime()+p.getExitRoomCountDownInterval())
					.forEach(p -> TexasUtil.outRoom(p));

			// 转移等待列表的玩家进入游戏中玩家列表
			TexasUtil.movePlayers(this.getWaitPlayers(), this.getIngamePlayers());
			// 记录玩家座位号
			for (PlayerVO p : getIngamePlayers()) {
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
			TexasUtil.updateNextDealer(this);
			// 得到一副洗好的牌（随机卡组）
			this.setCardList(new InitCards().cards);

			// 复制一份洗好的牌组放到完整牌组记录中，用于追溯回查
			cardListWhole = cardList.stream().collect(Collectors.toList());
			// 确定大小盲主，并分配筹码到奖池
			// 最佳位置
			int dealer = getDealer();
			// 小盲注
			int smallBet = getSmallBet();
			// 大盲注
			int bigBet = getBigBet();
			// 小盲位置
			int smallBetSeat = TexasUtil.getNextSeatNum(dealer, this);
			// 当只有2个玩家时，dealer才是小盲
			if (getIngamePlayers().size() == 2) {
				smallBetSeat = dealer;
			}
			this.setSmallBetSeatNum(smallBetSeat);
			// 大盲位置
			int bigBetSeat = TexasUtil.getNextSeatNum(smallBetSeat, this);
			this.setBigBetSeatNum(bigBetSeat);
			// 小盲玩家
			PlayerVO smallBetPlayer = TexasUtil.getPlayerBySeatNum(smallBetSeat, getIngamePlayers());
			// 大盲玩家
			PlayerVO bigBetPlayer = TexasUtil.getPlayerBySeatNum(bigBetSeat, getIngamePlayers());
			// 小盲玩家下小盲注
			betchipIn(smallBetPlayer, smallBet, false);
			// 大盲玩家下大盲注
			betchipIn(bigBetPlayer, bigBet, false);
			// 更新下一个该操作的玩家
			nextturn = TexasUtil.getNextSeatNum(bigBetSeat, this);
			// 更新下一轮该操作的玩家为小盲位置
			roundturn = smallBetSeat;
			// 当只有2个玩家时，第一轮Dealer（同时也是小盲）先操作
			// 第二轮开始大盲先操作
			if (getIngamePlayers().size() == 2) {
				roundturn = bigBetSeat;
			}
			// 分发手牌
			TexasUtil.assignHandPokerByRoom(this);
			SkillCardsUtil.assignSkillCardByRoom(this, 2, false);
			// 通知房间中在游戏中的玩家此刻的房间(包含私有信息【公共牌+手牌】的房间)状态信息

			for (PlayerVO p : getIngamePlayers()) {
				PrivateRoom pRoom = new PrivateRoom();
				pRoom.setRoom(this);
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
			startTimer(this);// 开始计时
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
	public void endGame() {
		Date now = new Date();
		// 牌型计算工具
		CardCalculator cardCalculator = new CardCalculator();
		// 尝试更新游戏状态为2：结算中
		if (this.getGamestate().compareAndSet(1, 2)) {
			long cut = 0;// 本局游戏的系统抽成筹码
			logger.info("endGame begin");
			int allinCount = 0;
			// 统计allin玩家数
			for (PlayerVO p : getIngamePlayers()) {
				if (p.getBodyChips() == 0) {
					allinCount++;
				}
			}
			// 如果公共牌没有发完，且allin人数大于等于2,则先发完公共牌
			if (calcCommunityCards.size() < 5 && allinCount >= 2) {
				logger.info("communityCards.size() < 5 && allinCount >= 2");
				// 发公共牌
				int assignCardCount = 5 - calcCommunityCards.size();
				TexasUtil.assignCommonCardByNum(this, assignCardCount);
			}
			int timeBetween = getRestBetweenGame();

			if (calcCommunityCards.size() == 5) {
				// 成手牌列表
				for (PlayerVO p : getIngamePlayers()) {
					List<String> hankPoker = new ArrayList<String>();
					List<Card> hankPokerAndCommonCard = new ArrayList<Card>();
					hankPokerAndCommonCard.addAll(calcCommunityCards);
					for (int i = 0; i < p.getHandPokers().length; i++) {
						hankPokerAndCommonCard.add(p.getHandPokers()[i]);
						hankPoker.add(p.getHandPokers()[i].toString());
					}

//					需要调整
					// 判断牌型及计算得分
					HandPower handPower = cardCalculator.rank(hankPokerAndCommonCard);
//					List<Card> maxCardsGroup = CardUtil.getMaxCardsGroup(hankPokerAndCommonCard);
					// 加入成手牌列表
					finalCardsMap.put(p.getSeatNum(), handPower.getCardList());
					calcFinalCardsMap.put(p.getSeatNum(), handPower);
					// 加入互相可见的手牌列表
					handCardsMap.put(p.getSeatNum(), hankPoker);

				}
			}
			// 奖池列表
			List<BetPool> betPoolList = new ArrayList<BetPool>();
			// 计算betpool
			logger.info("sumBetPoolList begin");
			sumBetPoolList(betPoolList, betMap, ingamePlayers);
			// 对每个分池结算
			for (BetPool betpool : betPoolList) {
				// 单个分池中的获胜玩家列表
				List<PlayerVO> winPlayerList = new ArrayList<>();
				// 本分池的玩家列表
				List<PlayerVO> poolPlayers = betpool.getBetPlayerList();
				if (calcFinalCardsMap.size() > 0) {
					// 获取本分池获胜玩家
					logger.info("compareCardsToWinList begin");
					winPlayerList = compareCardsToWinList(poolPlayers, calcFinalCardsMap);
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
					Long lastPoolWin = winPlayersMap.get(p.getSeatNum());
					if (lastPoolWin != null) {
						win = win + lastPoolWin;
					}
					winPlayersMap.put(p.getSeatNum(), win);
//					LobbyServiceImpl.updateRankList(p);
					logger.info("winPlayersMap.put :" + p.getSeatNum() + " thisPoolWin:" + win + "poolplayerssize:"
							+ betpool.getBetPlayerList().size());
				}
			}

			// 发送结算消息给玩家
			String msg = JsonUtils.toJson(this, Room.class);
			RetMsg retMsg = new RetMsg();
			retMsg.setAction("onGameEnd");
			retMsg.setState(1);
			retMsg.setMessage(msg);
			TexasUtil.sendMsgToPlayerByRoom(this, JsonUtils.toJson(retMsg, RetMsg.class));
			// 清除本局状态信息
			betMap.clear();
			// 清除betRoundMap
			betRoundMap.clear();
			// 每局开始最大下注为一个大盲
			roundMaxBet = bigBet;

			// 清除已经操作的玩家列表
			donePlayerList.clear();
			// 清除总下注
			betAmount = 0;
			// 清除公共牌
			communityCards.clear();
			calcCommunityCards.clear();
			// 清除牌堆
			cardList.clear();

			Date costEnd = new Date();
			long cost = costEnd.getTime() - now.getTime();
			if (cost > 500) {
				logger.error("endGame:" + " cost Millisecond" + cost);
			}
			// 尝试更新游戏状态为0：等待开始
			this.getGamestate().compareAndSet(2, 0);
			// 清除手牌
			for (PlayerVO p : ingamePlayers) {
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
			TexasUtil.movePlayers(getIngamePlayers(), getWaitPlayers());

			// 更新玩家筹码数到数据库
//			PlayerService pservice = (PlayerService) SpringUtil.getBean("playerService");
//			logger.info("updatePlayer ingamePlayers begin size:" + ingamePlayers.size());
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
//				logger.info(
//						"updatePlayer ingamePlayers begin p:" + p.getUserName() + " chips:" + playerData.getChips());
//			}

			// 判断是否可以开始下一局
			checkStart(timeBetween);
		}
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
			logger.info("finalCardsMap.size()==0 can not compareCardsToWinList");
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
			logger.info("compareCardsToWinList inThisPool:" + inThisPool);
			// 旧卡组为空，则加入
			if (oldHandPower == null) {
				oldHandPower = e.getValue();
				logger.info("getPlayerBySeatNum begin seatNum:" + e.getKey() + " poolPlayers:"
						+ JsonUtils.toJson(poolPlayers, poolPlayers.getClass()));
				PlayerVO wp = TexasUtil.getPlayerBySeatNum(e.getKey(), poolPlayers);
				if (wp != null) {
					winPlayerList.add(wp);
				} else {
					logger.info("winPlayerList.add e.getKey():" + e.getKey() + "wp not in poolPlayers");
				}
				logger.info("winPlayerList.add e.getKey():" + e.getKey());
			} else {
				HandPower newHandPower = e.getValue();
				// 比较新旧卡组，大或相等则清空winPlayerList，加入新卡组的玩家
				logger.info("compareCardsToWinList CardUtil.compareValue listNew:" + newHandPower + " listold" + oldHandPower);
				int result = oldHandPower.compareTo(newHandPower);
				logger.info("compareCardsToWinList CardUtil.compareValue result:" + result);
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
	 * 判断游戏是否可以开始
	 * 
	 */
	public void checkStart(int milsecond) {
		try {
			Thread.sleep(milsecond);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 不在等待开始，则返回
		if (!(getGamestate().get() == 0)) {
			return;
		}
		// 有效玩家数大于等于最小开始游戏玩家则开始（筹码大于0的玩家为有效玩家）
		int activePlayerCount = 0;
		if (getWaitPlayers().size() >= getMinPlayers()) {
			for(PlayerVO player:getWaitPlayers()) {
				if(player.getBodyChips() > 0) {
					activePlayerCount++;

					if(activePlayerCount >= getMinPlayers()) {
						startGame();
						break;
					}
				}
			}
//			startGame();
		}
	}

	/**
	 * 为进入房间的用户分配筹码
	 * 
	 * @param
	 * @param player
	 */
	public void assignChipsForInRoom(PlayerVO player) {
		long takeChip = getMaxChips();
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
		retMsg.setMessage(getMaxChips());

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
		long takeChip = getMaxChips() - player.getBodyChips();
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

	/**
	 * 玩家弃牌
	 * 
	 * @param player
	 */
	public void fold(PlayerVO player) {
		// 房间状态游戏中
		if (player.getRoom().getGamestate().get() != 1) {
			return;
		}
		if (player.getSeatNum() != nextturn) {
			return;
		}
		try {
			// 检测到玩家操作，计时取消
			cancelTimer();
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
			TexasUtil.sendMsgToPlayerByRoom(this, JsonUtils.toJson(retMsg, RetMsg.class));
			// 将玩家移动到等待列表
			TexasUtil.removeIngamePlayer(player);
			int index = donePlayerList.indexOf(player.getSeatNum());
			// 玩家在donePlayerList，则移除
			if (index != -1) {
				donePlayerList.remove(index);
			}
			player.setNextRoundSkillAction("");
			player.setThisRoundSkillAction("");
			getWaitPlayers().add(player);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 判断下一步是否round结束，endgame或下个玩家操作nextturn
		endRoundOrNextTurn();
		// 记录日志
		PlayerOpt opt = new PlayerOpt();
		opt.setOptTime(DateUtil.nowDatetime());
		opt.setOptType("fold");// 操作类型（跟注、加注、弃牌、全下）
		opt.setRemark("");// 备注
		opt.setPlayerId(player.getId());
		opt.setSeatNum(player.getSeatNum());
		opts.add(opt);
	}

	/**
	 * 判断下一步是否round结束，endgame或下个玩家操作nextturn
	 */
	public void endRoundOrNextTurn() {
		// 判断是否可以结束本轮
		boolean roundEnd = checkRoundEnd();
		if (!roundEnd) {
			// 更新nextturn
			TexasUtil.updateNextTurn(this);
			// 发送轮到某玩家操作的消息
			sendNextTurnMessage();
		}
	}

	public void check(PlayerVO player, boolean isAllin) {
		// 房间状态游戏中
		if (player.getRoom().getGamestate().get() != 1) {
			return;
		}
		// 是否轮到该玩家操作
		if (player.getSeatNum() != nextturn) {
			return;
		}
		if(!isAllin) {
			// 玩家此次操作之前的本轮下注额
			long oldBetThisRound = 0;
			if (getBetRoundMap().get(player.getSeatNum()) != null) {
				oldBetThisRound = getBetRoundMap().get(player.getSeatNum());
			}
			// 小于最大下注，不能check
			if (oldBetThisRound < getRoundMaxBet()) {
				logger.info("can not check, bet:" + oldBetThisRound + " getRoundMaxBet:" + getRoundMaxBet());
				return;
			}
		}

		try {
			// 检测到玩家操作，计时取消
			cancelTimer();
			// 记录当前轮已经操作过的玩家
			if (!donePlayerList.contains(player.getSeatNum())) {
				player.setNextRoundSkillAction("");
				player.setThisRoundSkillAction("");
				donePlayerList.add(player.getSeatNum());
			}
			// 如果不需要通知其他玩家则直接判断下一轮 （场景：当前此玩家已All in）
			if(!isAllin) {
				endRoundOrNextTurn();
			}
			// 发送过牌消息给玩家
			String msg = JsonUtils.toJson(player, PlayerVO.class);
			RetMsg retMsg = new RetMsg();
			retMsg.setAction("onPlayerCheck");
			retMsg.setState(1);
			retMsg.setMessage(msg);
			TexasUtil.sendMsgToPlayerByRoom(this, JsonUtils.toJson(retMsg, RetMsg.class));

		} catch (Exception e) {
			e.printStackTrace();
		}
		// 判断下一步是否round结束，endgame或下个玩家操作nextturn
		endRoundOrNextTurn();
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
		if (playerOpt && player.getSeatNum() != nextturn) {
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
		opts.add(opt);

		// 玩家此次操作之前的本轮下注额
		long oldBetThisRound = 0;
		if (thisRoom.getBetRoundMap().get(player.getSeatNum()) != null) {
			oldBetThisRound = thisRoom.getBetRoundMap().get(player.getSeatNum());
		}
		// 无效下注额,1筹码不足
		if (chip <= 0 || chip > player.getBodyChips()) {
			logger.error("betchipIn error not enough chips:" + chip + " getBodyChips():" + player.getBodyChips());
			return false;
		}
		if (playerOpt) {
			// 2.在没有allin的情况下，如果不是跟注，则下注必须是大盲的整数倍
			if (chip < player.getBodyChips()) {
				//2.1跟注-- 不能小于之前下注,否则强制增加到跟注筹码，不够则allin
				if ((chip + oldBetThisRound) < thisRoom.getRoundMaxBet()) {
					logger.error("betchipIn error < getRoundMaxBet() chip:" + chip + "oldBetThisRound:"
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
//						logger.error("betchipIn error % bigbet != 0:" + chip + "oldBetThisRound:" + oldBetThisRound
//								+ ",max:" + thisRoom.getRoundMaxBet());
//						return false;
//					}
					// 加注必须大于2倍之前最大下注（增量加注规则暂时不考虑）
					logger.error("betchipIn error (totalBet > totalBet && totalBet < 2*RoundMaxBet) :" + chip + "oldBetThisRound:" + oldBetThisRound
						+ ",max:" + thisRoom.getRoundMaxBet());
					return false;
				}
			}

		}
		try {
			if (playerOpt) {
				// 检测到玩家操作，计时取消
				cancelTimer();
			}
			// 设置本轮最大加注
			if ((int) (chip + oldBetThisRound) > thisRoom.getRoundMaxBet()) {
				thisRoom.setRoundMaxBet((int) (chip + oldBetThisRound));
				// 加注额大于之前，则所有玩家重新加注
				donePlayerList.clear();
			}
			// 总奖池
			thisRoom.setBetAmount(getBetAmount() + chip);
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
			endRoundOrNextTurn();
		}
		return true;
	}

	/**
	 * 判断本轮是否可以结束
	 */
	public boolean checkRoundEnd() {
		// 判断本轮是否可以结束
		boolean canEndRound = true;

		// 当前还有筹码的玩家数量
		int haveBodyChipsCount = 0;

		if (getIngamePlayers().size() == 1) {
			// 结算游戏
			logger.info("only one IngamePlayers endgame start");
			endGame();
			return true;
		}
		// 将已经allin的玩家加入已操作列表
		for (PlayerVO p : getIngamePlayers()) {
			if (p.getBodyChips() == 0) {
				donePlayerList.add(p.getSeatNum());
			}
		}
		// 所有人都已经操作过
		if (donePlayerList.size() >= getIngamePlayers().size()) {
			long betMax = 0l;// 以最大下注作为参照筹码
			for (int i = 0; i < getIngamePlayers().size(); i++) {
				PlayerVO p = getIngamePlayers().get(i);
				// 为betMax赋初始值
				if (getBetMap().get(p.getSeatNum()) != null && getBetMap().get(p.getSeatNum()) > betMax) {
					betMax = getBetMap().get(p.getSeatNum());
				}
			}
			logger.info("checkRoundEnd betMax:" + betMax);
			for (int i = 0; i < getIngamePlayers().size(); i++) {
				PlayerVO p = getIngamePlayers().get(i);
				// 已经弃牌的排除在外
				if (p == null || p.isFold()) {
					continue;
				}
				if (getBetMap().get(p.getSeatNum()) == null) {
					// 存在没有下注的玩家
					logger.info("checkRoundEnd no bet seatNum:" + p.getSeatNum());
					canEndRound = false;
					break;
				}
				// 没有allin的玩家中
				if (p.getBodyChips() > 0) {
					haveBodyChipsCount ++;
					// 没有弃牌的玩家中，存在下注额度小于betMax，则本轮不能结束
					if (betMax > getBetMap().get(p.getSeatNum())) {
						logger.info("not allin bet<maxbet seatNum:" + p.getSeatNum() + " bet "
								+ getBetMap().get(p.getSeatNum()) + " betMax:" + betMax);
						canEndRound = false;
						break;
					}
				}
			}
		} else {
			logger.info("checkRoundEnd getDonePlayerList().size() < getIngamePlayers().size()");
			canEndRound = false;
		}

		// 如果下一个可以操作的玩家无法更新，游戏结束
		int turn = TexasUtil.getNextSeatNum(nextturn, this);
		if (turn == nextturn) {
			canEndRound = true;
		}
		if (canEndRound) {
			logger.info("canEndRound = true");
			// 第二轮最低加注设为0
			setRoundMaxBet(0);
			// 开始新的一轮
			// 清除玩家本轮下注
			betRoundMap.clear();
			// 清空操作过的玩家列表
			donePlayerList.clear();
			// 已经无法操作的玩家加入DonePlayerList
			for (int i = 0; i < getIngamePlayers().size(); i++) {
				PlayerVO p = getIngamePlayers().get(i);
				if (p != null && p.getBodyChips() <= 0) {
					donePlayerList.add(p.getSeatNum());
				}
			}
			// 如果公共牌等于5张,且可操作玩家数小于总玩家数
			if (communityCards.size() < 5 && donePlayerList.size() < getIngamePlayers().size()) {
				// 更新nextturn为每轮第一个人
				PlayerVO roundturnp = TexasUtil.getPlayerBySeatNum(roundturn, getIngamePlayers());
				if (roundturnp == null || roundturnp.isFold() || roundturnp.getBodyChips() == 0) {
					roundturn = TexasUtil.getNextSeatNum(roundturn, this);
				}
				setNextturn(roundturn);

				// 如果只有一个玩家还有筹码则下一轮自动过牌（全场ALL IN情况下）
				if(haveBodyChipsCount <= 1) {
					sendNextTurnAutoCheckMessage();
				} else {
					// 发送轮到某玩家操作的消息
					sendNextTurnMessage();
				}

				// 发公共牌
				int assignCardCount = communityCards.size() == 0 ? 3 : 1;
				TexasUtil.assignCommonCardByNum(this, assignCardCount);

			} else {
				// 结算游戏
				endGame();
			}
		}
		return canEndRound;
	}

	private TimerTask timerTask;

	/**
	 * 开始计时
	 */
	public void startTimer(Room room) {
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
		timer.purge();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				try {
					// 弃牌or check
					PlayerVO p = TexasUtil.getPlayerBySeatNum(getNextturn(), getIngamePlayers());
					if (p != null) {
						// 帮其执行弃牌操作
						logger.info(p.getUserName() + " time is up,fold");
						fold(p);
					} else {
						if (room.getGamestate().get() == 1) {
							// 更新本应操作的玩家
							TexasUtil.updateNextTurn(room);
							// 发送轮到某玩家操作的消息
							sendNextTurnMessage();
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
		};
		// 考虑到网络传输延时，后台计时器多给与一个500毫秒的缓冲时间
		timer.schedule(timerTask, getOptTimeout() + 500);
	}

	/**
	 * 发送轮到某玩家操作的消息
	 */
	public void sendNextTurnMessage() {
//		String message = getNextturn() + "";
		RetMsg retMsg = new RetMsg();
		// 告诉前台轮到某个玩家操作了
		retMsg.setAction("onPlayerTurn");
		retMsg.setState(1);
		retMsg.setMessage(getNextturn() + "");
		String msg = JsonUtils.toJson(retMsg, RetMsg.class);
		// 轮到下一家操作，并开始计时
		startTimer(this);
		TexasUtil.sendMsgToPlayerByRoom(this, msg);
	}

	/**
	 * 发送轮到某玩家操作的消息,自动过牌
	 */
	public void sendNextTurnAutoCheckMessage() {
//		String message = getNextturn() + "";
		RetMsg retMsg = new RetMsg();
		// 告诉前台轮到某个玩家操作了
		retMsg.setAction("onPlayerTurnAutoCheck");
		retMsg.setState(1);
		retMsg.setMessage(getNextturn() + "");
		String msg = JsonUtils.toJson(retMsg, RetMsg.class);
		// 轮到下一家操作，并开始计时
		startTimer(this);
		TexasUtil.sendMsgToPlayerByRoom(this, msg);
	}

	/**
	 * 判断是否可以结束游戏
	 */
	public boolean checkEnd() {
		int playerCount = 0;
		for (PlayerVO p : getIngamePlayers()) {
			if (!p.isFold() && p.getBodyChips() != 0) {
				playerCount++;
			}
		}
		if (playerCount < 2) {
			this.endGame();
			return true;
		}
		logger.info(playerCount + "断线后人数大于1");
		return false;
	}

	// ThreeCardRoom房间中实现
	public void loseCompareCards(PlayerVO player) {
	}

	/**
	 * 取消计时
	 */
	public boolean cancelTimer() {
		boolean result = false;
		if (timerTask != null) {
			result = timerTask.cancel();
			timerTask = null;
		}
		return result;
	}

	public int getRestBetweenGame() {
		return restBetweenGame;
	}

	public void setRestBetweenGame(int restBetweenGame) {
		this.restBetweenGame = restBetweenGame;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getMaxChips() {
		return maxChips;
	}

	public void setMaxChips(int maxChips) {
		this.maxChips = maxChips;
	}

	public int getMinChips() {
		return minChips;
	}

	public void setMinChips(int minChips) {
		this.minChips = minChips;
	}

	public int getBigBet() {
		return bigBet;
	}

	public void setBigBet(int bigBet) {
		this.bigBet = bigBet;
	}

	public int getSmallBet() {
		return smallBet;
	}

	public void setSmallBet(int smallBet) {
		this.smallBet = smallBet;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public int getMinPlayers() {
		return minPlayers;
	}

	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}

	public int getDealer() {
		return dealer;
	}

	public void setDealer(int dealer) {
		this.dealer = dealer;
	}

	public AtomicInteger getGamestate() {
		return gamestate;
	}

	public void setGamestate(AtomicInteger gamestate) {
		this.gamestate = gamestate;
	}

	public int getRoomstate() {
		return roomstate;
	}

	public void setRoomstate(int roomstate) {
		this.roomstate = roomstate;
	}

	public List<PlayerVO> getWaitPlayers() {
		return waitPlayers;
	}

	public void setWaitPlayers(List<PlayerVO> waitPlayers) {
		this.waitPlayers = waitPlayers;
	}

	public List<PlayerVO> getIngamePlayers() {
		return ingamePlayers;
	}

	public void setIngamePlayers(List<PlayerVO> ingamePlayers) {
		this.ingamePlayers = ingamePlayers;
	}

	public List<Card> getCardList() {
		return cardList;
	}

	public void setCardList(List<Card> cardList) {
		this.cardList = cardList;
	}

	public List<String> getCommunityCards() {
		return communityCards;
	}

	public void setCommunityCards(List<String> communityCards) {
		this.communityCards = communityCards;
	}

	public List<Card> getCalcCommunityCards() {
		return calcCommunityCards;
	}

	public void setCalcCommunityCards(List<Card> calcCommunityCards) {
		this.calcCommunityCards = calcCommunityCards;
	}

	public String getCurrentRound() {
		return currentRound;
	}

	public void setCurrentRound(String currentRound) {
		this.currentRound = currentRound;
	}

	public int getNextturn() {
		return nextturn;
	}

	public void setNextturn(int nextturn) {
		this.nextturn = nextturn;
	}

	public Stack<Integer> getFreeSeatStack() {
		return freeSeatStack;
	}

	public void setFreeSeatStack(Stack<Integer> freeSeatStack) {
		this.freeSeatStack = freeSeatStack;
	}

	public long getBetAmount() {
		return betAmount;
	}

	public void setBetAmount(long betAmount) {
		this.betAmount = betAmount;
	}

	public Map<Integer, Long> getBetRoundMap() {
		return betRoundMap;
	}

	public void setBetRoundMap(Map<Integer, Long> betRoundMap) {
		this.betRoundMap = betRoundMap;
	}

	public int getOptTimeout() {
		return optTimeout;
	}

	public void setOptTimeout(int optTimeout) {
		this.optTimeout = optTimeout;
	}

	public Map<Integer, Long> getWinPlayersMap() {
		return winPlayersMap;
	}

	public void setWinPlayersMap(Map<Integer, Long> winPlayersMap) {
		this.winPlayersMap = winPlayersMap;
	}

	public int getSmallBetSeatNum() {
		return smallBetSeatNum;
	}

	public void setSmallBetSeatNum(int smallBetSeatNum) {
		this.smallBetSeatNum = smallBetSeatNum;
	}

	public int getBigBetSeatNum() {
		return bigBetSeatNum;
	}

	public void setBigBetSeatNum(int bigBetSeatNum) {
		this.bigBetSeatNum = bigBetSeatNum;
	}

	public Map<Integer, List<String>> getFinalCardsMap() {
		return finalCardsMap;
	}

	public void setFinalCardsMap(Map<Integer, List<String>> finalCardsMap) {
		this.finalCardsMap = finalCardsMap;
	}

	public Map<Integer, HandPower> getCalcFinalCardsMap() {
		return calcFinalCardsMap;
	}

	public void setCalcFinalCardsMap(Map<Integer, HandPower> calcFinalCardsMap) {
		this.calcFinalCardsMap = calcFinalCardsMap;
	}

	public int getRoundMaxBet() {
		return roundMaxBet;
	}

	public void setRoundMaxBet(int roundMaxBet) {
		this.roundMaxBet = roundMaxBet;
	}

	public Map<Integer, Long> getBetMap() {
		return betMap;
	}

	public void setBetMap(Map<Integer, Long> betMap) {
		this.betMap = betMap;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<Card> getCardListWhole() {
		return cardListWhole;
	}

	public void setCardListWhole(List<Card> cardListWhole) {
		this.cardListWhole = cardListWhole;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public TimerTask getTimerTask() {
		return timerTask;
	}

	public void setTimerTask(TimerTask timerTask) {
		this.timerTask = timerTask;
	}

	public List<SkillDictionaryVO> getSkillCards() {
		return skillCards;
	}

	public void setSkillCards(List<SkillDictionaryVO> skillCards) {
		this.skillCards = skillCards;
	}

	public int getSkillTrapCount() {
		return skillTrapCards!=null?skillTrapCards.size():0;
	}

	public void setSkillTrapCount(int skillTrapCount) {
		this.skillTrapCount = skillTrapCount;
	}

	public List<SkillDictionaryVO> getSkillTrapCards() {
		return skillTrapCards;
	}

	public void setSkillTrapCards(List<SkillDictionaryVO> skillTrapCards) {
		this.skillTrapCards = skillTrapCards;
	}

//	public static void main(String[] args) {
//		testSumBetPool();
//		// testCompareCardsToWinList();
//	}
//
//	public static void testSumBetPool() {
//		// 奖池列表
//		List<BetPool> betPoolList = new ArrayList<BetPool>();
//		Map<Integer, Long> betMap = new LinkedHashMap<>();
//		List<Player> ingamePlayers = new ArrayList<Player>();
//		for (int i = 0; i < 10; i++) {
//			Player p = new Player();
//			p.setSeatNum(i);
//			betMap.put(i, 100l);
//			if (i < 3) {
//				betMap.put(i, 2000l + i * 100);
//			} else {
//				betMap.put(i, 200l + i * 10);
//			}
//			ingamePlayers.add(p);
//		}
//		sumBetPoolList(betPoolList, betMap, ingamePlayers);
//		logger.info(JsonUtils.toJson(betMap, betMap.getClass()));
//		for (BetPool pool : betPoolList) {
//			logger.info("pool:" + pool.getBetSum());
//			logger.info("size:" + pool.getBetPlayerList().size());
//			logger.info(JsonUtils.toJson(pool.getBetPlayerList(), pool.getBetPlayerList().getClass()));
//		}
//		logger.info("poolSize:" + betPoolList.size());
//	}
//
//	public static void testCompareCardsToWinList() {
//		List<Player> poolPlayers = new ArrayList<>();
//		Map<Integer, List<Integer>> finalCardsMap = new HashMap<Integer, List<Integer>>();
//
//		poolPlayers.add(null);
//		for (int i = 1; i < 6; i++) {
//			Player p = new Player();
//			p.setSeatNum(i);
//			p.setFold(false);
//			poolPlayers.add(p);
//		}
//		finalCardsMap.put(1, new ArrayList<Integer>(Arrays.asList(24, 28, 39, 40, 51, 1)));
//		finalCardsMap.put(2, new ArrayList<Integer>(Arrays.asList(36, 39, 28, 40, 51, 2)));
//		finalCardsMap.put(3, new ArrayList<Integer>(Arrays.asList(40, 39, 32, 28, 26, 5)));
//		finalCardsMap.put(4, new ArrayList<Integer>(Arrays.asList(50, 51, 40, 41, 39, 3)));
//		finalCardsMap.put(5, new ArrayList<Integer>(Arrays.asList(12, 14, 39, 40, 51, 2)));
//		// "finalCardsMap\":{\"1\":,\"2\":[],\"3\":[],\"4\":[],\"5\":[]}
//		List<Player> winPlayerList = compareCardsToWinList(poolPlayers, finalCardsMap);
//		logger.info(JsonUtils.toJson(poolPlayers, poolPlayers.getClass()));
//		logger.info(JsonUtils.toJson(winPlayerList, winPlayerList.getClass()));
//	}
}
