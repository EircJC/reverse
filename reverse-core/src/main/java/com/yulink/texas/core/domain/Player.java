package com.yulink.texas.core.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import lombok.Data;

@Data
public class Player {
    /**  */
    private Integer id;

    /**  用户名  */
    private String userName;

    /**  用户密码  */
    private String userpwd;

    /**  用户昵称  */
    private String nickName;

    /**  邮箱  */
    private String email;

    /**  手机  */
    private String phone;

    /**  筹码数  */
    private Long chips;

    /**  头像地址  */
    private String picLogo;

    /**  注册时间  */
    private Date regdate;

    /**  状态1正常，2冻结  */
    private String status;

    /**  是否是机器人  */
    private String isrobot;

    /**  */
    private String type;

    /**  钱包地址  */
    private String walletAddress;

    /**  备注  */
    private String remark;

    public enum Column {
        id("id", "id", "INTEGER", false),
        userName("user_name", "userName", "VARCHAR", false),
        userpwd("userpwd", "userpwd", "VARCHAR", false),
        nickname("nick_name", "nickName", "VARCHAR", false),
        email("email", "email", "VARCHAR", false),
        phone("phone", "phone", "VARCHAR", false),
        chips("chips", "chips", "BIGINT", false),
        picLogo("pic_logo", "picLogo", "VARCHAR", false),
        regdate("regdate", "regdate", "TIMESTAMP", false),
        status("status", "status", "CHAR", false),
        isrobot("isrobot", "isrobot", "CHAR", false),
        type("type", "type", "VARCHAR", false),
        walletAddress("wallet_address", "walletAddress", "VARCHAR", false),
        remark("remark", "remark", "VARCHAR", false);

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