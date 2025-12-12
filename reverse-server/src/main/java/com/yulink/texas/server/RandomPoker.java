package com.yulink.texas.server;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.yulink.texas.common.InitCards;
import com.yulink.texas.common.card.Card;
import com.yulink.texas.common.card.CardNumber;
import com.yulink.texas.common.card.CardSuit;
import com.yulink.texas.server.common.entity.PrivateRoom;
import com.yulink.texas.server.common.utils.CardCalculator;
import com.yulink.texas.server.common.utils.HandPower;
import com.yulink.texas.server.common.utils.JsonUtils;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/1
 * @Copyright (c) bitmain.com All Rights Reserved
 */
public class RandomPoker {

    public static void main(String[] args) {

//        // 获取初始poker数组
//        List<String> pokers =  InitCards.initPoker();
//        StringBuffer sb = new StringBuffer();
//
//        // 第一次洗牌
//        List<String> randomPokers = InitCards.randomPoker(pokers);
//        for(String poker: randomPokers) {
//            sb.append(poker).append(",");
//        }
//        System.out.println(sb.toString());
//
//        // 把牌进行加盐hash重新封装数组
//        List<String> hexList = hexPokers(randomPokers,null, 123456);
//
//        // 获取到第二次洗牌顺序  25,33,1,4,67......  以此顺序作为index取hexList值排序，此顺序为所有玩家按座次手牌及公共牌组顺序
//        List<String> randomIndex = InitCards.randomPoker(InitCards.pokerInitIndex());

//      新规则  ========================
        CardCalculator cardCalculator = new CardCalculator();
//
        InitCards ic = new InitCards();
        StringBuffer sb = new StringBuffer();
        int index = 0;
        if(ic.cards != null) {
            Iterator it = ic.cards.iterator();
            while(it.hasNext()) {
                sb.append(index+":").append(it.next()).append(",");
                index ++;
            }
        }
        System.out.println(sb.toString());
        sb = new StringBuffer();
        List<Card> cards = ic.cards;
        cards = cards.subList(12,52);
        Iterator it = cards.iterator();
        index = 12;
        while(it.hasNext()) {
            sb.append(index+":").append(it.next()).append(",");
            index ++;
        }
        System.out.println(sb.toString());



//        System.out.println(RandomStringUtils.random(1,"hsdc"));
//        System.out.println(RandomStringUtils.random(1,"hsdc"));
//        System.out.println(RandomStringUtils.random(1,"hsdc"));
        List<Card> publicCardList = ic.cards.subList(12,17);
//        for(int i = 12; i <= 16; i++) {
//            publicCardList.add(ic.cards.get(i));
//        }
//        System.out.println(publicCardList.toString());
//        for(int i = 0; i < 12; i+=2) {
//            List<Card> cardList = new ArrayList<>();
//            cardList.addAll(publicCardList);
//            cardList.add(ic.cards.get(i));
//            cardList.add(ic.cards.get(i+1));
//
//            HandPower handPower = cardCalculator.rank(cardList);
//            System.out.println(ic.cards.get(i)+","+ic.cards.get(i+1)+"=="+handPower.toString() + ",HandPowerType:"+handPower.getHandPowerType().getPower()+",score:"+handPower.getScore());
//        }
        List<Card> cardList = new ArrayList<>(); // jim
        cardList.add(new Card(CardSuit.DIAMOND,CardNumber.THREE));
        cardList.add(new Card(CardSuit.HEART,CardNumber.NINE));
        cardList.add(new Card(CardSuit.HEART,CardNumber.FIVE));
        cardList.add(new Card(CardSuit.HEART,CardNumber.SEVEN));
        cardList.add(new Card(CardSuit.HEART,CardNumber.SIX));
        cardList.add(new Card(CardSuit.DIAMOND,CardNumber.FIVE));
        cardList.add(new Card(CardSuit.SPADE,CardNumber.NINE));
        HandPower handPower = cardCalculator.rank(cardList);

//        {\"handPokers\":[\"3h\",\"Td\"] bob
//            {\"handPokers\":[\"3d\",\"9h\"] jim
//                bob 5 jim 4
//                [\"5h\",\"7h\",\"6h\",\"9s\",\"5d\"]
//                "4":["3d","9h"],"5":["3h","Td"]
//                "4":["9s","9h","5h","5d","7h"],"5":["5h","5d","Td","9s","7h"]5w
        List<Card> cardList1 = new ArrayList<>(); // bob
        cardList1.add(new Card(CardSuit.HEART,CardNumber.THREE));
        cardList1.add(new Card(CardSuit.DIAMOND,CardNumber.TEN));
        cardList1.add(new Card(CardSuit.HEART,CardNumber.FIVE));
        cardList1.add(new Card(CardSuit.HEART,CardNumber.SEVEN));
        cardList1.add(new Card(CardSuit.HEART,CardNumber.SIX));
        cardList1.add(new Card(CardSuit.DIAMOND,CardNumber.FIVE));
        cardList1.add(new Card(CardSuit.SPADE,CardNumber.NINE));
        HandPower handPower1 = cardCalculator.rank(cardList1);
        System.out.println("jim:"+handPower.toString());
        System.out.println("bob:"+handPower1.toString());
        System.out.println("winner:====="+handPower1.compareTo(handPower));

        System.out.println(handPower.toString() + ",HandPowerType:"+handPower.getHandPowerType().getPower()+",score:"+handPower.getScore());
//        ONE_PAIR [5h, 5d, Td, 9s, 7h],HandPowerType:2,score:505100907
//        TWO_PAIR [9h, 9s, 5h, 5d, 7h],HandPowerType:3,score:909050507

        PrivateRoom pRoom = new PrivateRoom();
        ;
        // 私有房间信息（手牌）
//        pRoom.setHandPokers(cardList.toArray(new Card[2]));
//				String msg = JsonUtils.toJson(pRoom, PrivateRoom.class);
        System.out.println(JSONObject.toJSONString(pRoom));
//        System.out.println(JsonUtils.toJson(pRoom, PrivateRoom.class));
        System.out.println(JsonUtils.toJson(new Card(CardSuit.SPADE, CardNumber.THREE), Card.class));
        Gson gson = new Gson();
        System.out.println(gson.toJson(cardList.toArray(new Card[2])));

    }

    public List<Card> testCards() {
        List<Card> cards = new ArrayList<>();

        Card card_1 = new Card(CardSuit.CLUB, CardNumber.EIGHT);
        Card card_2 = new Card(CardSuit.CLUB, CardNumber.ACE);
        Card card_3 = new Card(CardSuit.DIAMOND, CardNumber.EIGHT);
        Card card_4 = new Card(CardSuit.DIAMOND, CardNumber.JACK);
        Card card_5 = new Card(CardSuit.SPADE, CardNumber.JACK);
        Card card_6 = new Card(CardSuit.CLUB, CardNumber.JACK);
        Card card_7 = new Card(CardSuit.HEART, CardNumber.EIGHT);
        cards.add(card_1);
        cards.add(card_2);
        cards.add(card_3);
        cards.add(card_4);
        cards.add(card_5);
        cards.add(card_6);
        cards.add(card_7);
        return cards;
    }

    // 生成初始种子对洗牌后的poker进行单向加密
    public static List<String> hexPokers(List<String> pokers, String matchId, long handId) {
        List<String> hexPokerList = new ArrayList<>();
        Random random = new Random();
        long seed = System.nanoTime()-handId;
        random.setSeed(seed);
        int index = 1;
        for(String poker: pokers) {
            long r = random.nextInt();
            String salt = sha1(String.valueOf(r));
            System.out.println(index+" "+ poker+" " +salt+" "+sha1(poker+salt));
            hexPokerList.add(sha1(poker+salt));
            index ++;
        }
        return hexPokerList;
    }



    public static String sha1(String str) {
        String result = null;
        try {
            MessageDigest md1 = MessageDigest.getInstance("SHA-1");
            md1.update(str.getBytes("UTF-8"));
            byte[] result1 = md1.digest();
            result = new BigInteger(1, result1).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
