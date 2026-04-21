package com.yulink.texas.server.common.room;

import com.yulink.texas.common.utils.CodeUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 房间类型列表
 * 
 * @author Ming
 *
 */
public class RoomTypeList {
	public static Map<String, Room> roomTypeMap = new HashMap<String, Room>();
	static {
		roomTypeMap.put("0-0", getNewRoom(0, 0));
		roomTypeMap.put("0-1", getNewRoom(0, 1));
		roomTypeMap.put("0-2", getNewRoom(0, 2));
		roomTypeMap.put("1-0", getNewRoom(1, 0));
		roomTypeMap.put("1-1", getNewRoom(1, 1));
		roomTypeMap.put("1-2", getNewRoom(1, 2));
		roomTypeMap.put("2-0", getNewRoom(2, 0));
		roomTypeMap.put("2-1", getNewRoom(2, 1));
		roomTypeMap.put("2-2", getNewRoom(2, 2));
	}

	/**
	 * 根据级别获取德州扑克房间
	 * 
	 * @param level
	 * @return
	 */
	public static Room getNewRoom(int type, int level) {
		Room room = new Room();
		int jMaxPlayer = 6;
		room.setRoomNo(CodeUtil.sequenceNo("ROOM-"));
		room.setMaxPlayers(6);
		room.setMinPlayers(2);
		room.setDealer(1);
		room.setRoomstate(1);
		room.setFreeSeatStack(getStack(jMaxPlayer));
		room.setOptTimeout(60000);
		room.setRestBetweenGame(8000);
		room.setType(type);
		if (level == 0) {
			room.setLevel(0);
			room.setMaxChips(10000);
			room.setMinChips(1000);
			room.setBigBet(100);
			room.setSmallBet(50);
		} else if (level == 1) {
			room.setLevel(1);
			room.setMaxChips(30000);
			room.setMinChips(10000);
			room.setBigBet(200);
			room.setSmallBet(100);
		} else if (level == 2) {
			room.setLevel(2);
			room.setMaxChips(100000);
			room.setMinChips(20000);
			room.setBigBet(500);
			room.setSmallBet(250);
		}
		return room;
	}

	public static Stack<Integer> getStack(int maxPlayer) {
		Stack<Integer> stack = new Stack<Integer>();
		for (int i = 0; i < maxPlayer; i++) {
			stack.push(i);
		}
		return stack;
	}

}
