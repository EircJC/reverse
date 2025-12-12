package com.yulink.texas.server.common.utils;

import com.yulink.texas.server.common.entity.PlayerVO;
import com.yulink.texas.server.common.room.Room;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TexasStatic {
	/**
	 * 登录玩家列表
	 * sessionId，playVO
	 */
	public static ConcurrentMap<String, PlayerVO> loginPlayerMap = new ConcurrentHashMap<String, PlayerVO>();
	/**
	 * 登录玩家列表
	 * playId，sessionId
	 */
	public static ConcurrentMap<String,String> playerSessionMap = new ConcurrentHashMap<String, String>();

//	/**
//	 * 房间列表，德州扑克
//	 */
//	public static List<Room> roomList=new CopyOnWriteArrayList<Room>();

	/**
	 * 房间列表，德州扑克
	 */
	public static ConcurrentMap<String, List<Room>> roomList = new ConcurrentHashMap<String, List<Room>>();

}
