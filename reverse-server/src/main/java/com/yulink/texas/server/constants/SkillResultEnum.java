package com.yulink.texas.server.constants;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/20
 * @Copyright (c) bitmain.com All Rights Reserved
 */
public enum SkillResultEnum {
    NONE("无影响","None", "1"),
    DONE("执行结束","Done", "2"),
    JUMP("跳过","Jump", "3"),
    CONTINUE("继续","Continue", "4"),
    FOLD("弃牌","Fold", "5"),
    CHECKORCALL("过牌/跟注","CheckOrCall", "6"),
    ALLIN("全下","AllIn", "7"),;

    private final String skillResultZh;

    private final String skillResultEn;

    private final String code;

    private SkillResultEnum(String skillResultZh, String skillResultEn, String code) {
        this.skillResultZh = skillResultZh;
        this.skillResultEn = skillResultEn;
        this.code = code;
    }

    public String getSkillResultZh() {
        return skillResultZh;
    }

    public String getSkillResultEn() {
        return skillResultEn;
    }

    public String getCode() {
        return code;
    }
}
