package com.yulink.texas.server.common.utils;

import com.google.gson.annotations.Expose;
import com.yulink.texas.server.common.entity.PlayerVO;
import java.util.ArrayList;
import java.util.List;

/**
 * 奖池计算
 * 
 * @author lixiaoran
 *
 */
public class BetPool {
	/**
	 * 该分池总金额
	 */
	@Expose
	private long betSum;
	/**
	 * 该分池的玩家列表
	 */
	@Expose
	private List<PlayerVO> betPlayerList = new ArrayList<>();

	public long getBetSum() {
		return betSum;
	}

	public void setBetSum(long betSum) {
		this.betSum = betSum;
	}

	public List<PlayerVO> getBetPlayerList() {
		return betPlayerList;
	}

	public void setBetPlayerList(List<PlayerVO> betPlayerList) {
		this.betPlayerList = betPlayerList;
	}
}
