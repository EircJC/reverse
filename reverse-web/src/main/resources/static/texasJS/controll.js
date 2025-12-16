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

// 注册，传入账号密码
function regist() {
    var data = {};
    data.action = mapping.regist;
    data.userName = $("#name").val();
    data.userpwd = $("#password").val();
    websocket.send(JSON.stringify(data));
}
// 登陆，传入账号密码
function login() {
    var data = {};
    data.action = mapping.login;
    data.userName = $("#name").val();
    data.userpwd = $("#password").val();
    websocket.send(JSON.stringify(data));
}
// 加入房间，传入房间类型
function enterRoom(type,level) {
    $("#infoDiv").hide();
    var data = {};
    data.action = mapping.enterRoom;
    data.level = $("#type option:selected").val();
    if (level != null && type != null) {
        data.level = level;
        data.type = type;
    }
    websocket.send(JSON.stringify(data));
    gamebackGroundType = "room";
    // 根据房间级别，更换桌面背景
    startIndexBackground = data.type;
    backGroundDrawed = false;
    ControllButtonsDrawed = false;
}
function exitRoom() {
    var data = {};
    data.action = mapping.exitRoom;
    websocket.send(JSON.stringify(data));
    $("#assignChipsInfo").hide();
}
// 站起
function standUp() {
    var data = {};
    data.action = mapping.standUp;
    websocket.send(JSON.stringify(data));
}
// 坐下
function sitDown() {
    var data = {};
    data.action = mapping.sitDown;
    websocket.send(JSON.stringify(data));
    return false;
}
// 加注
function raise() {
    var data = {};
    data.action = mapping.betChips;
    data.inChips = DrawSlipBar.betChips;
    websocket.send(JSON.stringify(data));
}
// 全下
function allIn() {
    var data = {};
    data.action = mapping.betChips;
    data.inChips = myInfo.bodyChips;
    websocket.send(JSON.stringify(data));
}
// 过牌
function check() {
    if (checkOrCall == "call") {
        call();
        return;
    }
    var data = {};
    data.action = mapping.check;
    websocket.send(JSON.stringify(data));
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
    websocket.send(JSON.stringify(data));
}
// 弃牌
function fold() {
    var data = {};
    data.action = mapping.fold;
    websocket.send(JSON.stringify(data));
}
//获取排行榜
function getRankList() {
    var data = {};
    data.action = mapping.getRankList;
    websocket.send(JSON.stringify(data));
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
    websocket.send(JSON.stringify(data));
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
        $("#messages").append("<br>"+skillMessage+";  技能描述: "+skillDescription+";  限制条件: ("+ skillConstrains +")");
        if(skillDefenseCard != null && skillDefenseCard != undefined) {
            $("#messages").append("<br><font color='blue'>(防御)</font> 被 "+destPlayerName+" 的 <font color='blue'>"+skillDefenseCard.skillNameZh+"</font> 抵挡; 技能描述: "+skillDefenseCard.description+";  限制条件: ("+ skillDefenseCard.constrains +")");
        }
    } else {
        $("#messages").append("<br>"+data.message);
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
                $("#messages").append("<br><font color='green'>(陷阱)</font> 玩家 <font color='#ff7f50'>"+srcPlayerName+"</font> 触发 "+skillTrapCard.usedPlayer.userName+" 的 <font color='green'>"+skillTrapCard.skillNameZh+"</font>  技能描述: "+skillTrapCard.description+";  限制条件: ("+ skillTrapCard.constrains +")");
            }
        }
    } else {
        $("#messages").append("<br>"+data.message);
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
            $("#messages").append("<br><font color='blue'>(防御)</font> "+srcPlayerName+" 的 <font color='blue'>"+skillDefenseCard.skillNameZh+"</font> 抵挡; (技能描述: "+skillDefenseCard.description+";  限制条件: "+ skillDefenseCard.constrains +")");
            $("#messages").append("<br>    躲过玩家 "+skillTrapCard.usedPlayer.userName+" 的陷阱 <font color='green'>"+skillTrapCard.skillNameZh+"</font> ; (技能描述: "+skillTrapCard.description+";  限制条件: "+ skillTrapCard.constrains +")");
        }
    } else {
        $("#messages").append("<br>"+data.message);
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
        $("#registDiv").hide();
    } else {
        document.getElementById('messages').innerHTML = 'login fail';
    }
}
// 注册结果
function onRegister(e, data) {
    if (data.state == 1) {
        // 注册成功后自动登陆
        login();
        $("#name").val("");
        $("#password").val("");
        document.getElementById('messages').innerHTML = 'register success';
    } else {
        document.getElementById('messages').innerHTML = 'register fail';
    }
}
function onEnterRoom(e, data) {
    if (data.state == 1) {
        $.texasMusic.stopBackMu();
        gamebackGroundType = "room";
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
        document.getElementById('messages').innerHTML = '进入房间失败';
    }
}
// 本人退出房间
function onOutRoom() {
    gamebackGroundType = "lobby";
    lobbyBackGroundLoaded = false;
    LobbyButtonsDrawed = false;
    removeAllPlayers();
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
    $("#trapInfo").show();
    $("#messages").html("新一局游戏开始 ");
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
        $("#messages").append("<br>房间号："+roomInfo.roomNo);
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
        // 发手牌
        myInfo.cards = roomInfo.handPokers;
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
            $("#messages").append("<br>玩家："+p.userName+" 下盲注："+p.betChips);
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
        $("#messages").append("<br>玩家："+player.userName+" 下注/跟注 ："+p.betChips);
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
        $("#messages").append("<br>玩家："+player.userName+" 过牌");
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
        $("#messages").append("<br>玩家："+player.userName+" 弃牌");
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
        var player = getPlayerBySeatNum(seatNum);
        createTimeBar(player);
        $("#messages").append("<br>---------------------------------------------------------");
        $("#messages").append("<br>轮到玩家："+player.userName+" 操作");

        if (player.id == myInfo.id) {
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
    websocket.send(JSON.stringify(data));
    $("#assignChipsInfo").hide();
    isExitRoom = false;
}

// 其他玩家操作
function onPlayerTurnAutoCheck(e, data) {
    if (data.state == 1) {
        setTimeout('check()', 1000);
    }
    $("#messages").append("<br>玩家："+p.userName+" 自动过牌");
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