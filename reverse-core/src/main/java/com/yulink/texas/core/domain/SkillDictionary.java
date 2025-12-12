package com.yulink.texas.core.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import lombok.Data;

@Data
public class SkillDictionary {
    /**  主键  */
    private Integer id;

    /**  技能字典编号  */
    private String skillDictionaryNo;

    /**  技能名称（中文）  */
    private String skillNameZh;

    /**  技能名称（英文）  */
    private String skillNameEn;

    /**  能耗  */
    private Integer power;

    /**  图片  */
    private String image;

    /**  类型（1:主动; 2:防御； 3:陷阱）  */
    private String type;

    /**  是否指向性技能（主动技能特有规则）  */
    private String pointTo;

    /**  级别（0:N; 1:R; 2:SR; 3:SSR; 4:UR）  */
    private String level;

    /**  状态  */
    private String status;

    /**  使用回合(P:翻牌前；F:翻牌；T:转牌；R:河牌)  */
    private String useRound;

    /**  陷阱触发动作，只有type=3启用(C:过牌；F:弃牌；B:下注；S:使用技能卡)  */
    private String trapAction;

    /**  优先级：0最小  */
    private Integer priority;

    /**  描述  */
    private String description;

    /**  限制条件  */
    private String constrains;

    /**  备注  */
    private String remark;

    /**  创建时间  */
    private Date createTime;

    public enum Column {
        id("id", "id", "INTEGER", false),
        skillDictionaryNo("skill_dictionary_no", "skillDictionaryNo", "VARCHAR", false),
        skillNameZh("skill_name_zh", "skillNameZh", "VARCHAR", false),
        skillNameEn("skill_name_en", "skillNameEn", "VARCHAR", false),
        power("power", "power", "INTEGER", false),
        image("image", "image", "VARCHAR", false),
        type("type", "type", "VARCHAR", false),
        pointTo("point_to", "pointTo", "VARCHAR", false),
        level("level", "level", "VARCHAR", false),
        status("status", "status", "VARCHAR", false),
        useRound("use_round", "useRound", "VARCHAR", false),
        trapAction("trap_action", "trapAction", "VARCHAR", false),
        priority("priority", "priority", "INTEGER", false),
        description("description", "description", "VARCHAR", false),
        constrains("constrains", "constrains", "VARCHAR", false),
        remark("remark", "remark", "VARCHAR", false),
        createTime("create_time", "createTime", "TIMESTAMP", false);

        private static final String BEGINNING_DELIMITER = "\"";

        private static final String ENDING_DELIMITER = "\"";

        private final String column;

        private final boolean isColumnNameDelimited;

        private final String javaProperty;

        private final String jdbcType;

        public String value() {
            return this.column;
        }

        public String getValue() {
            return this.column;
        }

        public String getJavaProperty() {
            return this.javaProperty;
        }

        public String getJdbcType() {
            return this.jdbcType;
        }

        Column(String column, String javaProperty, String jdbcType, boolean isColumnNameDelimited) {
            this.column = column;
            this.javaProperty = javaProperty;
            this.jdbcType = jdbcType;
            this.isColumnNameDelimited = isColumnNameDelimited;
        }

        public String desc() {
            return this.getEscapedColumnName() + " DESC";
        }

        public String asc() {
            return this.getEscapedColumnName() + " ASC";
        }

        public static Column[] excludes(Column ... excludes) {
            ArrayList<Column> columns = new ArrayList<>(Arrays.asList(Column.values()));
            if (excludes != null && excludes.length > 0) {
                columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));
            }
            return columns.toArray(new Column[]{});
        }

        public static Column[] all() {
            return Column.values();
        }

        public String getEscapedColumnName() {
            if (this.isColumnNameDelimited) {
                return new StringBuilder().append(BEGINNING_DELIMITER).append(this.column).append(ENDING_DELIMITER).toString();
            } else {
                return this.column;
            }
        }

        public String getAliasedEscapedColumnName() {
            return this.getEscapedColumnName();
        }
    }
}