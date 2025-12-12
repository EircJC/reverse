package com.yulink.texas.server.constants;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/20
 * @Copyright (c) bitmain.com All Rights Reserved
 */
public enum SkillTypeEnum {
    ACTIVE("主动技能","Active skills", "1"),
    DEFENSE("防御技能","Defense skills", "2"),
    TRAP("陷阱技能","Trap Skill", "3");

    private final String skillTypeNameZh;

    private final String skillTypeNameEn;

    private final String code;

    private SkillTypeEnum(String skillTypeNameZh, String skillTypeNameEn, String code) {
        this.skillTypeNameZh = skillTypeNameZh;
        this.skillTypeNameEn = skillTypeNameEn;
        this.code = code;
    }

    public String getSkillTypeNameZh() {
        return skillTypeNameZh;
    }

    public String getSkillTypeNameEn() {
        return skillTypeNameEn;
    }

    public String getCode() {
        return code;
    }
}
