package com.yulink.texas.server.common.entity;

import java.io.Serializable;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/16
 * @Copyright (c) bitmain.com All Rights Reserved
 */
public class UseSkill implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标id（指向性主动技释放目标）
     */
    private String destPlayerId;

    private String skillDictionaryNo;

    private String tokenId;

    /**
     * 使用倒转乾坤时次字段生效，表示需要重发哪条街的公共牌
     * 1:F
     * 2:T
     * 3:R
     */
    private Integer reverseRound;

    public String getDestPlayerId() {
        return destPlayerId;
    }

    public void setDestPlayerId(String destPlayerId) {
        this.destPlayerId = destPlayerId;
    }

    public String getSkillDictionaryNo() {
        return skillDictionaryNo;
    }

    public void setSkillDictionaryNo(String skillDictionaryNo) {
        this.skillDictionaryNo = skillDictionaryNo;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public Integer getReverseRound() {
        return reverseRound;
    }

    public void setReverseRound(Integer reverseRound) {
        this.reverseRound = reverseRound;
    }
}
