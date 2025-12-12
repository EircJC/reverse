package com.yulink.texas.server.common.entity;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

public class RetMsg implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * 类型 
	 * onPlayerSit 玩家入局
	 * onPlayerUp 玩家离开
	 * onGameStart 游戏开始
	 * onGameEnd 游戏结束
	 * onPlayerMove 其他玩家操作
	 * onMessage 其他玩家发消息
	 * onUseSkill 使用技能卡
	 * 
	 */
	@Expose
	private String action;
	/**
	 * 状态
	 * 0失败
	 * 1成功
	 */
	@Expose
	private int state;
	/**
	 * 消息
	 */
	@Expose
	private Object message;

	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public Object getMessage() {
		return message;
	}
	public void setMessage(Object message) {
		this.message = message;
	}

}
