package com.yulink.texas.core.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import lombok.Data;

@Data
public class SkillCard {
    /**  主键  */
    private Integer id;

    /**  技能卡编号  */
    private String skillCardNo;

    /**  技能字典编号）  */
    private String skillDictionaryNo;

    /**  合约TokenId  */
    private String tokenid;

    /**  玩家主键id  */
    private Integer playerOid;

    /**  使用数量，-1为无限量  */
    private Integer count;

    /**  类型  */
    private String type;

    /**  状态（0:停用; 1:启用）  */
    private String status;

    /**  备注  */
    private String remark;

    /**  创建时间  */
    private Date createTime;

    public enum Column {
        id("id", "id", "INTEGER", false),
        skillCardNo("skill_card_no", "skillCardNo", "VARCHAR", false),
        skillDictionaryNo("skill_dictionary_no", "skillDictionaryNo", "VARCHAR", false),
        tokenid("tokenId", "tokenid", "VARCHAR", false),
        playerOid("player_oid", "playerOid", "INTEGER", false),
        count("count", "count", "INTEGER", false),
        type("type", "type", "VARCHAR", false),
        status("status", "status", "VARCHAR", false),
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