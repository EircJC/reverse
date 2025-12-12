package com.yulink.texas.server.common.utils;

import com.yulink.texas.common.card.Card;
import com.yulink.texas.common.card.HandPowerType;
import java.util.ArrayList;
import java.util.List;

public class HandPower implements Comparable<HandPower> {
    private final HandPowerType handPowerType;
    private final List<Card> tieBreakingInformation;
    public List<String> cardList = new ArrayList<>();
    public int score;

    public HandPower(final HandPowerType handPowerType,
            final List<Card> tieBreakingInformation) {
        this.handPowerType = handPowerType;
        this.tieBreakingInformation = tieBreakingInformation;
        if(tieBreakingInformation != null && tieBreakingInformation.size() > 0) {
            for(Card card: tieBreakingInformation) {
                cardList.add(card.toString());
            }
        }

    }

    /**
     * this > other return 1
     * this = other return 0
     * this < other return -1
     *
     * 逻辑：先判断牌型handPowerType，如果牌型相同则判断分值score
     *
     * @param other
     * @return
     */
    public int compareTo(HandPower other) {
        int typeDifference = handPowerType.getPower()
            - other.handPowerType.getPower();
        if(typeDifference > 0) {
            return 1;
        } else if(typeDifference == 0) {
            int tmpScore = getScore() - other.getScore();
            return tmpScore > 0 ? 1 : tmpScore < 0 ? -1 : 0;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return handPowerType.toString() + " "
                + tieBreakingInformation.toString();
    }

    public HandPowerType getHandPowerType() {
        return handPowerType;
    }

    public List<Card> getTieBreakingInformation() {
        return tieBreakingInformation;
    }

    public List<String> getCardList() {
        return cardList;
    }

    public int getScore() {
        score = 0;
        if(tieBreakingInformation != null && tieBreakingInformation.size() >0) {
            int j = tieBreakingInformation.size();
            int cur = 100000000;
            for(int i = 0; i < j; i++) {
                Card card = tieBreakingInformation.get(i);
                score += card.getNumber().getPower()*cur;
                cur /= 100;
            }

            // 如果是顺子或同花顺并且有牌组有2和A则 该牌型为A2345，需要把A的值14 减13使其价值为1
            if((handPowerType.getPower() == 5 || handPowerType.getPower() == 9)
                && tieBreakingInformation.parallelStream().anyMatch(c -> c.getNumber().getPower() == 2)
                && tieBreakingInformation.parallelStream().anyMatch(c -> c.getNumber().getPower() == 14)) {
                score -= 13;
            }
        }

        return score;
    }
}
