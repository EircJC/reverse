package com.yulink.texas.common;

import com.yulink.texas.common.card.Card;

import com.yulink.texas.common.card.CardNumber;
import com.yulink.texas.common.card.CardSuit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @Author: chao.jiang
 * @Date: 2018/9/21
 * @Copyright (c) bitmain.com All Rights Reserved
 */
public class InitCards {
    public final List<Card> cards = new ArrayList<Card>();

    /**
     * 初始化牌组 & 洗牌
     */
    public InitCards() {
        for (CardSuit suit : CardSuit.values()) {
            for (CardNumber number : CardNumber.values()) {
                Card card = new Card(suit, number);
                cards.add(card);
            }
        }

        // 洗牌
        Collections.shuffle(cards);
    }

    public static List<String> randomPoker(List<String> pokers) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        // 洗牌
        for(int i = 51; i >=0; i--) {
            int r = random.nextInt(i+1);
//            System.out.println(r);
            Collections.swap(pokers, i, r == 0 ? r: r-1);
        }
        for(int i=0;i<52;i++) {
            sb.append(pokers.get(i));
            sb.append(",");
        }
//        System.out.println(sb.toString());
        return pokers;
    }

    public static List<String> pokerInitIndex() {
        List<String> pokerInitIndex = new ArrayList<>();
        for(int i = 1; i<= 52; i ++) {
            pokerInitIndex.add(String.valueOf(i));
        }
        return pokerInitIndex;
    }
    public static void main(String[] args) throws Exception {
//        for(int i=0;i<10;i++) {
//            randomPoker(initPoker());
//        }
    }
}
