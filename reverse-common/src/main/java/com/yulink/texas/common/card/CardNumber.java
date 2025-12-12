package com.yulink.texas.common.card;

import com.google.gson.annotations.SerializedName;

public enum CardNumber {
    @SerializedName("2")
    TWO("2", 2),
    @SerializedName("3")
    THREE("3", 3),
    @SerializedName("4")
    FOUR("4", 4),
    @SerializedName("5")
    FIVE("5", 5),
    @SerializedName("6")
    SIX("6", 6),
    @SerializedName("7")
    SEVEN("7", 7),
    @SerializedName("8")
    EIGHT("8", 8),
    @SerializedName("9")
    NINE("9", 9),
    @SerializedName("T")
    TEN("T", 10),
    @SerializedName("J")
    JACK("J", 11),
    @SerializedName("Q")
    QUEEN("Q", 12),
    @SerializedName("K")
    KING("K", 13),
    @SerializedName("A")
    ACE("A", 14);

    private final String symbol;
    private final int power;

    private CardNumber(String symbol, int power) {
        this.symbol = symbol;
        this.power = power;
    }

    @Override
    public String toString() {
        return symbol;
    }

    /**
     * 2-14
     * @return
     */
    public int getPower() {
        return power;
    }

    public String getSymbol() {
        return symbol;
    }
}
