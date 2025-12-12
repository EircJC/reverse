package com.yulink.texas.core.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import lombok.Data;

@Data
public class Order {
    /**  主键ID  */
    private Long id;

    /**  订单编号  */
    private String orderId;

    /**  买方customerId  */
    private Long buyerCustomerId;

    /**  卖方customerId  */
    private Long sellerCustomerId;

    /**  卖方业务员customerId  */
    private Long sellerSubCustomerId;

    /**  总金额  */
    private BigDecimal amount;

    /**  货币类型  */
    private String currency;

    /**  首付比例  */
    private BigDecimal advancePercent;

    /**  首付金额  */
    private BigDecimal advanceAmount;

    /**  尾款金额  */
    private BigDecimal restAmount;

    /**  类型  */
    private String type;

    /**  状态 0-待签约 1-待发货 2-待收货 3-交易成功 4-交易取消  */
    private String status;

    /**  备注信息  */
    private String comment;

    /**  创建时间  */
    private Date createTime;

    /**  更新时间  */
    private Date updateTime;

    public enum Column {
        id("id", "id", "BIGINT", false),
        orderId("order_id", "orderId", "VARCHAR", false),
        buyerCustomerId("buyer_customer_id", "buyerCustomerId", "BIGINT", false),
        sellerCustomerId("seller_customer_id", "sellerCustomerId", "BIGINT", false),
        sellerSubCustomerId("seller_sub_customer_id", "sellerSubCustomerId", "BIGINT", false),
        amount("amount", "amount", "DECIMAL", false),
        currency("currency", "currency", "VARCHAR", false),
        advancePercent("advance_percent", "advancePercent", "DECIMAL", false),
        advanceAmount("advance_amount", "advanceAmount", "DECIMAL", false),
        restAmount("rest_amount", "restAmount", "DECIMAL", false),
        type("type", "type", "CHAR", false),
        status("status", "status", "CHAR", false),
        comment("comment", "comment", "VARCHAR", false),
        createTime("create_time", "createTime", "TIMESTAMP", false),
        updateTime("update_time", "updateTime", "TIMESTAMP", false);

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
