package com.yulink.texas.common.card;

import com.google.gson.annotations.SerializedName;

public enum CardSuit {
    @SerializedName("s")
    SPADE("\u2660", "s"),
    @SerializedName("h")
    HEART("\u2665", "h"),
    @SerializedName("c")
    CLUB("\u2663", "c"),
    @SerializedName("d")
    DIAMOND("\u2666", "d");

    private final String logo;
    private final String symbol;

    private CardSuit(String logo, String symbol) {
        this.logo = logo;
        this.symbol = symbol;
    }

    public String getLogo() {
        return logo;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }


}
