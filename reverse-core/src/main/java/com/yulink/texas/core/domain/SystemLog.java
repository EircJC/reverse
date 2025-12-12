package com.yulink.texas.core.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import lombok.Data;

@Data
public class SystemLog {
    /**  主键  */
    private Integer id;

    /**  用户编号  */
    private Integer userid;

    /**  类型  */
    private String type;

    /**  操作  */
    private String operation;

    /**  时间  */
    private Date datetime;

    /**  机器码  */
    private String machine;

    /**  客户端类型  */
    private String clienttype;

    /**  记号  */
    private String token;

    /**  app版本  */
    private String appversion;

    /**  IP  */
    private String ip;

    /**  内容  */
    private String content;

    public enum Column {
        id("id", "id", "INTEGER", false),
        userid("userid", "userid", "INTEGER", false),
        type("type", "type", "VARCHAR", false),
        operation("operation", "operation", "VARCHAR", false),
        datetime("datetime", "datetime", "TIMESTAMP", false),
        machine("machine", "machine", "VARCHAR", false),
        clienttype("clienttype", "clienttype", "VARCHAR", false),
        token("token", "token", "VARCHAR", false),
        appversion("appversion", "appversion", "VARCHAR", false),
        ip("ip", "ip", "VARCHAR", false),
        content("content", "content", "LONGVARCHAR", false);

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