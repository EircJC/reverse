package com.yulink.texas.server.ws;

import com.yulink.texas.common.card.Card;
import com.yulink.texas.server.common.entity.PlayerVO;
import com.yulink.texas.server.common.entity.PrivateRoom;
import com.yulink.texas.server.common.entity.RetMsg;
import com.yulink.texas.server.common.room.Room;
import com.yulink.texas.server.common.room.RoomTypeList;
import com.yulink.texas.server.common.utils.JsonUtils;
import com.yulink.texas.server.common.utils.SkillCardsUtil;
import com.yulink.texas.server.common.utils.TexasStatic;
import com.yulink.texas.server.manager.RoomManager;
import com.yulink.texas.server.netty.NettyWebSocketHandler;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TexasUtil {
	private static Logger logger = LogManager.getLogger(TexasUtil.class);

	private static int skillAwardCount = 1;

	@Autowired
	public RoomManager roomManager;

	/**
	 * 获取一个对应级别的可用房间，直接进入
	 * 
	 * @param level
	 * @return
	 */
	public Room getUsableRoomThenIn(int type, int level, PlayerVO p) {
		List<Room> roomList = TexasStatic.roomList.get(type+"-"+level );
		if (roomList == null || roomList.isEmpty()) {
			return createRoomByPlayer(type, level, p);
		}
		Room room = tryJoinUsableRoom(roomList, p, true);
		if (room != null) {
			return room;
		}
		room = tryJoinUsableRoom(roomList, p, false);
		if (room != null) {
			return room;
		}
		return createRoomByPlayer(type, level, p);
	}

	private Room tryJoinUsableRoom(List<Room> roomList, PlayerVO player, boolean occupiedRoomFirst) {
		for (int i = 0; i < roomList.size(); i++) {
			Room room = roomList.get(i);
			if (room == null || room.getRoomstate() != 1) {
				continue;
			}
			boolean occupied = getRoomPlayerCount(room) > 0;
			if (occupiedRoomFirst != occupied) {
				continue;
			}
			boolean success = inRoom(room, player);
			if (success) {
				return room;
			}
		}
		return null;
	}

	/**
	 * 进入房间
	 * 
	 * <pre>
	 * 1，检查房间是否还可加入 2，加入房间/重新查找可以进入的房间 3，改变房间状态
	 */
	public boolean inRoom(Room room, PlayerVO player) {
		if (room == null || player == null) {
			return false;
		}
		// 判断房间内是否有playerID相同的玩家（session可能不一致），将oldsession 更新
		for(PlayerVO playerTmp: room.getIngamePlayers()) { // 游戏中玩家列表
			if(player.getId().equals(playerTmp.getId())) {
				playerTmp.setChannel(player.getChannel());
				// 设置房间
				player.setRoom(room);
				// 设置座位号
				player.setSeatNum(playerTmp.getSeatNum());
				return true;
			}
		}
		// 判断房间内是否有playerID相同的玩家（session可能不一致），将oldsession 更新
		for(PlayerVO playerTmp: room.getWaitPlayers()) { // 等待中玩家列表
			if(player.getId().equals(playerTmp.getId())) {
				// 设置房间
				player.setRoom(room);
				// 设置座位号
				player.setSeatNum(playerTmp.getSeatNum());
				playerTmp.setChannel(player.getChannel());
				return true;
			}
		}

		// 如果玩家已在房间中，则先出房间
//		outRoom(player);
		if (room.getRoomstate() == 0) {
			return false;
		}
		// 房间加锁
		synchronized (room.getFreeSeatStack()) {
			// 房间满人，修改状态为不可加入, 加入房间失败
			if (room.getFreeSeatStack().isEmpty()) {
				room.setRoomstate(0);
				return false;
			}
			room.getWaitPlayers().add(player);
			// 设定座位号
			int seatNum = room.getFreeSeatStack().pop();// 从空闲座位的栈中取出一个座位
			player.setSeatNum(seatNum);
			if (room.getFreeSeatStack().isEmpty()) {
				// 房间满人，修改状态为不可加入
				room.setRoomstate(0);
			}
			roomManager.assignChipsForInRoom(room, player);
			// 成功则设置房间
			player.setRoom(room);
		}
		return true;
	}

	/**
	 * 退出房间
	 */
	public void outRoom(PlayerVO player) {
		if (player == null || player.getRoom() == null) {
			return;
		}
		synchronized (player.getRoom().getFreeSeatStack()) {
			Room room = player.getRoom();
			// 通知所有房间内玩家，有玩家离开
			sendPlayerToOthers(player, room, "onPlayerLeaveRoom");
			removeWaitOrInGamePlayer(player);
			// 成功则设置房间
			int index = room.donePlayerList.indexOf(player.getSeatNum());
			if (index != -1) {
				room.donePlayerList.remove(index);
			}
			// 记录玩家的筹码变化
			roomManager.assignChipsForOutRoom(player);
			// 还座位号
			if (player.getSeatNum() != -1) {
				room.getFreeSeatStack().push(player.getSeatNum());
				player.setSeatNum(-1);
			}
			// 修改房间状态为可加入
			room.setRoomstate(1);
			// 在游戏中的玩家数少于最低玩家数时结束游戏
			roomManager.checkEnd(room);
			System.out.println(player.getUserName()+" 离开房间");
		}
	}

	/**
	 * 移除等待或游戏中的玩家
	 * 
	 * @param player
	 */
	public static void removeWaitOrInGamePlayer(PlayerVO player) {
		removeWaitPlayer(player);
		removeIngamePlayer(player);
	}

	public static boolean removeWaitPlayer(PlayerVO player) {
		boolean success = false;
		PlayerVO ret = null;
		if (player != null && player.getRoom() != null) {
			Room room = player.getRoom();
			// 等待中的玩家退出房间
			for (int i = 0; i < room.getWaitPlayers().size(); i++) {
				PlayerVO p = room.getWaitPlayers().get(i);
				if (p.getId().equals(player.getId())) {
					ret = room.getWaitPlayers().remove(i);
				}
			}
		}
		if (ret != null) {
			success = true;
		}
		return success;
	}

	public static boolean removeIngamePlayer(PlayerVO player) {
		boolean success = false;
		PlayerVO ret = null;
		if (player != null && player.getRoom() != null) {
			Room room = player.getRoom();
			// 等待中的玩家退出房间
			for (int i = 0; i < room.getIngamePlayers().size(); i++) {
				PlayerVO p = room.getIngamePlayers().get(i);
				if (p.getId().equals(player.getId())) {
					ret = room.getIngamePlayers().remove(i);
				}
			}
		}
		if (ret != null) {
			success = true;
		}
		return success;
	}

	/**
	 * 为房间中正在游戏的玩家分配手牌
	 * 
	 * @param room
	 * @param
	 */
	public static void assignHandPokerByRoom(Room room) {
		List<Card> cardList = room.getCardList();
		for (PlayerVO p : room.getIngamePlayers()) {
			int seatNum = p.getSeatNum();
			Card[] hankPoker = { cardList.get(seatNum*2), cardList.get(seatNum*2+1) };
			// 玩家手牌
			p.setHandPokers(hankPoker);
		}
		room.setCardList(cardList.subList(12,52));
	}

	/**
	 * 为房间中正在游戏的指定玩家分配手牌
	 *
	 * @param room
	 * @param
	 */
	public static void assignHandPokerByPlayer(Room room, PlayerVO player) {
		List<Card> cardList = room.getCardList();
		// 除去要分配的2张手牌外必须留4张牌不分配作为混淆使用
		if(cardList.size() < 6) {
			return;
		}
		Card[] hankPoker = { cardList.get(0), cardList.get(1) };
		// 玩家手牌
		player.setHandPokers(hankPoker);
		room.setCardList(cardList.subList(2,cardList.size()));
	}

	/**
	 * 为房间中正在游戏的指定玩家随机更换1张手牌
	 *
	 * @param room
	 * @param
	 */
	public static void assignHandPokerByPlayerOneCard(Room room, PlayerVO player) {
		List<Card> cardList = room.getCardList();
		// 除去要分配的1张手牌外必须留4张牌不分配作为混淆使用
		if(cardList.size() < 5) {
			return;
		}
		Random random = new Random();
		int nextInt = random.nextInt(2);
		Card[] hankPoker = { cardList.get(0), player.getHandPokers()[nextInt] };
		// 玩家手牌
		player.setHandPokers(hankPoker);
		room.setCardList(cardList.subList(1,cardList.size()));
	}



	/**
	 * 发公共牌
	 * 
	 * @param room 房间
	 * @param num  数量
	 */
	public static void assignCommonCardByNum(Room room, int num) {
		List<Card> cardList = room.getCardList();
		for (int i = 0; i < num; i++) {
			room.getCommunityCards().add(cardList.get(0).toString());
			room.getCalcCommunityCards().add(cardList.get(0));
			cardList.remove(0);
		}
		int communityCardsSize = room.getCommunityCards().size();
		if(communityCardsSize == 3) {
			room.setCurrentRound("F");
		}
		if(communityCardsSize == 4) {
			room.setCurrentRound("T");
		}
		if(communityCardsSize == 5) {
			room.setCurrentRound("R");
		}
		// 通知房间中的每个玩家
		SkillCardsUtil.assignSkillCardByRoom(room, skillAwardCount, false);
		for(PlayerVO p : room.getIngamePlayers()) {
			PrivateRoom pRoom = new PrivateRoom();
			pRoom.setRoom(room);
			// 私有房间信息（技能卡）
			pRoom.setPlayerSkillCards(p.getPlayerSkillCards());
			pRoom.setPower(p.getPower());
			pRoom.setCommunityCards(room.getCommunityCards());
			RetMsg retMsg = new RetMsg();
			retMsg.setAction("onAssignCommonCard");
			retMsg.setState(1);
			retMsg.setMessage(JsonUtils.toJson(pRoom, PrivateRoom.class));
			TexasUtil.sendMsgToOne(p, JsonUtils.toJson(retMsg, RetMsg.class));
		}
		for(PlayerVO p : room.getWaitPlayers()) {
			PrivateRoom pRoom = new PrivateRoom();
			pRoom.setRoom(room);
			pRoom.setCommunityCards(room.getCommunityCards());
			RetMsg retMsg = new RetMsg();
			retMsg.setAction("onAssignCommonCard");
			retMsg.setState(1);
			retMsg.setMessage(JsonUtils.toJson(pRoom, PrivateRoom.class));
			TexasUtil.sendMsgToOne(p, JsonUtils.toJson(retMsg, RetMsg.class));
		}
	}

	/**
	 * 将一个玩家列表中的玩家全部移动到另一个玩家列表中
	 * 
	 * @param from
	 * @param to
	 */
	public static void movePlayers(List<PlayerVO> from, List<PlayerVO> to) {
		to.addAll(from);
		from.clear();
//		while (from.size() > 0) {
//			to.add(from.get(0));// 添加来源列表的首位到目标列表
//			from.remove(0);// 移除来源列表的首位
//		}
	}

	/**
	 * 获取房间中的玩家数量
	 * 
	 * @param room
	 * @return
	 */
	public static int getRoomPlayerCount(Room room) {
		int playerCount = room.getWaitPlayers().size() + room.getIngamePlayers().size();
		return playerCount;
	}

	/**
	 * 创建一个相应级别的房间
	 * 
	 * @param level
	 */
	public static Room createRoom(int type, int level) {
		Room room = RoomTypeList.getNewRoom(type, level);
		String key = type+"-"+level;
		List<Room> roomList = TexasStatic.roomList.get(key);
		if(roomList == null) {
			roomList = new CopyOnWriteArrayList<Room>();
		}
		roomList.add(room);
 		TexasStatic.roomList.put(key, roomList);
		return room;
	}

	/**
	 * 创建一个相应级别的房间,玩家直接进入
	 * 
	 * @param level
	 */
	public Room createRoomByPlayer(int type, int level, PlayerVO player) {
		Room room = RoomTypeList.getNewRoom(type, level);
		inRoom(room, player);
		String key = type+"-"+level;
		List<Room> roomList = TexasStatic.roomList.get(key);
		if(roomList == null) {
			roomList = new CopyOnWriteArrayList<Room>();
		}
		roomList.add(room);
		TexasStatic.roomList.put(key, roomList);
		return room;
	}

	public void getRoomLevelStats(Channel channel, String message) {
		Room roomMessage = getRoomMessage(message);
		int type = roomMessage.getType();
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> levels = new ArrayList<Map<String, Object>>();
		response.put("type", type);
		response.put("levels", levels);
		for (int level = 0; level <= 2; level++) {
			Room roomConfig = RoomTypeList.roomTypeMap.get(type + "-" + level);
			if (roomConfig == null) {
				continue;
			}
			List<Room> roomList = TexasStatic.roomList.get(type + "-" + level);
			int roomCount = roomList == null ? 0 : roomList.size();
			int playerCount = 0;
			int playingPlayerCount = 0;
			int availableRoomCount = 0;
			if (roomList != null) {
				for (Room room : roomList) {
					if (room == null) {
						continue;
					}
					playerCount += getRoomPlayerCount(room);
					playingPlayerCount += room.getIngamePlayers().size();
					if (isRoomAvailable(room)) {
						availableRoomCount++;
					}
				}
			}
			Map<String, Object> item = new LinkedHashMap<String, Object>();
			item.put("type", type);
			item.put("level", level);
			item.put("smallBet", roomConfig.getSmallBet());
			item.put("bigBet", roomConfig.getBigBet());
			item.put("minChips", roomConfig.getMinChips());
			item.put("maxChips", roomConfig.getMaxChips());
			item.put("maxPlayers", roomConfig.getMaxPlayers());
			item.put("roomCount", roomCount);
			item.put("playerCount", playerCount);
			item.put("playingPlayerCount", playingPlayerCount);
			item.put("availableRoomCount", availableRoomCount);
			levels.add(item);
		}
		sendRoomLobbyMessage(channel, "onRoomLevelStats", 1, JsonUtils.toJsonAll(response, Map.class));
	}

	public void getRoomList(Channel channel, String message) {
		Room roomMessage = getRoomMessage(message);
		Map<String, Object> response = buildRoomListResponse(roomMessage.getType(), roomMessage.getLevel());
		sendRoomLobbyMessage(channel, "onRoomList", 1, JsonUtils.toJsonAll(response, Map.class));
	}

	public void inRoomByRoomNo(Channel channel, String message) {
		Room roomMessage = getRoomMessage(message);
		PlayerVO currPlayer = getPlayerByChannelId(channel.id().asShortText());
		if (currPlayer == null) {
			sendRoomLobbyMessage(channel, "onEnterRoom", 0, "请先登录");
			return;
		}
		Room roomConfig = RoomTypeList.roomTypeMap.get(roomMessage.getType() + "-" + roomMessage.getLevel());
		if (roomConfig == null) {
			sendRoomLobbyMessage(channel, "onEnterRoom", 0, "房间级别不存在");
			return;
		}
		if (currPlayer.getChips() < roomConfig.getMinChips()) {
			sendRoomLobbyMessage(channel, "onEnterRoom", 0, "筹码不足");
			return;
		}
		Room room = findRoom(roomMessage.getType(), roomMessage.getLevel(), roomMessage.getRoomNo());
		if (room == null) {
			sendRoomLobbyMessage(channel, "onEnterRoom", 0, "房间不存在或已关闭");
			return;
		}
		if (!isRoomAvailable(room) && !isPlayerInRoom(room, currPlayer)) {
			sendRoomLobbyMessage(channel, "onEnterRoom", 0, "房间已满，请创建新房间");
			return;
		}
		boolean success = inRoom(room, currPlayer);
		if (!success) {
			sendRoomLobbyMessage(channel, "onEnterRoom", 0, "房间已满，请创建新房间");
			return;
		}
		sendEnterRoomSuccess(currPlayer, room);
	}

	public void createRoomAndIn(Channel channel, String message) {
		Room roomMessage = getRoomMessage(message);
		PlayerVO currPlayer = getPlayerByChannelId(channel.id().asShortText());
		if (currPlayer == null) {
			sendRoomLobbyMessage(channel, "onEnterRoom", 0, "请先登录");
			return;
		}
		Room roomConfig = RoomTypeList.roomTypeMap.get(roomMessage.getType() + "-" + roomMessage.getLevel());
		if (roomConfig == null) {
			sendRoomLobbyMessage(channel, "onEnterRoom", 0, "房间级别不存在");
			return;
		}
		if (currPlayer.getChips() < roomConfig.getMinChips()) {
			sendRoomLobbyMessage(channel, "onEnterRoom", 0, "筹码不足");
			return;
		}
		Room room = createRoomByPlayer(roomMessage.getType(), roomMessage.getLevel(), currPlayer);
		sendEnterRoomSuccess(currPlayer, room);
	}

	private Map<String, Object> buildRoomListResponse(int type, int level) {
		Room roomConfig = RoomTypeList.roomTypeMap.get(type + "-" + level);
		List<Room> roomList = TexasStatic.roomList.get(type + "-" + level);
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> rooms = new ArrayList<Map<String, Object>>();
		int availableRoomCount = 0;
		response.put("type", type);
		response.put("level", level);
		if (roomConfig != null) {
			response.put("smallBet", roomConfig.getSmallBet());
			response.put("bigBet", roomConfig.getBigBet());
			response.put("minChips", roomConfig.getMinChips());
			response.put("maxChips", roomConfig.getMaxChips());
			response.put("maxPlayers", roomConfig.getMaxPlayers());
		}
		if (roomList != null) {
			for (Room room : roomList) {
				if (room == null) {
					continue;
				}
				boolean available = isRoomAvailable(room);
				if (available) {
					availableRoomCount++;
				}
				Map<String, Object> item = new LinkedHashMap<String, Object>();
				item.put("roomNo", room.getRoomNo());
				item.put("type", room.getType());
				item.put("level", room.getLevel());
				item.put("smallBet", room.getSmallBet());
				item.put("bigBet", room.getBigBet());
				item.put("maxPlayers", room.getMaxPlayers());
				item.put("playerCount", getRoomPlayerCount(room));
				item.put("waitPlayerCount", room.getWaitPlayers().size());
				item.put("playingPlayerCount", room.getIngamePlayers().size());
				item.put("gamestate", room.getGamestate().get());
				item.put("available", available);
				rooms.add(item);
			}
		}
		response.put("rooms", rooms);
		response.put("roomCount", rooms.size());
		response.put("availableRoomCount", availableRoomCount);
		return response;
	}

	private Room findRoom(int type, int level, String roomNo) {
		if (roomNo == null || roomNo.trim().length() == 0) {
			return null;
		}
		List<Room> roomList = TexasStatic.roomList.get(type + "-" + level);
		if (roomList == null) {
			return null;
		}
		for (Room room : roomList) {
			if (room != null && roomNo.equals(room.getRoomNo())) {
				return room;
			}
		}
		return null;
	}

	private boolean isPlayerInRoom(Room room, PlayerVO player) {
		if (room == null || player == null || player.getId() == null) {
			return false;
		}
		for (PlayerVO roomPlayer : room.getWaitPlayers()) {
			if (roomPlayer != null && player.getId().equals(roomPlayer.getId())) {
				return true;
			}
		}
		for (PlayerVO roomPlayer : room.getIngamePlayers()) {
			if (roomPlayer != null && player.getId().equals(roomPlayer.getId())) {
				return true;
			}
		}
		return false;
	}

	private boolean isRoomAvailable(Room room) {
		if (room == null) {
			return false;
		}
		return room.getRoomstate() == 1
				&& room.getFreeSeatStack() != null
				&& !room.getFreeSeatStack().isEmpty()
				&& getRoomPlayerCount(room) < room.getMaxPlayers();
	}

	private void sendEnterRoomSuccess(PlayerVO currPlayer, Room room) {
		RetMsg rm = new RetMsg();
		rm.setAction("onEnterRoom");
		rm.setState(1);
		PrivateRoom pRoom = new PrivateRoom();
		pRoom.setRoom(room);
		rm.setMessage(JsonUtils.toJson(pRoom, PrivateRoom.class));
		sendMsgToOne(currPlayer, JsonUtils.toJson(rm, RetMsg.class));
		sendPlayerToOthers(currPlayer, room, "onPlayerEnterRoom");
		roomManager.checkStart(room, 800);
	}

	private void sendRoomLobbyMessage(Channel channel, String action, int state, Object message) {
		RetMsg rm = new RetMsg();
		rm.setAction(action);
		rm.setState(state);
		rm.setMessage(message);
		sendMsgToOne(channel, JsonUtils.toJson(rm, RetMsg.class));
	}

	/**
	 * 移除没有玩家的空房间
	 */
//	public static void removeEmptyRoom() {
//		for (int i = 0; i < TexasStatic.roomList.size(); i++) {
//			Room room = TexasStatic.roomList.get(i);
//			int count = room.getIngamePlayers().size() + room.getWaitPlayers().size();
//			if (count == 0) {
//				TexasStatic.roomList.remove(i);
//			}
//		}
//	}

	/**
	 * 发送表情或文字
	 * 
	 * @param
	 * @param
	 */
	public static void sendMessage(Channel channel, String message) {
		PlayerVO p = getPlayerByChannelId(channel.id().asShortText());
		if (p != null) {
			RetMsg retMsg = new RetMsg();
			retMsg.setMessage(message);
			retMsg.setAction("onPlayerSendMessage");
			retMsg.setState(1);
			String msg = JsonUtils.toJson(retMsg, RetMsg.class);
			sendMsgToPlayerByRoom(p.getRoom(), msg);
		}
	}

	/**
	 * 给房间中正在游戏的玩家发送消息
	 * 
	 * @param room
	 * @param msg
	 */
	public static void sendMsgToIngamePlayerByRoom(Room room, String msg) {
		sendMsgToList(room.getIngamePlayers(), msg);
	}

	/**
	 * 给房间中处于等待状态的玩家发消息
	 * 
	 * @param room
	 * @param msg
	 */
	public static void sendMsgToWaitPlayerByRoom(Room room, String msg) {
		sendMsgToList(room.getWaitPlayers(), msg);
	}

	/**
	 * 给房间中的每一个玩家发消息
	 * 
	 * @param room
	 * @param msg
	 */
	public static void sendMsgToPlayerByRoom(Room room, String msg) {
		sendMsgToIngamePlayerByRoom(room, msg);
		sendMsgToWaitPlayerByRoom(room, msg);
	}

	/**
	 * 给一组玩家发消息
	 * 
	 * @param playerList
	 * @param msg
	 */
	public static void sendMsgToList(List<PlayerVO> playerList, String msg) {
		playerList.parallelStream().forEach(player -> sendMsgToPlayer(player, msg));
		logger.info("toAllPlayers:" + msg);
	}

	/**
	 * 给一个玩家发消息,批量发送调用
	 * 
	 * @param player
	 * @param msg
	 */
	public static void sendMsgToPlayer(PlayerVO player, String msg) {
		if (player != null && player.getChannel() != null) {
			NettyWebSocketHandler.sendMessage(player.getChannel(), msg);
		}
	}

	public static void sendMsgToOne(PlayerVO p, String msg) {
		if (p != null) {
			Channel channel = p.getChannel();
			if (channel != null) {
				NettyWebSocketHandler.sendMessage(channel, msg);
				logger.info("toOne:" + msg);
			}
		}
	}

	public static void sendMsgToOne(Channel channel, String msg) {
		if (channel != null) {
			NettyWebSocketHandler.sendMessage(channel, msg);
			logger.info("toOne:" + msg);
		}
	}

	public static PlayerVO getPlayerByChannelId(String channelId) {
		PlayerVO p = TexasStatic.loginPlayerMap.get(channelId);
		return p;
	}

	public static Channel getChannelByPlayer(PlayerVO p) {
		Channel channel = p.getChannel();
		return channel;
	}

	/**
	 * 更新下一个轮到的玩家
	 * 
	 * @param room
	 * @return
	 */
	public static void updateNextTurn(Room room) {
		int thisturn = room.getNextturn();
		// TODO 特殊判断。。。
		thisturn = getNextSeatNum(thisturn, room, true);
		room.setNextturn(thisturn);
	}

	/**
	 * 更新下一个轮到的玩家
	 * 
	 * @param clockwise 是否顺时针
	 * @param room
	 * @return
	 */
	public static void updateNextTurn(Room room, boolean clockwise) {
		int thisturn = room.getNextturn();
		thisturn = getNextSeatNum(thisturn, room, clockwise);
		room.setNextturn(thisturn);
	}

	/**
	 * 获取下一个可操作玩家的座位号
	 * 
	 * @param
	 */
	public static int getNextSeatNum(int seatNum, Room room) {
		int begin = seatNum;
		while (true) {
			seatNum = getNextNum(seatNum, room);
			PlayerVO pi = getPlayerBySeatNum(seatNum, room.getIngamePlayers());
			if (pi != null && !pi.isFold() && pi.getBodyChips() != 0) {
				break;
			}
			// 已经循环一圈
			if (begin == seatNum) {
				break;
			}
		}
		return seatNum;
	}

	/**
	 * 获取下一个可操作玩家的座位号
	 * 
	 * @param clockwise 是否顺时针
	 * @param
	 */
	public static int getNextSeatNum(int seatNum, Room room, boolean clockwise) {
		int begin = seatNum;
		while (true) {
			seatNum = getNextNum(seatNum, room, clockwise);
			PlayerVO pi = getPlayerBySeatNum(seatNum, room.getIngamePlayers());
			if (pi != null && !pi.isFold() && pi.getBodyChips() != 0) {
				break;
			}
			// 已经循环一圈
			if (begin == seatNum) {
				break;
			}
		}
		return seatNum;
	}

	/**
	 * 获取下一个玩家座位号,得到下一个dealer使用
	 * 
	 * @param
	 */
	public static int getNextSeatNumDealer(int seatNum, Room room) {
		boolean finded = false;
		int begin = seatNum;
		while (!finded) {
			seatNum = getNextNum(seatNum, room);
			for (PlayerVO pw : room.getWaitPlayers()) {
				if (pw.getSeatNum() == seatNum) {
					finded = true;
					break;
				}
			}
			for (PlayerVO pi : room.getIngamePlayers()) {
				if (pi.getSeatNum() == seatNum) {
					finded = true;
					break;
				}
			}
			// 已经循环一圈
			if (begin == seatNum) {
				break;
			}
		}
		return seatNum;
	}

	/**
	 * 
	 * 返回下一个座位号
	 * 
	 * @param seatNum
	 * @param room
	 * @return
	 */
	private static int getNextNum(int seatNum, Room room) {
		int nextSeatNum = seatNum + 1;
		if (nextSeatNum >= room.getMaxPlayers()) {
			nextSeatNum = 0;
		}
		return nextSeatNum;
	}

	/**
	 * 
	 * 返回下一个座位号
	 * 
	 * @param clockwise 是否顺时针
	 * @param seatNum
	 * @param room
	 * @return
	 */
	private static int getNextNum(int seatNum, Room room, boolean clockwise) {
		if (clockwise) {
			return getNextNum(seatNum, room);
		} else {
			int nextSeatNum = seatNum - 1;
			if (nextSeatNum < 0) {
				nextSeatNum = room.getMaxPlayers() - 1;
			}
			return nextSeatNum;
		}
	}

	/**
	 * 根据座位号返回玩家
	 * 
	 * @param seatNum
	 * @param
	 * @return
	 */
	public static PlayerVO getPlayerBySeatNum(int seatNum, List<PlayerVO> playerList) {
		Optional<PlayerVO> player = null;
		player = playerList.parallelStream().filter(p -> p.getSeatNum() == seatNum).findFirst();
		if (player.isPresent()) {
			return player.get();
		} else {
			return null;
		}
	}

	/**
	 * 每局开始时更新下一个dealer
	 * 
	 * @param room
	 * @return
	 */
	public static void updateNextDealer(Room room) {
		int d = room.getDealer();
		d = getNextSeatNumDealer(d, room);
		room.setDealer(d);
	}

	/**
	 * 改变玩家chips的方法
	 * 
	 * @param p
	 * @param chips
	 */
	public static void changePlayerChips(PlayerVO p, Long chips) {
		synchronized (p) {
			p.setBodyChips(p.getBodyChips() + chips);
		}
	}

	/**
	 * 按值排序一个map
	 * 
	 * @param oriMap
	 * @return
	 */
	public static Map<Integer, Long> sortMapByValue(Map<Integer, Long> oriMap) {
		Map<Integer, Long> sortedMap = new LinkedHashMap<Integer, Long>();
		if (oriMap != null && !oriMap.isEmpty()) {
			List<Entry<Integer, Long>> entryList = new ArrayList<Entry<Integer, Long>>(oriMap.entrySet());
			Collections.sort(entryList, new Comparator<Entry<Integer, Long>>() {
				public int compare(Entry<Integer, Long> entry1, Entry<Integer, Long> entry2) {
					Long value1 = 0l, value2 = 0l;
					value1 = entry1.getValue();
					value2 = entry2.getValue();
					return value1.compareTo(value2);
				}
			});
			Iterator<Entry<Integer, Long>> iter = entryList.iterator();
			Entry<Integer, Long> tmpEntry = null;
			while (iter.hasNext()) {
				tmpEntry = iter.next();
				sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
			}
		}
		oriMap.clear();
		return sortedMap;
	}

	/**
	 * 求Map<Integer, Long>中Value(值)的最大值
	 * 
	 * @param map
	 * @return
	 */
	public static Long getMaxValue(Map<Integer, Long> map) {
		if (map == null)
			return null;
		Collection<Long> c = map.values();
		Object[] obj = c.toArray();
		Arrays.sort(obj);
		return (Long) obj[obj.length - 1];
	}

	/**
	 * 进入房间
	 * 
	 * @param session
	 * @param message
	 */
	public void inRoom(Channel channel, String message) {
		Room roomMessage = getRoomMessage(message);
		RetMsg rm = new RetMsg();
		rm.setAction("onEnterRoom");
		rm.setState(1);
		PlayerVO currPlayer = getPlayerByChannelId(channel.id().asShortText());
		if (currPlayer == null) {
			rm.setState(0);
			rm.setMessage("请先登录");
			String retMsg = JsonUtils.toJson(rm, RetMsg.class);
			sendMsgToOne(currPlayer, retMsg);
			return;
		}
		// 进入房间一个非机器人，则进入机器人陪玩
		if (!currPlayer.getUserName().contains("robot")) {
			Date now = new Date();
			//// 创建机器人
//			RobotManager.init(RobotManager.MAX_ROBOT_COUNT);
			Date costEnd = new Date();
			long cost = costEnd.getTime() - now.getTime();
			if (cost > 100) {
				logger.error("add robot:" + message + " cost Millisecond" + cost);
			}
		}

		// 从数据库重新更新玩家筹码

//		PlayerVO upPlayer = new PlayerVO();
//		upPlayer.setId(currPlayer.getId());
//		PlayerService pservice = (PlayerService) SpringUtil.getBean("playerService");
//		upPlayer = pservice.selectPlayer(upPlayer);
//		long bodyChips = currPlayer.getBodyChips();
//		long restChips = upPlayer.getChips() - bodyChips;
//		currPlayer.setChips(restChips);
//		currPlayer.setChips(bodyChips);

		Room roomConfig = RoomTypeList.roomTypeMap.get(roomMessage.getType()+"-"+roomMessage.getLevel());

		if (currPlayer.getChips() < roomConfig.getMinChips()) {
			rm.setState(0);
			rm.setMessage("筹码不足");
			String retMsg = JsonUtils.toJson(rm, RetMsg.class);
			sendMsgToOne(currPlayer, retMsg);
			return;
		}
		// 查找空房间，没有则创建新房间
		Room usableRoom = getUsableRoomThenIn(roomMessage.getType(), roomMessage.getLevel(), currPlayer);
		logger.info("进入房间编号："+usableRoom.getRoomNo() +"  玩家编号："+currPlayer.getId()+"  玩家名称："+currPlayer.getUserName());
		PrivateRoom pRoom=new PrivateRoom();
		pRoom.setRoom(usableRoom);
		String roominfo = JsonUtils.toJson(pRoom, PrivateRoom.class);
		rm.setMessage(roominfo);
		// 通知玩家加入房间成功
		String retMsg = JsonUtils.toJson(rm, RetMsg.class);
		sendMsgToOne(currPlayer, retMsg);
		// 通知所有房间内玩家，有玩家加入
		sendPlayerToOthers(currPlayer, usableRoom, "onPlayerEnterRoom");
		// 检查房间是否可以开始游戏，尽快开始
		roomManager.checkStart(usableRoom, 800);
	}

	/**
	 * 退出房间
	 * 
	 * @param session
	 * @param message
	 * @param sendOrNot 是否向退出房间的玩家发送退出成功消息
	 */
	public void outRoom(Channel channel, String message, boolean sendOrNot) {
		PlayerVO p = TexasStatic.loginPlayerMap.get(channel.id().asShortText());

		if (sendOrNot) {
			// 告诉自己离开
			RetMsg rm = new RetMsg();
			rm.setAction("onOutRoom");
			rm.setState(1);
			rm.setMessage(JsonUtils.toJson(p, PlayerVO.class));
			String retMsg = JsonUtils.toJson(rm, RetMsg.class);
			sendMsgToOne(p, retMsg);
		}
		// 告诉其他玩家有人离开
		outRoom(p);

	}

	public static Room getRoomMessage(String message) {
		Room room = JsonUtils.fromJson(message, Room.class);
		return room;
	}

	/**
	 * 向除currPlayer之外的玩家发送currPlayer玩家信息
	 * 
	 * @param currPlayer
	 * @param room
	 * @param action
	 */
	public static void sendPlayerToOthers(PlayerVO currPlayer, Room room, String action) {
		String currPlayerInfo = JsonUtils.toJson(currPlayer, PlayerVO.class);
		RetMsg rm_inRoom = new RetMsg();
		rm_inRoom.setAction(action);
		rm_inRoom.setState(1);
		rm_inRoom.setMessage(currPlayerInfo);
		String inRoomMessage = JsonUtils.toJson(rm_inRoom, RetMsg.class);
		sendMessageToOtherPlayers(currPlayer.getId(), room, inRoomMessage);
	}

	/**
	 * 向除currPlayer之外的玩家发送message
	 * 
	 * @param selfId
	 * @param room
	 * @param message
	 */
	public static void sendMessageToOtherPlayers(String selfId, Room room, String message) {
		if (room == null) {
			return;
		}
		// 通知其他玩家
		List<PlayerVO> waitPlayers = room.getWaitPlayers();
		for (PlayerVO p : waitPlayers) {
			if (p != null && p.getId() != null && !p.getId().equals(selfId)) {
				Channel channel = getChannelByPlayer(p);
				NettyWebSocketHandler.sendMessage(channel, message);
			}

		}
		List<PlayerVO> ingamePlayers = room.getIngamePlayers();
		for (PlayerVO p : ingamePlayers) {
			if (p != null && p.getId() != null && !p.getId().equals(selfId)) {
				Channel channel = getChannelByPlayer(p);
				NettyWebSocketHandler.sendMessage(channel, message);
			}

		}
	}

	public static void sendErrorMsg(String action, String errorMsg, PlayerVO playerVO) {
		RetMsg rm = new RetMsg();
		rm.setAction(action);
		rm.setState(0);
		rm.setMessage(errorMsg);
		String retMsg = JsonUtils.toJson(rm, RetMsg.class);
		sendMsgToOne(playerVO, retMsg);
	}
}
