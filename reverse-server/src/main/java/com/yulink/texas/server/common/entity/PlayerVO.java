package com.yulink.texas.server.common.entity;

import com.google.gson.annotations.Expose;
import com.yulink.texas.common.card.Card;
import com.yulink.texas.server.common.room.Room;
import io.netty.channel.Channel;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/7
 * @Copyright (c) bitmain.com All Rights Reserved
 */
public class PlayerVO extends BetPlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    @Expose
    private String userName;
    /**
     * 昵称
     */
    @Expose
    private String nickName;
    /**
     * 邮箱
     */
    @Expose
    private String email;
    /**
     * 手机
     */
    @Expose
    private String phone;
    /**
     * 用户密码
     */
    @Expose(serialize = false, deserialize = true)
    private String userpwd;
    /**
     * 用户Session
     */
//    @Expose(serialize = false, deserialize = false)
//    private Session session;

    private Channel channel;
    /**
     * 头像
     */
    @Expose
    private String picLogo;
    /**
     * 筹码(用户所拥有的总筹码)
     */
    @Expose
    private long chips;

    /**
     * 用户状态
     */
    @Expose(serialize = false, deserialize = false)
    private int state;
    /**
     * 注册日期
     */
    @Expose(serialize = false, deserialize = false)
    private String regdate;

    /**
     * 用户所在房间
     */
    @Expose(serialize = false, deserialize = false)
    private Room room;
    /**
     * 是否已经弃牌
     */
    @Expose
    private boolean isFold = true;
    /**
     * 手牌
     */
    private Card[] handPokers;

    /**
     * 玩家显示的卡牌
     */
    private List<SkillDictionaryVO> playerSkillCards = new CopyOnWriteArrayList<SkillDictionaryVO>();

    /**
     * 玩家当前生效的防御技能卡
     */
    private List<SkillDictionaryVO> skillDefenseCards = new CopyOnWriteArrayList<SkillDictionaryVO>();

    /**
     * 玩家持有卡牌数量
     */
    @Expose
    private int playerSkillCardsCount;

    /**
     * 玩家当前防御buff数量
     */
    @Expose
    private int playerSkillDefenseCount;

    /**
     * 卡牌总列表(洗过牌之后的，只限于本局)
     */
    private List<SkillDictionaryVO> skillCards = new CopyOnWriteArrayList<SkillDictionaryVO>();

    /**
     * 能量点
     */
    @Expose
    private int power;

    /**
     * 用于额外计算的能量，如额外消耗的和减少的
     */
    private int extPower;

    /**
     * 停止发放技能卡回合数（迟缓、高级迟缓等技能产生）
     */
    @Expose
    private int stopGetSkillCardRound;

    /**
     * 陷阱躲避次数buff
     */
    private int avoidTrapCount;

    /**
     * 当前回合是否可以使用技能
     */
    @Expose
    protected boolean isSkillUsed;

    /**
     * 停止使用技能卡回合数（冻结、全场静默、孤注一掷等技能产生）
     */
    @Expose
    private int stopUseSkillCardRound;

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

    /**
     * 玩家退出房间倒计时开始（玩家筹码输光时的时间戳），补充完筹码则该字段清空
     */
    private long exitRoomCountDownStartTime = 0;

    /**
     * 玩家退出房间倒计时间隔：10s；
     * 如果玩家在 now >= exitRoomCountDownStartTime + exitRoomCountDownInterval 则退出房间
     */
    private long exitRoomCountDownInterval = 10000;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserpwd() {
        return userpwd;
    }

    public void setUserpwd(String userpwd) {
        this.userpwd = userpwd;
    }

//    public Session getSession() {
//        return session;
//    }
//
//    public void setSession(Session session) {
//        this.session = session;
//    }

    // 添加getter和setter方法
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    public String getPicLogo() {
        return picLogo;
    }

    public void setPicLogo(String picLogo) {
        this.picLogo = picLogo;
    }

    public long getChips() {
        return chips;
    }

    public void setChips(long chips) {
        this.chips = chips;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getRegdate() {
        return regdate;
    }

    public void setRegdate(String regdate) {
        this.regdate = regdate;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Card[] getHandPokers() {
        return handPokers;
    }

    public void setHandPokers(Card[] handPokers) {
        this.handPokers = handPokers;
    }

    public boolean isFold() {
        return isFold;
    }

    public void setFold(boolean fold) {
        this.isFold = fold;
    }

    public List<SkillDictionaryVO> getPlayerSkillCards() {
        return playerSkillCards;
    }

    public void setPlayerSkillCards(List<SkillDictionaryVO> playerSkillCards) {
        this.playerSkillCards = playerSkillCards;
    }

    public List<SkillDictionaryVO> getSkillDefenseCards() {
        return skillDefenseCards;
    }

    public void setSkillDefenseCards(List<SkillDictionaryVO> skillDefenseCards) {
        this.skillDefenseCards = skillDefenseCards;
    }

    public int getPlayerSkillCardsCount() {
        return playerSkillCardsCount;
    }

    public void setPlayerSkillCardsCount(int playerSkillCardsCount) {
        this.playerSkillCardsCount = playerSkillCardsCount;
    }

    public int getPlayerSkillDefenseCount() {
        return skillDefenseCards!=null?skillDefenseCards.size():0 + avoidTrapCount;
    }

    public void setPlayerSkillDefenseCount(int playerSkillDefenseCount) {
        this.playerSkillDefenseCount = playerSkillDefenseCount;
    }

    public List<SkillDictionaryVO> getSkillCards() {
        return skillCards;
    }

    public void setSkillCards(List<SkillDictionaryVO> skillCards) {
        this.skillCards = skillCards;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getExtPower() {
        return extPower;
    }

    public void setExtPower(int extPower) {
        this.extPower = extPower;
    }

    public int getStopGetSkillCardRound() {
        return stopGetSkillCardRound;
    }

    public void setStopGetSkillCardRound(int stopGetSkillCardRound) {
        this.stopGetSkillCardRound = stopGetSkillCardRound;
    }

    public int getAvoidTrapCount() {
        return avoidTrapCount;
    }

    public void setAvoidTrapCount(int avoidTrapCount) {
        this.avoidTrapCount = avoidTrapCount;
    }

    public boolean isSkillUsed() {
        return isSkillUsed;
    }

    public void setSkillUsed(boolean skillUsed) {
        isSkillUsed = skillUsed;
    }

    public int getStopUseSkillCardRound() {
        return stopUseSkillCardRound;
    }

    public void setStopUseSkillCardRound(int stopUseSkillCardRound) {
        this.stopUseSkillCardRound = stopUseSkillCardRound;
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

    public long getExitRoomCountDownStartTime() {
        return exitRoomCountDownStartTime;
    }

    public void setExitRoomCountDownStartTime(long exitRoomCountDownStartTime) {
        this.exitRoomCountDownStartTime = exitRoomCountDownStartTime;
    }

    public long getExitRoomCountDownInterval() {
        return exitRoomCountDownInterval;
    }

    public void setExitRoomCountDownInterval(long exitRoomCountDownInterval) {
        this.exitRoomCountDownInterval = exitRoomCountDownInterval;
    }
}
