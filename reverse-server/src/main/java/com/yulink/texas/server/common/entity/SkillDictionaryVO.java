package com.yulink.texas.server.common.entity;

import com.google.gson.annotations.Expose;
import com.yulink.texas.core.domain.SkillDictionary;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/16
 * @Copyright (c) bitmain.com All Rights Reserved
 */
public class SkillDictionaryVO extends SkillDictionary {

    /**  技能字典编号  */
    @Expose
    private String skillDictionaryNo;

    /**  技能名称（中文）  */
    @Expose
    private String skillNameZh;

    /**  技能名称（英文）  */
    @Expose
    private String skillNameEn;

    /**  卡牌图片  */
    @Expose
    private String image;

    /**  能耗  */
    @Expose
    private Integer power;

    /**  类型（1:主动; 2:防御； 3:陷阱）  */
    @Expose
    private String type;

    /**  是否指向性技能（主动技能特有规则）  */
    @Expose
    private String pointTo;

    /**  级别（0:N; 1:R; 2:SR; 3:SSR; 4:UR）  */
    @Expose
    private String level;

    /**  使用回合(P:翻牌前；F:翻牌；T:转牌；R:河牌)  */
    @Expose
    private String useRound;

    /**  描述  */
    @Expose
    private String description;

    /**  限制条件  */
    @Expose
    private String constrains;

    /**
     * 剩余使用数量(系统卡组数量-1  无限量)
     */
    @Expose
    private int count;

    /**
     * 合约tokenId(系统卡组不显示)
     */
    @Expose
    private String tokenId;

    /**
     * 使用者信息
     */
    @Expose
    private PlayerVO usedPlayer;

    @Override
    public String getSkillDictionaryNo() {
        return skillDictionaryNo;
    }

    @Override
    public void setSkillDictionaryNo(String skillDictionaryNo) {
        this.skillDictionaryNo = skillDictionaryNo;
    }

    @Override
    public String getSkillNameZh() {
        return skillNameZh;
    }

    @Override
    public void setSkillNameZh(String skillNameZh) {
        this.skillNameZh = skillNameZh;
    }

    @Override
    public String getSkillNameEn() {
        return skillNameEn;
    }

    @Override
    public void setSkillNameEn(String skillNameEn) {
        this.skillNameEn = skillNameEn;
    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public Integer getPower() {
        return power;
    }

    @Override
    public void setPower(Integer power) {
        this.power = power;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getPointTo() {
        return pointTo;
    }

    @Override
    public void setPointTo(String pointTo) {
        this.pointTo = pointTo;
    }

    @Override
    public String getLevel() {
        return level;
    }

    @Override
    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String getUseRound() {
        return useRound;
    }

    @Override
    public void setUseRound(String useRound) {
        this.useRound = useRound;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getConstrains() {
        return constrains;
    }

    @Override
    public void setConstrains(String constrains) {
        this.constrains = constrains;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public PlayerVO getUsedPlayer() {
        return usedPlayer;
    }

    public void setUsedPlayer(PlayerVO usedPlayer) {
        this.usedPlayer = usedPlayer;
    }
}
