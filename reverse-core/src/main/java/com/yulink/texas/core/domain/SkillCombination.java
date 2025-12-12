package com.yulink.texas.core.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import lombok.Data;

@Data
public class SkillCombination {
    /**  主键  */
    private Integer id;

    /**  技能卡组编号  */
    private String skillCombinationNo;

    /**  卡组名称  */
    private String skillCombinationName;

    /**  技能卡编号  */
    private String skillCardNo;

    /**  玩家主键id  */
    private Integer playerOid;

    /**  类型  */
    private String type;

    /**  状态（0:停用; 1:启用）  */
    private String status;

    /**  备注  */
    private String remark;

    /**  创建时间  */
    private Date createTime;

    /**  描述  */
    private String description;

    /**  限制条件  */
    private String constrains;

    public enum Column {
        id("id", "id", "INTEGER", false),
        skillCombinationNo("skill_combination_no", "skillCombinationNo", "VARCHAR", false),
        skillCombinationName("skill_combination_name", "skillCombinationName", "VARCHAR", false),
        skillCardNo("skill_card_no", "skillCardNo", "VARCHAR", false),
        playerOid("player_oid", "playerOid", "INTEGER", false),
        type("type", "type", "VARCHAR", false),
        status("status", "status", "VARCHAR", false),
        remark("remark", "remark", "VARCHAR", false),
        createTime("create_time", "createTime", "TIMESTAMP", false),
        description("description", "description", "LONGVARCHAR", false),
        constrains("constrains", "constrains", "LONGVARCHAR", false);

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