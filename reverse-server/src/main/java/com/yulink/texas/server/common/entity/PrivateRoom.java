package com.yulink.texas.server.common.entity;

import com.google.gson.annotations.Expose;
import com.yulink.texas.server.common.room.Room;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 私有房间信息（包含自己的手牌）
 * 
 * @author Ming
 *
 */
public class PrivateRoom implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 房间类型
	 * 0：普通德扑房
	 * 1：系统版卡牌德扑
	 * 2：玩家自定义卡组卡牌德扑
	 */
	@Expose
	private int type;

	@Expose
	private String roomNo;

	@Expose
	private List<String> handPokers;

	/**
	 * 公共牌
	 */
	@Expose
	protected List<String> communityCards = new ArrayList<String>();
	/**
	 * 奖池,下注总额
	 */
	@Expose
	protected long betAmount;
	/**
	 * 操作超时时间，单位毫秒（玩家在规定时间内没有完成操作，则系统自动帮其弃牌）
	 */
	@Expose
	private int optTimeout = 10000;
	/**
	 * 大盲下注筹码
	 */
	@Expose
	private int bigBet;
	@Expose
	private int roundMaxBet;
	@Expose
	private int nextturn;
	@Expose
	private int dealer;

	/**
	 * 每轮第一个行动的玩家
	 */
	protected int roundturn = 0;

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
	/**
	 * 在一回合中，每个玩家下的注[座位号，本轮下注额]
	 */
	@Expose
	protected Map<Integer, Long> betRoundMap = new LinkedHashMap<>();

	/**
	 * 玩家能量
	 */
	@Expose
	protected int power;

	/**
	 * 玩家技能卡列表
	 */
	@Expose
	protected List<SkillDictionaryVO> playerSkillCards = new ArrayList<>();

	/**
	 * 玩家持卡概览
	 */
	@Expose
	protected List<PlayerSkillSummary> playerSkillSummaryList = new CopyOnWriteArrayList<>();

	/**
	 * 当前场上陷阱数量
	 */
	@Expose
	protected int skillTrapCount = 0;

	/**
	 * 玩家当前防御buff数量
	 */
	@Expose
	private int playerSkillDefenseCount;

	/**
	 * 当前使用技能名称
	 */
	@Expose
	protected String skillDictionaryName;

	/**
	 * 当前使用技能描述
	 */
	@Expose
	protected String skillDescription;

	/**
	 * 当前使用技能限制条件
	 */
	@Expose
	protected String skillConstrains;

	/**
	 * 当前使用技能目标玩家
	 */
	@Expose
	protected String destPlayerName;

	/**
	 * 当前使用技能玩家名称
	 */
	@Expose
	protected String srcPlayerName;

	/**
	 * 当前使用技能第三方承受玩家
	 */
	@Expose
	protected String thirdDestPlayerName;
	/**
	 * 当前回合是否可以使用技能
	 */
	@Expose
	protected boolean isSkillUsed;

	/**
	 * 下回合玩家指定动作 (通过技能产生的默认动作)
	 * checkOrCall
	 * fold
	 * allin
	 */
	@Expose
	private String nextRoundSkillAction;

	/**
	 * 本回合玩家指定动作 (通过陷阱技能产生的默认动作，会覆盖当前玩家真是操作)
	 * checkOrCall
	 * fold
	 * allin
	 */
	@Expose
	private String thisRoundSkillAction;

	@Expose
	private String currentRound;

	/**
	 * 使用技能时被触发目标的防御技能(前端显示)
	 */
	@Expose
	protected SkillDictionaryVO skillDefenseCard;

	/**
	 * 使用技能时被触发场上的陷阱技能(前端显示)
	 */
	@Expose
	protected SkillDictionaryVO skillTrapCard;

	public void setRoom(Room room) {
		setRoomNo(room.getRoomNo());
		setBetRoundMap(room.getBetMap());
		setBigBet(room.getBigBet());
		setIngamePlayers(room.getIngamePlayers());
		setWaitPlayers(room.getWaitPlayers());
		setNextturn(room.getNextturn());
		setCommunityCards(room.getCommunityCards());
		setBetAmount(room.getBetAmount());
		setOptTimeout(room.getOptTimeout());
		setRoundMaxBet(room.getRoundMaxBet());
		setType(room.getType());
		setSkillTrapCount(room.getSkillTrapCount());
		setCurrentRound(room.getCurrentRound());
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getRoomNo() {
		return roomNo;
	}

	public void setRoomNo(String roomNo) {
		this.roomNo = roomNo;
	}

	public int getRoundMaxBet() {
		return roundMaxBet;
	}

	public void setRoundMaxBet(int roundMaxBet) {
		this.roundMaxBet = roundMaxBet;
	}
	public int getRoundturn() {
		return roundturn;
	}

	public void setRoundturn(int roundturn) {
		this.roundturn = roundturn;
	}

	public List<String> getCommunityCards() {
		return communityCards;
	}

	public void setCommunityCards(List<String> communityCards) {
		this.communityCards = communityCards;
	}

	public long getBetAmount() {
		return betAmount;
	}

	public void setBetAmount(long betAmount) {
		this.betAmount = betAmount;
	}

	public int getOptTimeout() {
		return optTimeout;
	}

	public void setOptTimeout(int optTimeout) {
		this.optTimeout = optTimeout;
	}

	public int getDealer() {
		return dealer;
	}

	public void setDealer(int dealer) {
		this.dealer = dealer;
	}

	public int getNextturn() {
		return nextturn;
	}

	public void setNextturn(int nextturn) {
		this.nextturn = nextturn;
	}

	public int getBigBet() {
		return bigBet;
	}

	public void setBigBet(int bigBet) {
		this.bigBet = bigBet;
	}

	public Map<Integer, Long> getBetRoundMap() {
		return betRoundMap;
	}

	public void setBetRoundMap(Map<Integer, Long> betRoundMap) {
		this.betRoundMap = betRoundMap;
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

	public List<String> getHandPokers() {
		return handPokers;
	}

	public void setHandPokers(List<String> handPokers) {
		this.handPokers = handPokers;
	}

	public List<SkillDictionaryVO> getPlayerSkillCards() {
		return playerSkillCards;
	}

	public void setPlayerSkillCards(List<SkillDictionaryVO> playerSkillCards) {
		this.playerSkillCards = playerSkillCards;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public List<PlayerSkillSummary> getPlayerSkillSummaryList() {
		return playerSkillSummaryList;
	}

	public void setPlayerSkillSummaryList(
		List<PlayerSkillSummary> playerSkillSummaryList) {
		this.playerSkillSummaryList = playerSkillSummaryList;
	}

	public int getSkillTrapCount() {
		return skillTrapCount;
	}

	public void setSkillTrapCount(int skillTrapCount) {
		this.skillTrapCount = skillTrapCount;
	}

	public int getPlayerSkillDefenseCount() {
		return playerSkillDefenseCount;
	}

	public void setPlayerSkillDefenseCount(int playerSkillDefenseCount) {
		this.playerSkillDefenseCount = playerSkillDefenseCount;
	}

	public String getSkillDictionaryName() {
		return skillDictionaryName;
	}

	public void setSkillDictionaryName(String skillDictionaryName) {
		this.skillDictionaryName = skillDictionaryName;
	}

	public String getSkillDescription() {
		return skillDescription;
	}

	public void setSkillDescription(String skillDescription) {
		this.skillDescription = skillDescription;
	}

	public String getSkillConstrains() {
		return skillConstrains;
	}

	public void setSkillConstrains(String skillConstrains) {
		this.skillConstrains = skillConstrains;
	}

	public String getDestPlayerName() {
		return destPlayerName;
	}

	public void setDestPlayerName(String destPlayerName) {
		this.destPlayerName = destPlayerName;
	}

	public String getSrcPlayerName() {
		return srcPlayerName;
	}

	public void setSrcPlayerName(String srcPlayerName) {
		this.srcPlayerName = srcPlayerName;
	}

	public String getThirdDestPlayerName() {
		return thirdDestPlayerName;
	}

	public void setThirdDestPlayerName(String thirdDestPlayerName) {
		this.thirdDestPlayerName = thirdDestPlayerName;
	}

	public boolean isSkillUsed() {
		return isSkillUsed;
	}

	public void setSkillUsed(boolean skillUsed) {
		isSkillUsed = skillUsed;
	}

	public String getNextRoundSkillAction() {
		return nextRoundSkillAction;
	}

	public void setNextRoundSkillAction(String nextRoundSkillAction) {
		this.nextRoundSkillAction = nextRoundSkillAction;
	}

	public String getThisRoundSkillAction() {
		return thisRoundSkillAction;
	}

	public void setThisRoundSkillAction(String thisRoundSkillAction) {
		this.thisRoundSkillAction = thisRoundSkillAction;
	}

	public String getCurrentRound() {
		return currentRound;
	}

	public void setCurrentRound(String currentRound) {
		this.currentRound = currentRound;
	}

	public SkillDictionaryVO getSkillDefenseCard() {
		return skillDefenseCard;
	}

	public void setSkillDefenseCard(SkillDictionaryVO skillDefenseCard) {
		this.skillDefenseCard = skillDefenseCard;
	}

	public SkillDictionaryVO getSkillTrapCard() {
		return skillTrapCard;
	}

	public void setSkillTrapCard(
		SkillDictionaryVO skillTrapCard) {
		this.skillTrapCard = skillTrapCard;
	}
}
