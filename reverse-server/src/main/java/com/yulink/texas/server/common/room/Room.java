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
//	public GameLog gameLog = new GameLog();
	public List<PlayerOpt> opts = new ArrayList<PlayerOpt>();
	/**
	 * 房间编号
	 */
	@Expose
	private String roomNo;
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

	public TimerTask timerTask;

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
	public List<PlayerVO> ingamePlayers = new CopyOnWriteArrayList<PlayerVO>();
//	/**
//	 * 一局的牌组，随着分发手牌和公共牌会进行remove操作
//	 */
	public List<Card> cardList = new CopyOnWriteArrayList<>();

	/**
	 * 完整牌组，初始赋值cardList值，不进行remove操作
	 */
	public List<Card> cardListWhole = new CopyOnWriteArrayList<>();
	/**
	 * 公共牌
	 */
	@Expose
	public List<String> communityCards = new CopyOnWriteArrayList<String>();

	/**
	 * 公共牌 用于计算，不给用户展示
	 */
	public List<Card> calcCommunityCards = new CopyOnWriteArrayList<Card>();

	/**
	 * 当前所处回合
	 * P: 翻牌前
	 * F: 翻牌
	 * T: 转牌
	 * R: 河牌
	 */
	@Expose
	public String currentRound;

	/**
	 * 奖池,下注总额
	 */
	@Expose
	public long betAmount;
	/**
	 * 每个玩家下的注，玩家和其本局游戏下注的总额
	 */
	public Map<Integer, Long> betMap = new LinkedHashMap<>();

	/**
	 * 在一回合中，每个玩家下的注[座位号，本轮下注额]
	 */
	@Expose
	public Map<Integer, Long> betRoundMap = new LinkedHashMap<>();

	/**
	 * 操作过的玩家列表
	 */
	public List<Integer> donePlayerList = new CopyOnWriteArrayList<Integer>();

	/**
	 * 下一个行动的玩家id
	 */
	@Expose
	public volatile int nextturn = 0;// next player
	/**
	 * 每轮第一个行动的玩家
	 */
	public volatile int roundturn = 0;
	/**
	 * 本轮游戏玩家下的最大注倍数，第一轮为1，一共3种，1,2,4
	 */
	public int roundMaxBet = bigBet;
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
	public Map<Integer, List<String>> finalCardsMap = new LinkedHashMap<Integer, List<String>>();

	/**
	 * 游戏中玩家成手牌列表(不展示给用户) 用于计算结果
	 */
	public Map<Integer, HandPower> calcFinalCardsMap = new LinkedHashMap<Integer, HandPower>();
	/**
	 * 最后亮牌玩家手牌列表
	 */
	@Expose
	public Map<Integer, List<String>> handCardsMap = new LinkedHashMap<Integer, List<String>>();
	/**
	 * 所有获胜玩家列表
	 */
	@Expose
	public Map<Integer, Long> winPlayersMap = new LinkedHashMap<Integer, Long>();

	/**
	 * 系统卡牌列表（具体到玩家的时候该列表需要单独洗牌并填充）
	 */
	public List<SkillDictionaryVO> skillCards = new CopyOnWriteArrayList<>();

	/**
	 * 当前场上陷阱数量
	 */
	@Expose
	public int skillTrapCount = 0;

	/**
	 * 当前场上陷阱列表
	 */
	public List<SkillDictionaryVO>  skillTrapCards = new CopyOnWriteArrayList<>();





	// ThreeCardRoom房间中实现
	public void loseCompareCards(PlayerVO player) {
	}

	public int getRestBetweenGame() {
		return restBetweenGame;
	}

	public void setRestBetweenGame(int restBetweenGame) {
		this.restBetweenGame = restBetweenGame;
	}

	public String getRoomNo() {
		return roomNo;
	}

	public void setRoomNo(String roomNo) {
		this.roomNo = roomNo;
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
