package com.yulink.texas.server.common.entity;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/18
 * @Copyright (c) bitmain.com All Rights Reserved
 */
public class PlayerSkillSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    @Expose
    private String id;

    @Expose
    private String username;

    /**
     * 头像
     */
    @Expose
    private String picLogo;

    /**
     * 能量点
     */
    @Expose
    private int power;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPicLogo() {
        return picLogo;
    }

    public void setPicLogo(String picLogo) {
        this.picLogo = picLogo;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getPlayerSkillCardsCount() {
        return playerSkillCardsCount;
    }

    public void setPlayerSkillCardsCount(int playerSkillCardsCount) {
        this.playerSkillCardsCount = playerSkillCardsCount;
    }

    /**
     * 玩家持有卡牌数量
     */
    @Expose
    private int playerSkillCardsCount;


}
