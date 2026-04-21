/**
 * Created by xll on 2016/10/30.
 */
var myInfo = {};
var roomInfo = {};
var nextRoundSkillAction = "";
// 陷阱信息提示开关（如果触发闪避之后陷阱回调不在提示该信息）
var trapRemindSwitch = true;
// 最大带入筹码量
var maxChips;
var isExitRoom = false;
var lobbyRoomState = {
    type: null,
    level: null,
    levelStats: null,
    roomList: null
};

function syncScenePanels() {
    if (gamebackGroundType == "login") {
        $("#registDiv").show();
        $("#roomDiv").hide();
        $("#infoDiv").show();
        $("#propsDiv").hide();
        return;
    }
    if (gamebackGroundType == "lobby") {
        $("#registDiv").hide();
        $("#roomDiv").show();
        $("#infoDiv").show();
        $("#propsDiv").hide();
        updateLobbyUserInfo();
        return;
    }
    $("#registDiv").hide();
    $("#roomDiv").hide();
    $("#infoDiv").hide();
    $("#propsDiv").show();
}

function updateLobbyUserInfo() {
    if ($("#lobbyUserInfo").length === 0) {
        return;
    }
    if (myInfo != null && myInfo.userName != null && myInfo.userName !== "") {
        $("#lobbyUserInfo").html("当前玩家：<strong>" + myInfo.userName + "</strong>");
    } else {
        $("#lobbyUserInfo").text("当前玩家：未登录");
    }
}

function showLobbyModePage() {
    $("#lobbyModePage").show();
    $("#lobbyLevelPage").hide();
    $("#lobbyRoomListPage").hide();
    lobbyRoomState.type = null;
    lobbyRoomState.level = null;
}

function showLobbyLevelPage(type) {
    $("#lobbyModePage").hide();
    $("#lobbyLevelPage").show();
    $("#lobbyRoomListPage").hide();
    $("#lobbyLevelTitle").text(getRoomTypeName(type) + " · 选择盲注级别");
    $("#blindLevelGrid").html("<div class='lobby-loading'>正在读取当前盲注级别的房间与玩家数据...</div>");
}

function showLobbyRoomListPage(type, level) {
    $("#lobbyModePage").hide();
    $("#lobbyLevelPage").hide();
    $("#lobbyRoomListPage").show();
    $("#roomListTitle").text(getBlindLevelName(level) + " · 房间列表");
    $("#roomListSubtitle").text("盲注 " + getBlindText(level) + "，请选择未满房间入座；如果没有空位，可以创建新房间等待其他玩家。");
    $("#roomListGrid").html("<div class='lobby-loading'>正在读取当前盲注下所有房间...</div>");
}

function getRoomTypeName(type) {
    if (type == 1) {
        return "技能卡牌版";
    }
    if (type == 2) {
        return "自定义卡组版";
    }
    return "传统德州扑克";
}

function getBlindLevelName(level) {
    if (level == 1) {
        return "进阶场";
    }
    if (level == 2) {
        return "高手场";
    }
    return "新手场";
}

function getBlindText(levelInfoOrLevel) {
    if (typeof levelInfoOrLevel === "object" && levelInfoOrLevel != null) {
        return levelInfoOrLevel.smallBet + " / " + levelInfoOrLevel.bigBet;
    }
    if (levelInfoOrLevel == 1) {
        return "100 / 200";
    }
    if (levelInfoOrLevel == 2) {
        return "250 / 500";
    }
    return "50 / 100";
}

function selectRoomMode(type) {
    if (type == 0) {
        enterRoom(0, 0);
        return;
    }
    lobbyRoomState.type = type;
    showLobbyLevelPage(type);
    requestRoomLevelStats(type);
}

function requestRoomLevelStats(type) {
    var data = {};
    data.action = mapping.getRoomLevelStats;
    data.type = type;
    sendWsPayload(data);
}

function selectBlindLevel(type, level) {
    lobbyRoomState.type = type;
    lobbyRoomState.level = level;
    showLobbyRoomListPage(type, level);
    requestRoomList(type, level);
}

function requestRoomList(type, level) {
    var data = {};
    data.action = mapping.getRoomList;
    data.type = type;
    data.level = level;
    sendWsPayload(data);
}

function joinRoomByNo(roomNo, type, level) {
    var data = {};
    data.action = mapping.joinRoomByNo;
    data.roomNo = roomNo;
    data.type = type;
    data.level = level;
    rememberRoom(type, level);
    setMessageStatus("正在进入房间 " + roomNo + "...", "warning");
    sendWsPayload(data);
}

function createLevelRoom(type, level) {
    var data = {};
    data.action = mapping.createRoom;
    data.type = type;
    data.level = level;
    rememberRoom(type, level);
    setMessageStatus("正在创建新房间...", "warning");
    sendWsPayload(data);
}

function renderBlindLevels(levels, type) {
    if (levels == null || levels.length === 0) {
        $("#blindLevelGrid").html("<div class='lobby-loading'>当前模式还没有可用盲注配置。</div>");
        return;
    }
    var html = "";
    for (var i in levels) {
        var item = levels[i];
        html += "<button class='blind-card level-" + item.level + "' onclick='selectBlindLevel(" + type + "," + item.level + ")'>";
        html += "<span class='room-tag'>" + getBlindLevelName(item.level) + "</span>";
        html += "<div class='room-name'>盲注 " + escapeMessageHtml(getBlindText(item)) + "</div>";
        html += "<div class='room-desc'>带入 " + escapeMessageHtml(item.minChips) + " - " + escapeMessageHtml(item.maxChips) + "，最多 " + escapeMessageHtml(item.maxPlayers) + " 人同桌。</div>";
        html += "<div class='lobby-stat-grid'>";
        html += "<div class='lobby-stat'><strong>" + escapeMessageHtml(item.playerCount) + "</strong><span>入座玩家</span></div>";
        html += "<div class='lobby-stat'><strong>" + escapeMessageHtml(item.playingPlayerCount) + "</strong><span>对局中</span></div>";
        html += "<div class='lobby-stat'><strong>" + escapeMessageHtml(item.roomCount) + "</strong><span>房间数</span></div>";
        html += "<div class='lobby-stat'><strong>" + escapeMessageHtml(item.availableRoomCount) + "</strong><span>可加入</span></div>";
        html += "</div>";
        html += "</button>";
    }
    $("#blindLevelGrid").html(html);
}

function renderRoomList(roomListInfo) {
    var rooms = roomListInfo.rooms || [];
    var html = "";
    $("#roomListTitle").text(getBlindLevelName(roomListInfo.level) + " · 房间列表");
    $("#roomListSubtitle").text("盲注 " + roomListInfo.smallBet + " / " + roomListInfo.bigBet + "，当前 " + roomListInfo.roomCount + " 个房间，" + roomListInfo.availableRoomCount + " 个可加入。");
    if (rooms.length === 0) {
        html += "<div class='empty-room-list'>当前盲注还没有房间，创建一个新房间后会自动坐下等人。</div>";
    } else {
        for (var i in rooms) {
            var room = rooms[i];
            var statusClass = room.available ? "open" : "full";
            var statusText = room.available ? "可加入" : "已满";
            var gameText = room.gamestate == 1 ? "对局中" : (room.gamestate == 2 ? "结算中" : "等待中");
            html += "<div class='room-list-card " + statusClass + "'>";
            html += "<div class='room-list-main'>";
            html += "<div class='room-list-no'>" + escapeMessageHtml(room.roomNo) + "</div>";
            html += "<div class='room-list-meta'>";
            html += "<span>" + gameText + "</span>";
            html += "<span>玩家 " + escapeMessageHtml(room.playerCount) + " / " + escapeMessageHtml(room.maxPlayers) + "</span>";
            html += "<span>等待 " + escapeMessageHtml(room.waitPlayerCount) + "</span>";
            html += "<span>对局 " + escapeMessageHtml(room.playingPlayerCount) + "</span>";
            html += "</div>";
            html += "</div>";
            if (room.available) {
                html += "<button class='room-join-btn' onclick=\"joinRoomByNo('" + escapeMessageHtml(room.roomNo) + "'," + room.type + "," + room.level + ")\">进入坐下</button>";
            } else {
                html += "<button class='room-join-btn disabled' disabled>" + statusText + "</button>";
            }
            html += "</div>";
        }
    }
    html += "<button class='create-room-btn' onclick='createLevelRoom(" + roomListInfo.type + "," + roomListInfo.level + ")'>创建新房间并坐下</button>";
    $("#roomListGrid").html(html);
}

$(function () {
    syncScenePanels();
    $("#name, #password").on("keydown", function (event) {
        if (event.keyCode === 13) {
            login();
        }
    });
});

// 注册，传入账号密码
function regist() {
    var data = {};
    data.action = mapping.regist;
    data.userName = $("#name").val();
    data.userpwd = $("#password").val();
    sendWsPayload(data);
}
// 登陆，传入账号密码
function login() {
    var userName = $("#name").val();
    var userpwd = $("#password").val();
    rememberCredentials(userName, userpwd);
    sendWsPayload(buildLoginPayload(userName, userpwd));
}
// 加入房间，传入房间类型
function enterRoom(type,level) {
    $("#infoDiv").hide();
    var data = {};
    data.action = mapping.enterRoom;
    data.type = 0;
    data.level = 0;
    if (level != null && type != null) {
        data.level = level;
        data.type = type;
    }
    rememberRoom(data.type, data.level);
    sendWsPayload(data);
    gamebackGroundType = "room";
    syncScenePanels();
    // 根据房间级别，更换桌面背景
    startIndexBackground = data.type;
    backGroundDrawed = false;
    ControllButtonsDrawed = false;
}
function exitRoom() {
    var data = {};
    data.action = mapping.exitRoom;
    clearRoomMemory();
    sendWsPayload(data);
    $("#assignChipsInfo").hide();
}
// 站起
function standUp() {
    var data = {};
    data.action = mapping.standUp;
    sendWsPayload(data);
}
// 坐下
function sitDown() {
    var data = {};
    data.action = mapping.sitDown;
    sendWsPayload(data);
    return false;
}
// 加注
function raise() {
    var data = {};
    data.action = mapping.betChips;
    data.inChips = DrawSlipBar.betChips;
    sendWsPayload(data);
}
// 全下
function allIn() {
    var data = {};
    data.action = mapping.betChips;
    data.inChips = myInfo.bodyChips;
    sendWsPayload(data);
}
// 过牌
function check() {
    if (checkOrCall == "call") {
        call();
        return;
    }
    var data = {};
    data.action = mapping.check;
    sendWsPayload(data);
}
// 跟注
function call() {
    if (checkOrCall == "check") {
        check();
        return;
    }
    var data = {};
    data.action = mapping.betChips;
    data.inChips = roundMaxBet - myInfo.betChips;
    if (data.inChips <= 0) {
        return;
    }
    // 身上不够则allin
    if (myInfo.bodyChips < roundMaxBet) {
        data.inChips = myInfo.bodyChips;
    }
    sendWsPayload(data);
}
// 弃牌
function fold() {
    var data = {};
    data.action = mapping.fold;
    sendWsPayload(data);
}
//获取排行榜
function getRankList() {
    var data = {};
    data.action = mapping.getRankList;
    sendWsPayload(data);
}
// 使用技能
function useSkill(skillDictionaryNo, destPlayerId, tokenId, reverseRound) {
    $("#selectPlayer").hide();
    var data = {};
    data.action = mapping.useSkill;
    data.destPlayerId = destPlayerId; // 目标id（指向性主动技释放目标）
    data.skillDictionaryNo = skillDictionaryNo;
    data.tokenId = tokenId;
    data.reverseRound = reverseRound; // 使用倒转乾坤时次字段生效，表示需要重发哪条街的公共牌
    sendWsPayload(data);
}
// 使用技能后回调函数
function onUseSkill(e, data) {
    $("#selectPlayerContent").html("");
    $("#selectPlayer").hide();
    $("#selectReverseRoundContent").html("");
    $("#selectReverseRound").hide();
    var skillMessage = '';
    var skillDictionaryName;
    var skillDescription;
    var skillConstrains;
    var destPlayerName;
    var srcPlayerName;
    var thirdDestPlayerName;
    var skillDefenseCard;
    if (data.state == 1) {
        roomInfo = JSON.parse(data.message);
        skillDictionaryName = roomInfo.skillDictionaryName;
        skillDescription = roomInfo.skillDescription;
        skillConstrains = roomInfo.skillConstrains;
        destPlayerName = roomInfo.destPlayerName;
        srcPlayerName = roomInfo.srcPlayerName;
        thirdDestPlayerName = roomInfo.thirdDestPlayerName;
        skillDefenseCard = roomInfo.skillDefenseCard;
        nextRoundSkillAction = roomInfo.nextRoundSkillAction;
        var thisRoundSkillAction = roomInfo.thisRoundSkillAction;
        var cards = roomInfo.communityCards;

        // var skillTrapCard = roomInfo.skillTrapCard;
        if(cards != null) {
            commonCards = [];
            // 加入公共牌列表
            for (var i in cards) {
                commonCards.push(cards[i]);
            }
        }

        commonCardsDrawed = false;
        commonSkillDrawed = false;

        // 重画当前玩家手牌
        for (var i in players) {
            var player = players[i];
            if(player.id == myInfo.id) {
                player.cardDrawed = false;
                player.cards = roomInfo.handPokers;
            }
        }

        // 如果使用技能后返回下一个动作为allin，则直接生效
        if(nextRoundSkillAction == "allin") {
            allIn();
        }
        // if(thisRoundSkillAction == "allin") {
        //     allIn();
        // }
        // if(thisRoundSkillAction == "checkOrCall") {
        //     check();
        // }
        // if(thisRoundSkillAction == "fold") {
        //     fold();
        // }

        if(destPlayerName == '') {
            skillMessage = "玩家 "+srcPlayerName+" 使用了："+skillDictionaryName+"";
        } else {
            skillMessage = "玩家 "+srcPlayerName+" 对玩家 "+destPlayerName+" 使用了："+skillDictionaryName+"";
        }
    } else {
        skillMessage = data.message;
    }
    drawSkillMessage(skillMessage);

    if(destPlayerName == '') {
        skillMessage = "玩家 <font color='#ff7f50'>"+srcPlayerName+"</font> 使用了：<font color='red'>"+skillDictionaryName+"</font>";
    } else {
        skillMessage = "玩家 <font color='#ff7f50'>"+srcPlayerName+"</font> 对玩家 <font color='#ff7f50'>"+destPlayerName+"</font> 使用了：<font color='red'>"+skillDictionaryName+"</font>";
    }
    if(thirdDestPlayerName != '') {
        skillMessage += " 但最终生效玩家为 <font color='#ff7f50'>"+thirdDestPlayerName+"</font>";
    }
    if (data.state == 1) {
        skillConstrains = (skillConstrains != null && skillConstrains != '') ? skillConstrains : "无";
        appendGameMessage(skillMessage+"；技能描述："+skillDescription+"；限制条件：("+ skillConstrains +")", "accent", true);
        if(skillDefenseCard != null && skillDefenseCard != undefined) {
            appendGameMessage("<font color='blue'>(防御)</font> 被 "+destPlayerName+" 的 <font color='blue'>"+skillDefenseCard.skillNameZh+"</font> 抵挡；技能描述："+skillDefenseCard.description+"；限制条件：("+ skillDefenseCard.constrains +")", "accent", true);
        }
    } else {
        appendGameMessage(data.message, "danger");
    }

    setTimeout("clearSkillMessage()", 3000);
}

// 触发陷阱技能后回调函数
function onTrapSkill(e, data) {
    var skillTrapCard;
    var srcPlayerName;
    if (data.state == 1) {
        roomInfo = JSON.parse(data.message);
        srcPlayerName = roomInfo.srcPlayerName;
        var cards = roomInfo.communityCards;
        skillTrapCard = roomInfo.skillTrapCard;
        if(cards != null) {
            commonCards = [];
            // 加入公共牌列表
            for (var i in cards) {
                commonCards.push(cards[i]);
            }
        }

        commonCardsDrawed = false;
        commonSkillDrawed = false;

        // 重画当前玩家手牌
        for (var i in players) {
            var player = players[i];
            if(player.id == myInfo.id) {
                player.cardDrawed = false;
                player.cards = roomInfo.handPokers;
            }
        }
    }

    if (data.state == 1) {
        if(skillTrapCard != null && skillTrapCard != undefined) {
            if(trapRemindSwitch) {
                appendGameMessage("<font color='green'>(陷阱)</font> 玩家 <font color='#ff7f50'>"+srcPlayerName+"</font> 触发 "+skillTrapCard.usedPlayer.userName+" 的 <font color='green'>"+skillTrapCard.skillNameZh+"</font>；技能描述："+skillTrapCard.description+"；限制条件：("+ skillTrapCard.constrains +")", "warning", true);
            }
        }
    } else {
        appendGameMessage(data.message, "danger");
    }
    trapRemindSwitch = true;
}

// 触发陷阱闪避类防御技能后回调函数
function onAvoidTrap(e, data) {
    if (data.state == 1) {
        roomInfo = JSON.parse(data.message);
        var srcPlayerName = roomInfo.srcPlayerName;
        var skillDefenseCard = roomInfo.skillDefenseCard;
        var skillTrapCard = roomInfo.skillTrapCard;

        if(skillDefenseCard != null && skillDefenseCard != undefined && skillTrapCard != null && skillTrapCard != undefined) {
            trapRemindSwitch = false;
            appendGameMessage("<font color='blue'>(防御)</font> "+srcPlayerName+" 的 <font color='blue'>"+skillDefenseCard.skillNameZh+"</font> 抵挡；技能描述："+skillDefenseCard.description+"；限制条件："+ skillDefenseCard.constrains, "accent", true);
            appendGameMessage("躲过玩家 "+skillTrapCard.usedPlayer.userName+" 的陷阱 <font color='green'>"+skillTrapCard.skillNameZh+"</font>；技能描述："+skillTrapCard.description+"；限制条件："+ skillTrapCard.constrains, "warning", true);
        }
    } else {
        appendGameMessage(data.message, "danger");
    }
}

//收到排行榜数据
function onGetRankList(e, data) {
    if (data.state == 1) {
        var rankList = JSON.parse(data.message);
        drawRankList(rankList);
    }
}
// 登陆结果
function onLogin(e, data) {
    if (data.state == 1) {
        myInfo = JSON.parse(data.message);
        // myInfo = data.message;
        gamebackGroundType = "lobby";
        syncScenePanels();
        showLobbyModePage();
        if (shouldRejoinRoom()) {
            enterRoom(texasWsState.room.type, texasWsState.room.level);
            markRoomRejoinCompleted();
        }
    } else {
        clearCredentials();
        setGameMessage("登录失败", "danger");
    }
}
// 注册结果
function onRegister(e, data) {
    if (data.state == 1) {
        // 注册成功后自动登陆
        login();
        $("#name").val("");
        $("#password").val("");
        setGameMessage("注册成功，正在自动登录", "success");
    } else {
        setGameMessage("注册失败", "danger");
    }
}
function onRoomLevelStats(e, data) {
    if (data.state == 1) {
        var levelStats = JSON.parse(data.message);
        lobbyRoomState.levelStats = levelStats;
        renderBlindLevels(levelStats.levels, levelStats.type);
    } else {
        $("#blindLevelGrid").html("<div class='lobby-loading danger'>" + escapeMessageHtml(data.message) + "</div>");
    }
}
function onRoomList(e, data) {
    if (data.state == 1) {
        var roomListInfo = JSON.parse(data.message);
        lobbyRoomState.roomList = roomListInfo;
        renderRoomList(roomListInfo);
    } else {
        $("#roomListGrid").html("<div class='lobby-loading danger'>" + escapeMessageHtml(data.message) + "</div>");
    }
}
function onEnterRoom(e, data) {
    if (data.state == 1) {
        $.texasMusic.stopBackMu();
        gamebackGroundType = "room";
        syncScenePanels();
        clearPotChips();
        clearCommonCards();
        clearSkillDiv();
        roomInfo = JSON.parse(data.message);
        // 等待玩家列表
        var waitPlayers = roomInfo.waitPlayers;
        // 开局重置ingame状态
        for (var i in waitPlayers) {
            var p = waitPlayers[i];
            p.ingame = false;
            p.cards = [];
            p.cardDrawed = true;
        }
        // 游戏中玩家列表
        var ingamePlayers = roomInfo.ingamePlayers;
        // 游戏中玩家手牌赋值
        for (var i in ingamePlayers) {
            var p = ingamePlayers[i];
            p.ingame = true;
            // 清空手牌
            p.cards = null;
            p.cardDrawed = false;
        }
        // 合并房间所有玩家列表
        var allPlayers = waitPlayers.concat(ingamePlayers);
        //找到自己的信息
        for (var i in allPlayers) {
            var player = allPlayers[i];
            if (player.id == myInfo.id) {
                myInfo = player;
            }
            player.infoDrawed = false;
        }
        // 根据座位号排序
        allPlayers.sort(function (playerA, playerB) {
            return playerA.seatNum - playerB.seatNum
        });
        players = allPlayers;
        // 设置slipbar最大下注参数
        DrawSlipBar.betChipsMax = myInfo.bodyChips;
        commonCards = roomInfo.communityCards;
        commonCardsDrawed = false;
        pot = roomInfo.betAmount;
        drawRoomInfos();
        //如果在游戏结束阶段
        winnerList = roomInfo.winPlayersMap;
        drawWinners();
    } else {
        setGameMessage("进入房间失败：" + data.message, "danger");
    }
}
// 本人退出房间
function onOutRoom() {
    gamebackGroundType = "lobby";
    lobbyBackGroundLoaded = false;
    LobbyButtonsDrawed = false;
    removeAllPlayers();
    syncScenePanels();
    showLobbyModePage();
    clearRoomMemory();
    $.texasMusic.playBackMu();
}
// 其他玩家加入房间
function onPlayerEnterRoom(e, data) {
    if (data.state == 1) {
        var player = JSON.parse(data.message);
        createPlayer(player);
    }
}
function onPlayerLeaveRoom(e, data) {
    if (data.state == 1) {
        var player = JSON.parse(data.message);
        removePlayer(player);
    }
}
// 发公共牌
function onAssignCommonCard(e, data) {
    if (data.state == 1) {
        roundMaxBet = 0;
        // var cards = JSON.parse(data.message);
        roomInfo = JSON.parse(data.message);
        var cards = roomInfo.communityCards;
        commonCards = [];
        // 加入公共牌列表
        for (var i in cards) {
            commonCards.push(cards[i]);
        }
        commonCardsDrawed = false;
        commonSkillDrawed = false;
        // 汇总本轮筹码
        playerChipsToPot();
        $.texasMusic.playSendCardMu();
    }
}
// 汇总本轮筹码
function playerChipsToPot() {
    for (var i in players) {
        if (players[i].betChips != null && players[i].betChips != 0) {
            pot = pot + players[i].betChips;
            players[i].betChips = 0;
            clearBetChips(players[i]);
        }
    }
    drawPotChips();
}
// 每轮最大下注，每轮清0
var roundMaxBet = 0;
// 游戏开始
function onGameStart(e, data) {
    // 清空操作记录列表
    $("#infoDiv").hide();
    $("#trapInfo").css("display", "inline-flex");
    setGameMessage("新一局游戏开始", "success");
    roomInfo.dealerDrawed = false;
    isExitRoom = false;
    roundMaxBet = 0;
    pot = 0;
    clearWinner();
    clearCommonCards();
    clearAllBetChips();
    clearSkillDiv();
    $("#selectPlayerContent").html("");
    $("#selectPlayer").hide();
    $("#selectReverseRoundContent").html("");
    $("#selectReverseRound").hide();
    if (data.state == 1) {
        $.texasMusic.playSendCardMu();
        roomInfo = JSON.parse(data.message);
        appendGameMessage("房间号：" + roomInfo.roomNo, "accent");
        if (roomInfo.handPokers != null) {
            console.log("onGameStart handPokers:", roomInfo.handPokers);
        }
        // 开局重置ingame状态
        for (var i in players) {
            players[i].ingame = false;
        }
        // 游戏中玩家手牌赋值
        for (var i in roomInfo.ingamePlayers) {
            var player = roomInfo.ingamePlayers[i];
            var p = getPlayerBySeatNum(player.seatNum);
            p.ingame = true;
            // 清空手牌
            p.cards = null;
            p.cardDrawed = false;
            p.infoDrawed = false;
            p.bodyChips = player.bodyChips;
        }
        // 发手牌，优先回填到 players 中的本人对象，避免 myInfo 引用漂移导致不渲染
        var currentPlayer = getPlayerBySeatNum(myInfo.seatNum);
        if (currentPlayer == null && myInfo.id != null) {
            for (var j in players) {
                if (players[j].id == myInfo.id) {
                    currentPlayer = players[j];
                    break;
                }
            }
        }
        if (currentPlayer != null) {
            currentPlayer.cards = roomInfo.handPokers;
            currentPlayer.cardDrawed = false;
            currentPlayer.infoDrawed = false;
            myInfo = currentPlayer;
        } else {
            myInfo.cards = roomInfo.handPokers;
        }
        drawRoomInfos();
    }
    /**
     * 房间类型  新增功能
     * 0：普通德扑房
     * 1：系统版卡牌德扑
     * 2：玩家自定义卡组卡牌德扑
     */
    if(roomInfo.type == 1) {
        commonSkillDrawed = false;
        drawPlayerSkillInfos();
    }
}
function drawRoomInfos() {
    // 玩家下注赋值
    for (var i in roomInfo.betRoundMap) {
        var p = getPlayerBySeatNum(i);
        p.betChips = roomInfo.betRoundMap[i];
        p.betDrawed = false;
        if (p.betChips > roundMaxBet) {
            roundMaxBet = p.betChips;
        }
        if(p.betChips > 0) {
            appendGameMessage("玩家：" + p.userName + " 下盲注：" + p.betChips, "info");
        }
    }
    // 根据新的下注信息，重置控制按钮，拖动条
    resetSlipBarAndButtons();
    // 操作倒计时
    timeBar.time = roomInfo.optTimeout / 1000;
    createTimeBar(getPlayerBySeatNum(roomInfo.nextturn));
}
// 有玩家下注
function onPlayerBet(e, data) {
    if (data.state == 1) {
        // 下注声音播放
        $.texasMusic.playbetChipsMu();
        // 重画下注筹码
        var player = JSON.parse(data.message);
        var bet = player.inChips;
        var seatNum = player.seatNum;
        var p = getPlayerBySeatNum(seatNum);
        if (p.betChips == null) {
            p.betChips = 0;
        }
        p.betChips = p.betChips + bet;
        p.betDrawed = false;
        // 重画玩家个人信息筹码数量
        p.bodyChips = p.bodyChips - bet;
        p.infoDrawed = false;
        // 本轮最大下注赋值
        if (p.betChips > roundMaxBet) {
            roundMaxBet = p.betChips;
        }
        cancelTimeBar();
        // 根据新的下注信息，重置控制按钮，拖动条
        resetSlipBarAndButtons();
        appendGameMessage("玩家：" + player.userName + " 下注/跟注：" + p.betChips, "info");
    }
}
// 根据新的下注信息，重置控制按钮，拖动条
function resetSlipBarAndButtons() {
    // 设置slipbar最大下注参数
    DrawSlipBar.betChipsMax = myInfo.bodyChips;
    // 设置slipbar最小下注参数
    DrawSlipBar.betChipsMin = roomInfo.bigBet;
    if (myInfo.betChips == null) {
        myInfo.betChips = 0;
    }
    // 设置最小加注为当前最多下注的2倍
    if (roundMaxBet - myInfo.betChips > 0) {
        DrawSlipBar.betChipsMin = roundMaxBet * 2 - myInfo.betChips;
    }
    // 最小下注大于身上筹码则设为身上筹码
    if (DrawSlipBar.betChipsMin > DrawSlipBar.betChipsMax) {
        DrawSlipBar.betChipsMin = DrawSlipBar.betChipsMax;
    }
    DrawSlipBar.betChips = DrawSlipBar.betChipsMin;
    DrawSlipBar.draw();
    // 重画controllButton
    if (roundMaxBet == myInfo.betChips) {
        checkOrCall = "check";
    } else {
        checkOrCall = "call";
    }
    ControllButtonsDrawed = false;
}
// 有玩家过牌
function onPlayerCheck(e, data) {
    if (data.state == 1) {
        $.texasMusic.playCheckMu();
        var player = JSON.parse(data.message);
        cancelTimeBar();
        ControllButtonsDrawed = false;
        appendGameMessage("玩家：" + player.userName + " 过牌", "info");
    }
}
// 有玩家弃牌
function onPlayerFold(e, data) {
    if (data.state == 1) {
        $.texasMusic.playFoldMu();
        var player = JSON.parse(data.message);
        var seatNum = player.seatNum;
        var p = getPlayerBySeatNum(seatNum);
        p.ingame = false;
        clearCards(p);
        cancelTimeBar();
        ControllButtonsDrawed = false;
        appendGameMessage("玩家：" + player.userName + " 弃牌", "warning");
    }
}
// 游戏结束
function onGameEnd(e, data) {
    if (data.state == 1) {
        // 汇总本轮筹码
        playerChipsToPot();
        var endRoomInfo = JSON.parse(data.message);
        winnerList = endRoomInfo.winPlayersMap;
        cancelTimeBar();
        drawWinners();
        // 玩家手牌列表
        var handCardsMap = endRoomInfo.handCardsMap;
        for (var seatNum in handCardsMap) {
            for (var i in players) {
                var player = players[i];
                if (player.id == myInfo.id) {
                    continue;
                }
                if (player.seatNum == seatNum) {
                    player.cards = handCardsMap[seatNum];
                    player.cardDrawed = false;
                }
            }
        }
        // 玩家成牌列表
        var finalCardsMap = endRoomInfo.finalCardsMap;
        for (var seatNum in finalCardsMap) {
            for (var i in players) {
                var player = players[i];
                if (player.seatNum == seatNum) {
                    player.finalCards = finalCardsMap[seatNum].splice(0, 5);
                    // 牌力大小，同花
                    player.finalCardsLevel = finalCardsMap[seatNum][0];
                }
            }
        }
    }
}
// 其他玩家操作
function onPlayerTurn(e, data) {
    if (data.state == 1) {
        var seatNum = JSON.parse(data.message);
        roomInfo.nextturn = seatNum;
        var player = getPlayerBySeatNum(seatNum);
        resetSlipBarAndButtons();
        createTimeBar(player);
        appendGameMessage("轮到玩家：" + (player == null ? seatNum : player.userName) + " 操作", "accent");

        if (player != null && player.id == myInfo.id) {
            // var nextRoundSkillAction = roomInfo.nextRoundSkillAction;
            if(nextRoundSkillAction == "allin") {
                allIn();
            }
            if(nextRoundSkillAction == "checkOrCall") {
                check();
            }
            if(nextRoundSkillAction == "fold") {
                fold();
            }
            nextRoundSkillAction = "";
            roomInfo.nextRoundSkillAction = "";
        }

    }
}

// 补充筹码操作
function onAssignChips(e, data) {
    if (data.state == 1) {
        maxChips = data.message;
        assignChipsInfo();
        setTimeout('doAssignChipsFail()', 10000);
        isExitRoom = true;
    }
}
function doAssignChipsFail() {
    if(isExitRoom) {
        exitRoom();
    }
}

function doAssignChips() {
    var assignChipsNum = $("#assignChipsNum").val();
    var data = {};
    data.action = mapping.assignChips;
    data.assignChipsNum = assignChipsNum;
    sendWsPayload(data);
    $("#assignChipsInfo").hide();
    isExitRoom = false;
}

// 其他玩家操作
function onPlayerTurnAutoCheck(e, data) {
    if (data.state == 1) {
        setTimeout('check()', 1000);
    }
    appendGameMessage("玩家自动过牌", "warning");
}

// 其他玩家发消息
function onPlayerMessage(e, data) {
    if (data.state == 1) {
        var player = JSON.parse(data.message);
    }
}
// 根据座位号获取players中的玩家
function getPlayerBySeatNum(seatNum) {
    for (var i in players) {
        if (players[i].seatNum == seatNum) {
            return players[i];
        }
    }
}
