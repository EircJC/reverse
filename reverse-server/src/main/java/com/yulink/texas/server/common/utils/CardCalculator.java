package com.yulink.texas.server.common.utils;

import com.yulink.texas.common.card.Card;
import com.yulink.texas.common.card.CardNumber;
import com.yulink.texas.common.card.CardSuit;
import com.yulink.texas.common.card.HandPowerType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 牌型计算器
 * @Author: chao.jiang
 * @Date: 2022/9/5
 * @Copyright (c) bitmain.com All Rights Reserved
 */
public class CardCalculator {

    private final Comparator<CardNumber> cardNumberComparator = new Comparator<CardNumber>() {
        public int compare(CardNumber cardNumber1, CardNumber cardNumber2) {
            return cardNumber1.getPower() - cardNumber2.getPower();
        }
    };

    private final Comparator<Card> flashbackCardComparator = new Comparator<Card>() {
        public int compare(Card card1, Card card2) {
            return card2.getNumber().getPower() - card1.getNumber().getPower();
        }
    };

    public HandPower rank(List<Card> cards) {
        MapList<CardNumber, Card> numberGroup = getNumberGroup(cards);
        MapList<CardSuit, Card> suitGroup = getSuitGroup(cards);
        List<Card> bestCards = new ArrayList<>(); //最终7选5牌组列表

        // Straight flush
        bestCards = getStraightFlushCard(suitGroup);
        if (bestCards != null && bestCards.size() == 5) {
            return new HandPower(HandPowerType.STRAIGHT_FLUSH,
                bestCards);
        }


        // Four of a kind
        bestCards = getCardNumberForCount(4, numberGroup);
        if (bestCards != null && bestCards.size() == 4) {
            bestCards.addAll(getBigCardByCount(1, cards, bestCards));
            return new HandPower(HandPowerType.FOUR_OF_A_KIND,
                bestCards);
        }


        // Full house
        bestCards = getFullHouse(numberGroup);
        if (bestCards != null && bestCards.size() >= 5) {
            return new HandPower(HandPowerType.FULL_HOUSE, bestCards.subList(0, 5));
        }

        // Flush
        bestCards = getFlush(suitGroup);
        if (bestCards != null && bestCards.size() == 5) {
            return new HandPower(HandPowerType.FLUSH, bestCards);
        }

        // Straight
        bestCards = getStraightCard(cards);
        if (bestCards != null && bestCards.size() == 5) {
            return new HandPower(HandPowerType.STRAIGHT, bestCards);
        }

        // Three of a kind
        bestCards = getCardNumberForCount(3, numberGroup);
        if (bestCards != null && bestCards.size() == 3) {
            bestCards.addAll(getBigCardByCount(2, cards, bestCards));
            return new HandPower(HandPowerType.THREE_OF_A_KIND, bestCards);
        }

        // Pair(s)
        bestCards = getCardNumberForCount(2, numberGroup);

        if (bestCards != null && bestCards.size() >=2) {
            Collections.sort(bestCards, flashbackCardComparator);
            // One Pair
            if (bestCards.size() == 2) {
                bestCards.addAll(getBigCardByCount(3, cards, bestCards));
                return new HandPower(HandPowerType.ONE_PAIR, bestCards);
            } else {
               // Two pair
                bestCards.addAll(getBigCardByCount(1, cards, bestCards));
                return new HandPower(HandPowerType.TWO_PAIR, bestCards);
            }
        }

        // High Card
        Collections.sort(cards, flashbackCardComparator);
        return new HandPower(HandPowerType.HIGH_CARD, cards.subList(0, 5));
    }

    private List<Card> getFullHouse(MapList<CardNumber, Card> numberGroup) {
        List<Card> fullHouseCardNumbers = new ArrayList<Card>();

        List<CardNumber> cardNumbers = new ArrayList<CardNumber>(
            numberGroup.keySet());
        Collections.sort(cardNumbers, cardNumberComparator);
        Collections.reverse(cardNumbers);

        // Find the best cards for the triple
        for (CardNumber cardNumber : cardNumbers) {
            if (numberGroup.get(cardNumber).size() >= 3) {
                fullHouseCardNumbers.addAll(numberGroup.get(cardNumber));
                break;
            }
        }

        // Find the best card for the pair
        if (fullHouseCardNumbers.size() > 0) {
            for (CardNumber cardNumber : cardNumbers) {
                if (numberGroup.get(cardNumber).size() >= 2
                    && !cardNumber.equals(fullHouseCardNumbers.get(0).getNumber())) {
                    fullHouseCardNumbers.addAll(numberGroup.get(cardNumber));
                    break;
                }
            }
        }

        return fullHouseCardNumbers;
    }

    private List<Card> getCardNumberForCount(Integer count,
        MapList<CardNumber, Card> numberGroup) {

        List<Card> cardList = new ArrayList<>();
        for (Map.Entry<CardNumber, List<Card>> entry : numberGroup.entrySet()) {
            if (entry.getValue().size() == count) {
                cardList.addAll(entry.getValue());
            }
        }
        return cardList;
    }

    /**
     * 在牌组cards中查出count个最大单张，单张需要通过excludeCards排除
     * @param count
     * @param cards
     * @param excludeCards
     * @return
     */
    private List<Card> getBigCardByCount(Integer count, List<Card> cards, List<Card> excludeCards) {
        if(cards == null || cards.size() == 0 || count == null || count.intValue() == 0) {
            return null;
        }
        List<Card> cardList = new ArrayList<>();
        Collections.sort(cards, flashbackCardComparator);
        int cur = 0;
        for(int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            if(!excludeCards.contains(card)) {
                cardList.add(card);
                cur ++;
            }
            if(count.intValue() == cur) {
                break;
            }
        }

        return cardList;
    }

    private List<Card> getStraightFlushCard(MapList<CardSuit, Card> suitGroup) {
        List<Card> flushSuit = getFlush(suitGroup);
        if (flushSuit == null) {
            return null;
        }

        return getStraightCard(flushSuit);
    }

    private List<Card> getStraightCard(List<Card> cards) {
        List<Card> bestCard = new ArrayList<>();
        Collections.sort(cards, flashbackCardComparator);

        for (Card card : cards) {
            // 牌面数字排重
            if(bestCard.parallelStream().anyMatch(c -> card.getNumber().getPower() == c.getNumber().getPower())) {
                continue;
            }
            if(cards.parallelStream().anyMatch(c -> card.getNumber().getPower() - 1 == c.getNumber().getPower())) {
                bestCard.add(card);
            } else {
                if(cards.parallelStream().anyMatch(c -> card.getNumber().getPower() + 1 == c.getNumber().getPower())) {
                    bestCard.add(card);
                }
                if(bestCard.size() >= 5) {
                    break;
                }
                if(bestCard.size() == 4 && bestCard.get(3).getNumber().getPower() == 2) {
                    for(Card cardTmp : cards) {
                        if(cardTmp.getNumber().getPower() == 14) {
                            bestCard.add(cardTmp);
                            break;
                        }
                    }
                } else {
                    bestCard.clear();
                }
            }
        }
        return bestCard.size() >= 5 ? bestCard.subList(0, 5) : null;
    }

    private List<Card> getFlush(MapList<CardSuit, Card> suitGroup) {
        for (List<Card> cards : suitGroup) {
            if (cards.size() >= 5) {
                Collections.sort(cards, flashbackCardComparator);
                return cards.subList(0,5);
            }
        }
        return null;
    }

    private MapList<CardNumber, Card> getNumberGroup(List<Card> cards) {
        MapList<CardNumber, Card> numberGroup = new MapList<CardNumber, Card>();
        for (Card card : cards) {
            numberGroup.add(card.getNumber(), card);
        }
        return numberGroup;
    }

    private MapList<CardSuit, Card> getSuitGroup(List<Card> cards) {
        MapList<CardSuit, Card> suitGroup = new MapList<CardSuit, Card>();
        for (Card card : cards) {
            suitGroup.add(card.getSuit(), card);
        }
        return suitGroup;
    }
}
