package com.yulink.texas.core.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import lombok.Data;

@Data
public class GameLog {
    /**  主键  */
    private Integer id;

    /**  房间信息（普通场）  */
    private String roomtype;

    /**  房间信息（级别）  */
    private String roomlevel;

    /**  游戏开始时间  */
    private Date starttime;

    /**  游戏结算时间  */
    private Date endtime;

    /**  底池总金额  */
    private Integer countbetpool;

    /**  抽成  */
    private Integer cut;

    /**  庄家  */
    private String dealer;

    /**  小盲  */
    private String smallbet;

    /**  大盲  */
    private String bigbet;

    /**  第一圈（底牌圈）  */
    private String roundinfo;

    /**  分奖池信息  */
    private String betpoolinfo;

    /**   公共牌  */
    private String communitycards;

    /**  房间中玩家的初始信息  */
    private String playersinitinfo;

    /**  房间中玩家的最终信息   */
    private String playersfinalinfo;

    public enum Column {
        id("id", "id", "INTEGER", false),
        roomtype("roomType", "roomtype", "VARCHAR", false),
        roomlevel("roomLevel", "roomlevel", "VARCHAR", false),
        starttime("startTime", "starttime", "TIMESTAMP", false),
        endtime("endTime", "endtime", "TIMESTAMP", false),
        countbetpool("countBetpool", "countbetpool", "INTEGER", false),
        cut("cut", "cut", "INTEGER", false),
        dealer("dealer", "dealer", "LONGVARCHAR", false),
        smallbet("smallBet", "smallbet", "LONGVARCHAR", false),
        bigbet("bigBet", "bigbet", "LONGVARCHAR", false),
        roundinfo("roundInfo", "roundinfo", "LONGVARCHAR", false),
        betpoolinfo("betpoolInfo", "betpoolinfo", "LONGVARCHAR", false),
        communitycards("communityCards", "communitycards", "LONGVARCHAR", false),
        playersinitinfo("playersInitInfo", "playersinitinfo", "LONGVARCHAR", false),
        playersfinalinfo("playersFinalInfo", "playersfinalinfo", "LONGVARCHAR", false);

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