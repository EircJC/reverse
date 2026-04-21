/**
 * Created by xll on 2016/10/30.
 */
var texasCanvas = {};
var texasContext = {};
// 屏幕缩放倍数
var screenScale = 1.5;
// 背景图高
var oneHeight = 353;
// 背景图宽
var oneWidth = 666.6;
// 屏幕的实际宽和高
var screenHeight = oneHeight * screenScale;
var screenWidth = oneWidth * screenScale;
// 筹码
var chipsImage = new Image();
chipsImage.src = "texasImages/chips_alpha.png";
var cardScale = 0.55 * screenScale;
// 卡牌高度
var cardHeight = 111;
// 卡牌宽度
var cardWidth = 81;
// 卡牌背高度
var cardBackHeight = 165;
// 卡牌背宽度
var cardBackWidth = 121;
var checkPoint = {};
// checkPoint.x = oneWidth * 0.68 * screenScale;
// checkPoint.y = oneHeight * 0.82 * screenScale;
checkPoint.x = screenWidth * 0.68;
checkPoint.y = screenHeight * 0.72;
// 玩家头像等信息区域
var playerRecWidth = 50 * screenScale;
var playerRecHeight = 50 * screenScale;
var roomConfig = {
    maxPlayer: 6
}
// 进度条
var timeBar = {
    time: 600,// 10秒走完
    x: 0, y: 0, width: 100, height: 8, maxwidth: playerRecWidth * 2, color: "blue", drawing: true
}
// 画牌桌背景
var backGroundImg = new Image();
backGroundImg.src = "texasImages/pokertable2.png";
// 背景图序号,0到5 共6个桌面
var startIndexBackground = 0;
// 背景图已经绘制
var backGroundLoaded = false;
var backGroundDrawed = false;
var players = [];
// 游戏所处背景，首页login,房间room,大厅lobby
var gamebackGroundType = "login";
var loginBackGroundLoaded = false;
var lobbyBackGroundLoaded = false;

// 卡牌开关
var commonSkillDrawed = false;
function drawBackground() {
    if (gamebackGroundType == "login" && !loginBackGroundLoaded) {
        loginBackGroundLoaded = true;
        texasContext.clearRect(0, 0, screenWidth, screenHeight);

        var gradient = texasContext.createLinearGradient(0, 0, screenWidth, screenHeight);
        gradient.addColorStop(0, "#10192d");
        gradient.addColorStop(0.55, "#15263f");
        gradient.addColorStop(1, "#0f3b31");
        texasContext.fillStyle = gradient;
        texasContext.fillRect(0, 0, screenWidth, screenHeight);

        texasContext.globalAlpha = 0.16;
        texasContext.fillStyle = "#ffd36f";
        texasContext.beginPath();
        texasContext.arc(screenWidth * 0.18, screenHeight * 0.18, 110, 0, Math.PI * 2);
        texasContext.fill();

        texasContext.globalAlpha = 0.12;
        texasContext.fillStyle = "#7f95ff";
        texasContext.beginPath();
        texasContext.arc(screenWidth * 0.82, screenHeight * 0.28, 160, 0, Math.PI * 2);
        texasContext.fill();

        texasContext.globalAlpha = 0.08;
        texasContext.fillStyle = "#ffffff";
        texasContext.fillRect(screenWidth * 0.08, screenHeight * 0.12, screenWidth * 0.84, screenHeight * 0.76);

        texasContext.globalAlpha = 1;
        texasContext.strokeStyle = "rgba(255,255,255,0.08)";
        texasContext.lineWidth = 1;
        for (var i = 0; i < 6; i++) {
            texasContext.beginPath();
            texasContext.moveTo(screenWidth * 0.08, screenHeight * (0.18 + i * 0.1));
            texasContext.lineTo(screenWidth * 0.92, screenHeight * (0.18 + i * 0.1));
            texasContext.stroke();
        }

        drawLoginArea();
    } else if (gamebackGroundType == "lobby" && !lobbyBackGroundLoaded) {
        if (backGroundImg.complete) {
            lobbyBackGroundLoaded = true;
            backGroundLoaded = true;
            // 先定义onload再定义src
            var x = 0, y = 0;
            texasContext.clearRect(0, 0, screenWidth, screenHeight);
            texasContext.drawImage(backGroundImg, 0, oneHeight * startIndexBackground, oneWidth,
                oneHeight, x, y, screenWidth, screenHeight);
            // 信息背景灰色
            texasContext.globalAlpha = 0.5;
            texasContext.fillStyle = "#604D4F";
            texasContext.fillRect(0, 0, screenWidth, screenHeight);
            drawLobbyArea();
            getRankList();
        }
    }
    else if (gamebackGroundType == "room" && !backGroundDrawed) {
        backGroundDrawed = true;
        var x = 0, y = 0;
        var startX = 0;
        texasContext.globalAlpha = 1;
        texasContext.clearRect(0, 0, screenWidth, screenHeight);
        texasContext.drawImage(backGroundImg, startX, oneHeight * startIndexBackground, oneWidth,
            oneHeight, x, y, screenWidth, screenHeight);
        drawRoomButtons();

    }
}
// 玩家登陆信息填写部分
function drawLoginArea() {
    syncScenePanels();
    drawLoginButtons();
}
// 大厅，选房间界面
function drawLobbyArea() {
    syncScenePanels();
    drawLobbyButtons();
}
// 在指定位置创建timebar
function createTimeBar(player) {
    if (player == null || player.position == null) {
        return;
    }
    cancelTimeBar();
    timeBar.width = timeBar.maxwidth;
    var postion = player.position;
    timeBar.x = postion.x;
    timeBar.y = postion.y - timeBar.height;
    timeBar.color = "blue";
    timeBar.drawing = true;
}
// 画进度条
function drawTimeBar() {
    if (timeBar.drawing) {
        /* 设置填充颜色 */
        texasContext.fillStyle = timeBar.color;
        /* 边框的宽度 */
        texasContext.globalAlpha = 1;
        var sWidth = timeBar.width;
        var sHeight = timeBar.height;
        // 用背景图擦除原来的位置,修复边框像素
        var fix = 2 * screenScale;
        clearRectByBackGround(timeBar.x - fix, timeBar.y - fix, timeBar.maxwidth + fix,
            timeBar.height + fix);
        if (timeBar.x > 0 && sWidth > 0) {
            texasContext.fillRect(timeBar.x, timeBar.y, sWidth, sHeight);
        }
    }
}
// 更新进度条的位置
function updateTimeBar(modifier) {
    if (timeBar.width >= 0) {
        timeBar.width = timeBar.width - modifier * ( timeBar.maxwidth / timeBar.time);
        if (timeBar.width / timeBar.maxwidth < 0.6) {
            timeBar.color = "yellow";
        }
        if (timeBar.width / timeBar.maxwidth < 0.3) {
            timeBar.color = "red";
        }
    }
}
// 更新进度条的位置
function cancelTimeBar() {
    if (timeBar.width != 0) {
        timeBar.width = 0;
        //停止绘画
        timeBar.drawing = false;
        // 用背景图擦除原来的位置,修复边框像素
        var fix = 2 * screenScale;
        clearRectByBackGround(timeBar.x - fix, timeBar.y - fix, timeBar.maxwidth + fix,
            timeBar.height + fix);
    }
}
function createPlayer(player) {
    if (player.betChips == null) {
        player.betChips = 0;
    }
    for (var i in players) {
        var currentPlayer = players[i];
        if ((player.id != null && currentPlayer.id == player.id)
            || (player.seatNum != null && currentPlayer.seatNum == player.seatNum)) {
            player.index = currentPlayer.index;
            player.position = currentPlayer.position;
            player.betPosition = currentPlayer.betPosition;
            player.cardDrawed = false;
            player.infoDrawed = false;
            players[i] = player;
            return;
        }
    }
    players.push(player);
}
function removeAllPlayers() {
    while (players.length != 0) {
        removePlayer(players[0]);
    }
}
function removePlayer(player) {
    for (var i in players) {
        if (players[i].id == player.id) {
            var arrDeleted = players.splice(i, 1);
            player = arrDeleted[0];
            break;
        }
    }
    if (player.position != null) {
        // 用背景图擦除原来的位置
        clearRectByBackGround(player.position.x, player.position.y, playerRecWidth * 2, playerRecHeight);
    }

    var position = player.betPosition;
    if (position != null) {
        // 用背景图擦除原来的位置
        clearBetChips(player);
        player.betPosition = null;
    }
    clearCards(player);
    if (player.seatNum == roomInfo.nextturn) {
        cancelTimeBar();
    }
}
// 画所有玩家
function drawPlayersPhoto(width, height) {
    // 先画自己
    for (var i in players) {
        var p = {};
        var player = players[i];
        if (player.position == null) {
            if (player.index == null) {
                player.index = (player.seatNum - myInfo.seatNum + roomConfig.maxPlayer) % roomConfig.maxPlayer;
            }
            player.position = getPlayerPosition(width, height, player.index);
            drawPlayer(player.position.x, player.position.y, player);
        }
    }
}
// 获取每个人下注显示的位置
var chipsPositionArr = [];
function getChipsPosition(screenWidth, screenHeight, index) {
    if (chipsPositionArr[index] != null) {
        return chipsPositionArr[index];
    }
    var position = {};
    position.x = -500;
    position.y = -500;
    if (index == 0) {
        position.x = screenWidth / 2 - 10;
        position.y = screenHeight * 0.67;
    }
    if (index == 5) {
        position.x = screenWidth * 0.64;
        position.y = screenHeight * 0.64;
    }
    if (index == 4) {
        position.x = screenWidth * 0.64;
        position.y = screenHeight * 0.32;
    }
    if (index == 3) {
        position.x = screenWidth / 2 - 10;
        position.y = screenHeight * 0.3;
    }
    if (index == 2) {
        position.x = screenWidth * 0.32;
        position.y = screenHeight * 0.32;
    }
    if (index == 1) {
        position.x = screenWidth * 0.32;
        position.y = screenHeight * 0.64;
    }
    chipsPositionArr[index] = position;
    return position;
}
// 6个座位从自己开始逆时针p从0到5,根据屏幕大小自适应取位置
var playerPositionArr = [];
function getPlayerPosition(screenWidth, screenHeight, index) {
    if (playerPositionArr[index] != null) {
        return playerPositionArr[index];
    }
    var position = {};
    position.x = -500;
    position.y = -500;
    if (index == 0) {
        position.x = screenWidth / 2 - 10;
        position.y = screenHeight * 0.735;
    }
    if (index == 5) {
        position.x = screenWidth * 0.8;
        position.y = screenHeight * 0.56;
    }
    if (index == 4) {
        position.x = screenWidth * 0.8;
        position.y = screenHeight * 0.265;
    }
    if (index == 3) {
        position.x = screenWidth / 2 - 10;
        position.y = screenHeight * 0.09;
    }
    if (index == 2) {
        position.x = screenWidth * 0.15;
        position.y = screenHeight * 0.265;
    }
    if (index == 1) {
        position.x = screenWidth * 0.15;
        position.y = screenHeight * 0.56;
    }
    playerPositionArr[index] = position;
    return position;
}
// 用背景图擦除原来的位置
function clearRectByBackGround(x, y, width, height) {
    texasContext.drawImage(backGroundImg, x / screenScale, y / screenScale + oneHeight * startIndexBackground, width / screenScale,
        height / screenScale, x, y, width, height);
}
// 绘制玩家头像框
function drawPlayer(x, y, player) {
    // texasContext.globalAlpha = 1;
    if (player.img == null) {
        player.img = new Image();
        // 用背景图擦除原来的位置
        clearRectByBackGround(x, y, playerRecWidth, playerRecHeight);
        /* 透明度 */
        texasContext.globalAlpha = 0.7;
        /* 透明度 */
        texasContext.globalAlpha = 1;
        player.img.onload = function () {
            clearRectByBackGround(x, y, playerRecWidth, playerRecHeight);
            texasContext.drawImage(player.img, x, y, playerRecWidth, playerRecHeight);
        }
        player.img.onerror = function () {
            var fallbackAvatar = player.avatarFallbackTried ? "texasImages/player.png" : getLocalPlayerAvatarUrl(player);
            if (player.avatarFallbackTried && player.img.src.indexOf("texasImages/player.png") >= 0) {
                player.img.onerror = null;
                return;
            }
            player.avatarFallbackTried = true;
            player.picLogo = fallbackAvatar;
            player.img.src = fallbackAvatar;
        }
        player.img.src = getPlayerAvatarUrl(player);
    }

}

// 画已经下注的筹码和数额
function drawBetChips() {
    for (var i in players) {
        var player = players[i];
        if (player.betPosition == null) {
            player.betPosition = getChipsPosition(screenWidth, screenHeight, player.index);
        }
        var p = player.betPosition;
        var x = p.x;
        var y = p.y;
        texasContext.font = screenScale * 8 + "px" + " 楷体";
        texasContext.fillStyle = "#fff";
        // 根据dealer位置画D图标
        if (player.seatNum == roomInfo.dealer && !roomInfo.dealerDrawed) {
            roomInfo.dealerDrawed = true;
            texasContext.fillText("D ", x - 10 * screenScale, y);
        } else if (player.seatNum != roomInfo.dealer) {
            // 用背景图擦除原来的位置
            clearRectByBackGround(x - 10 * screenScale, y - 10 * screenScale, 10 * screenScale,
                10 * screenScale);
        }
        // 判断是否已经画过筹码
        if (player.betDrawed) {
            continue;
        }
        player.betDrawed = true;
        if (player.betChips != null && player.betChips != 0) {
            clearBetChips(player);
            // 写筹码数额
            texasContext.fillText(player.betChips, x + 15 * screenScale, y + screenScale * 10);
            texasContext.drawImage(chipsImage, 0, 0, 160,
                160, x, y, 15 * screenScale, 15 * screenScale);
        }
    }
}
// 清除所有玩家下注
function clearAllBetChips() {
    for (var i = 0; i < 6; i++) {
        var position = getChipsPosition(screenWidth, screenHeight, i);
        if (position != null) {
            // 用背景图擦除原来的位置
            clearRectByBackGround(position.x, position.y, 40 * screenScale, 15 * screenScale);
        }
    }
}
// 清除玩家下注
function clearBetChips(player) {
    var position = getChipsPosition(screenWidth, screenHeight, player.index);
    if (position != null) {
        // 用背景图擦除原来的位置
        clearRectByBackGround(position.x, position.y, 40 * screenScale, 15 * screenScale);
        player.betPosition = null;
    }

}
// 画底池
var potText = "0";
var pot = 0;
function drawPotChips() {
    if (pot == null) {
        pot = potText;
    }
    var x = ( screenWidth / 2 - 10);
    var y = screenHeight * 0.4;
    // 先清除该区域，防止重影
    clearRectByBackGround(x, y - 20, 80, 20)
    texasContext.font = screenScale * 10 + "px" + " 楷体";
    texasContext.fillStyle = "#F0E68C";
    texasContext.fillText("POT:" + pot, x, y);
}
// 清除底池
function clearPotChips() {
    potText = null;
    // 清除文字时Y轴变换上移
    clearRectByBackGround(( screenWidth / 2 - 10), screenHeight * 0.4 - 10 * screenScale, 50 * screenScale, 10 * screenScale);
}
// 画玩家信息
function drawPlayerInfos() {
    for (var i in players) {
        var player = players[i];
        if (player.infoDrawed) {
            continue;
        }
        player.infoDrawed = true;
        drawPlayerInfo(player);
    }
}
// 绘制玩家姓名筹码信息
function drawPlayerInfo(player) {
    var point = player.position;
    var x = point.x;
    var y = point.y;
    // 用背景图擦除原来的位置
    clearRectByBackGround(x + playerRecWidth, y, playerRecWidth, playerRecHeight);
    // 玩家信息背景灰色
    /* 设置填充颜色 */
    texasContext.fillStyle = "#604D4F";
    texasContext.fillRect(x + playerRecWidth, y, playerRecWidth, playerRecHeight);
    // 玩家名 设置字体，颜色
    texasContext.font = screenScale * 10 + "px" + " 楷体";
    texasContext.fillStyle = "#fff";
    texasContext.fillText(player.userName, x + playerRecWidth + 10, y + screenScale * 10);
    // 玩家剩余筹码 设置字体，颜色
    texasContext.font = screenScale * 10 + "px" + " 楷体";
    texasContext.fillStyle = "#fff";
    texasContext.fillText("$" + player.bodyChips, x + playerRecWidth + 5, y + screenScale * 40);
}
var checkOrCall = "check";
var ControllButtonsDrawed = false;
var canvasHotspots = [];
var hoveredHotspotKey = null;
var pressedHotspotKey = null;
var showCanvasHotspotDebug = false;

function resetCanvasHotspots() {
    canvasHotspots = [];
    $(texasCanvas).off("click.texasControls");
    $(texasCanvas).off("mousemove.texasControls");
    $(texasCanvas).off("mousedown.texasControls");
    $(texasCanvas).off("mouseup.texasControls");
    $(texasCanvas).off("mouseleave.texasControls");
    $(texasCanvas).css("cursor", "default");
    hoveredHotspotKey = null;
    pressedHotspotKey = null;
}

function findCanvasHotspot(cx, cy) {
    for (var i = canvasHotspots.length - 1; i >= 0; i--) {
        var hotspot = canvasHotspots[i];
        if (cx >= hotspot.x && cx <= hotspot.x + hotspot.width
            && cy >= hotspot.y && cy <= hotspot.y + hotspot.height) {
            return hotspot;
        }
    }
    return null;
}

function setHoveredHotspot(key) {
    if (hoveredHotspotKey !== key) {
        hoveredHotspotKey = key;
        ControllButtonsDrawed = false;
    }
}

function setPressedHotspot(key) {
    if (pressedHotspotKey !== key) {
        pressedHotspotKey = key;
        ControllButtonsDrawed = false;
    }
}

function getCanvasPointerPosition(evt) {
    if (texasCanvas == null || texasCanvas.getBoundingClientRect == null) {
        return {x: 0, y: 0};
    }
    var rect = texasCanvas.getBoundingClientRect();
    var rectWidth = rect.width || texasCanvas.width || 1;
    var rectHeight = rect.height || texasCanvas.height || 1;
    return {
        x: (evt.clientX - rect.left) * (texasCanvas.width / rectWidth),
        y: (evt.clientY - rect.top) * (texasCanvas.height / rectHeight)
    };
}

function bindCanvasHotspots() {
    $(texasCanvas).off("click.texasControls");
    $(texasCanvas).off("mousemove.texasControls");
    $(texasCanvas).off("mousedown.texasControls");
    $(texasCanvas).off("mouseup.texasControls");
    $(texasCanvas).off("mouseleave.texasControls");

    $(texasCanvas).on("click.texasControls", function (e) {
        var point = getCanvasPointerPosition(e);
        var cx = point.x;
        var cy = point.y;
        var hotspot = findCanvasHotspot(cx, cy);
        setPressedHotspot(null);
        if (hotspot != null && !hotspot.disabled) {
            var func = eval(hotspot.clickFunc);
            new func(hotspot.type, hotspot.level);
        }
    });

    $(texasCanvas).on("mousemove.texasControls", function (e) {
        var point = getCanvasPointerPosition(e);
        var cx = point.x;
        var cy = point.y;
        var hotspot = findCanvasHotspot(cx, cy);
        if (hotspot != null) {
            setHoveredHotspot(hotspot.key);
            $(texasCanvas).css("cursor", hotspot.disabled ? "not-allowed" : "pointer");
            return;
        }
        setHoveredHotspot(null);
        $(texasCanvas).css("cursor", "default");
    });

    $(texasCanvas).on("mousedown.texasControls", function (e) {
        var point = getCanvasPointerPosition(e);
        var cx = point.x;
        var cy = point.y;
        var hotspot = findCanvasHotspot(cx, cy);
        if (hotspot != null && !hotspot.disabled) {
            setPressedHotspot(hotspot.key);
        }
    });

    $(texasCanvas).on("mouseup.texasControls", function () {
        setPressedHotspot(null);
    });

    $(texasCanvas).on("mouseleave.texasControls", function () {
        setHoveredHotspot(null);
        setPressedHotspot(null);
        $(texasCanvas).css("cursor", "default");
    });
}

function isMyTurn() {
    return roomInfo != null && myInfo != null
        && roomInfo.nextturn != null && myInfo.seatNum != null
        && roomInfo.nextturn == myInfo.seatNum;
}

function isActionButtonDisabled(actionName) {
    if (actionName == "exitRoom") {
        return false;
    }
    if (myInfo == null || myInfo.bodyChips == null) {
        return true;
    }
    if (!isMyTurn()) {
        return true;
    }
    if ((actionName == "raise" || actionName == "call") && myInfo.bodyChips <= 0) {
        return true;
    }
    return false;
}

function drawRoundedRectPath(x, y, width, height, radius) {
    var rectRadius = Math.min(radius, width / 2, height / 2);
    texasContext.beginPath();
    texasContext.moveTo(x + rectRadius, y);
    texasContext.lineTo(x + width - rectRadius, y);
    texasContext.quadraticCurveTo(x + width, y, x + width, y + rectRadius);
    texasContext.lineTo(x + width, y + height - rectRadius);
    texasContext.quadraticCurveTo(x + width, y + height, x + width - rectRadius, y + height);
    texasContext.lineTo(x + rectRadius, y + height);
    texasContext.quadraticCurveTo(x, y + height, x, y + height - rectRadius);
    texasContext.lineTo(x, y + rectRadius);
    texasContext.quadraticCurveTo(x, y, x + rectRadius, y);
    texasContext.closePath();
}

function drawCanvasHotspotMarker(x, y, width, height, label, disabled) {
    if (!showCanvasHotspotDebug) {
        return;
    }
    var markerColor = disabled ? "rgba(255, 155, 155, 0.88)" : "rgba(80, 226, 255, 0.92)";
    var fillColor = disabled ? "rgba(255, 107, 107, 0.08)" : "rgba(80, 226, 255, 0.08)";
    var tagWidth = Math.max(70, label.length * 9 + 18);
    var tagHeight = 18;
    var tagX = x + (width - tagWidth) / 2;
    var tagY = y - tagHeight - 8;

    texasContext.save();
    texasContext.globalAlpha = 1;
    texasContext.fillStyle = fillColor;
    drawRoundedRectPath(x, y, width, height, 12 * screenScale);
    texasContext.fill();

    texasContext.setLineDash([6, 5]);
    texasContext.lineWidth = 2;
    texasContext.strokeStyle = markerColor;
    drawRoundedRectPath(x, y, width, height, 12 * screenScale);
    texasContext.stroke();
    texasContext.setLineDash([]);

    texasContext.fillStyle = markerColor;
    texasContext.fillRect(x - 1, y - 1, 8, 8);
    texasContext.fillRect(x + width - 7, y - 1, 8, 8);
    texasContext.fillRect(x - 1, y + height - 7, 8, 8);
    texasContext.fillRect(x + width - 7, y + height - 7, 8, 8);

    if (tagY > 0) {
        texasContext.fillStyle = "rgba(9, 18, 34, 0.92)";
        drawRoundedRectPath(tagX, tagY, tagWidth, tagHeight, 9);
        texasContext.fill();
        texasContext.lineWidth = 1;
        texasContext.strokeStyle = markerColor;
        texasContext.stroke();

        texasContext.font = "600 " + (screenScale * 8) + "px PingFang SC";
        texasContext.fillStyle = markerColor;
        texasContext.textAlign = "center";
        texasContext.textBaseline = "middle";
        texasContext.fillText(label, tagX + tagWidth / 2, tagY + tagHeight / 2);
        texasContext.textAlign = "start";
        texasContext.textBaseline = "alphabetic";
    }
    texasContext.restore();
}

// 绘制所有房间内控制按钮
function drawControllButtons() {
    var registerHotspots = !ControllButtonsDrawed;
    if (registerHotspots) {
        resetCanvasHotspots();
        ControllButtonsDrawed = true;
    }

    drawRoomButtons(registerHotspots);
    var bwidth = screenWidth * 0.1;
    var bheight = screenHeight * 0.07;
    drawControllButton(checkPoint.x, checkPoint.y, bwidth, bheight, checkOrCall, checkOrCall, null, null, {
        key: "action-" + checkOrCall,
        disabled: isActionButtonDisabled(checkOrCall),
        hotspotLabel: checkOrCall == "call" ? "点击区 · 跟注" : "点击区 · 过牌",
        registerHotspot: registerHotspots
    });
    drawControllButton(screenWidth * 0.82, screenHeight * 0.72, bwidth, bheight, "fold", "fold", null, null, {
        key: "action-fold",
        disabled: isActionButtonDisabled("fold"),
        hotspotLabel: "点击区 · 弃牌",
        registerHotspot: registerHotspots
    });
    drawControllButton(screenWidth * 0.75, screenHeight * 0.86, bwidth, bheight, "raise", "raise", null, null, {
        key: "action-raise",
        disabled: isActionButtonDisabled("raise"),
        hotspotLabel: "点击区 · 加注",
        registerHotspot: registerHotspots
    });
    if (registerHotspots) {
        bindCanvasHotspots();
    }
}
var loginButtonsDrawed = false;
// 绘制登陆控制按钮
function drawLoginButtons() {
    if (!loginButtonsDrawed) {
        loginButtonsDrawed = true;
        resetCanvasHotspots();
    }
}
var LobbyButtonsDrawed = false;
// 选房间按钮
function drawLobbyButtons() {
    if (!LobbyButtonsDrawed) {
        LobbyButtonsDrawed = true;
        resetCanvasHotspots();
    }
}
// 房间控制
function drawRoomButtons(registerHotspots) {
    var registerHotspot = registerHotspots !== false;
    var bwidth = screenWidth * 0.13;
    var bheight = screenHeight * 0.07;
    drawControllButton(screenWidth * 0.03, screenHeight * 0.02, bwidth, bheight, "Exit Room", "exitRoom", 0, 0, {
        key: "action-exitRoom",
        variant: "danger",
        disabled: false,
        hotspotLabel: "点击区 · 退出房间",
        registerHotspot: registerHotspot
    });
}
// 绘制一个控制按钮
function drawControllButton(x, y, bwidth, bheight, txt, clickFunc, type, level, options) {
    var buttonOptions = options || {};
    var key = buttonOptions.key || (clickFunc + "-" + txt + "-" + type + "-" + level);
    var disabled = buttonOptions.disabled === true;
    var hovered = hoveredHotspotKey === key;
    var pressed = pressedHotspotKey === key;
    var variant = buttonOptions.variant || "primary";
    var hotspotLabel = buttonOptions.hotspotLabel || ("点击区 · " + txt);

    var topColor = "#7d5cff";
    var bottomColor = "#3f47ff";
    if (variant === "danger") {
        topColor = "#ff7878";
        bottomColor = "#ff4d6d";
    }
    if (variant === "neutral") {
        topColor = "#4f68ff";
        bottomColor = "#3850dc";
    }
    if (disabled) {
        topColor = "rgba(118, 130, 160, 0.65)";
        bottomColor = "rgba(76, 86, 112, 0.65)";
    } else if (pressed) {
        topColor = "rgba(255, 212, 111, 0.92)";
        bottomColor = "rgba(255, 170, 64, 0.92)";
    } else if (hovered) {
        topColor = "rgba(146, 167, 255, 0.98)";
        bottomColor = "rgba(86, 108, 255, 0.98)";
    }

    var gradient = texasContext.createLinearGradient(x, y, x, y + bheight);
    gradient.addColorStop(0, topColor);
    gradient.addColorStop(1, bottomColor);
    // 用渐变填色
    texasContext.fillStyle = gradient;
    texasContext.globalAlpha = 1;
    drawRoundedRectPath(x, y, bwidth, bheight, 14 * screenScale);
    texasContext.fill();

    texasContext.lineWidth = 1.5;
    texasContext.strokeStyle = disabled ? "rgba(255,255,255,0.10)" : "rgba(255,255,255,0.26)";
    texasContext.stroke();

    if (!disabled) {
        texasContext.fillStyle = hovered || pressed ? "rgba(255,255,255,0.14)" : "rgba(255,255,255,0.08)";
        drawRoundedRectPath(x + 2, y + 2, bwidth - 4, bheight * 0.42, 12 * screenScale);
        texasContext.fill();
    }

    texasContext.font = "600 " + (screenScale * 12) + "px PingFang SC";
    texasContext.fillStyle = disabled ? "rgba(240,244,255,0.72)" : "#fff";
    texasContext.textAlign = "center";
    texasContext.textBaseline = "middle";
    texasContext.fillText(txt, x + bwidth / 2, y + bheight / 2 + (pressed ? 1 : 0));
    texasContext.textAlign = "start";
    texasContext.textBaseline = "alphabetic";

    if (buttonOptions.registerHotspot !== false) {
        canvasHotspots.push({
            key: key,
            x: x,
            y: y,
            width: bwidth,
            height: bheight,
            clickFunc: clickFunc,
            type: type,
            level: level,
            disabled: disabled
        });
    }

    drawCanvasHotspotMarker(x, y, bwidth, bheight, hotspotLabel, disabled);
}
// 绘制所有玩家的扑克牌
function drawCards() {
    for (var i in players) {
        var player = players[i];

        if (player.cardDrawed) {
            continue;
        }
        player.cardDrawed = true;
        if (player.cards != null) {
            drawHandCards(player);
        } else if (player.ingame != null && player.ingame) {
            drawCardBack(player);
        } else {
            clearCards(player);
        }
    }
}
// 绘制手牌
function drawHandCards(player) {
    var p = player.position;
    if (player == null || p == null || player.cards == null) {
        return;
    }
    var cards = player.cards;
    var x = p.x;
    var y = p.y;
    x = x - cardWidth * screenScale * 0.8;
    texasContext.globalAlpha = 1;
    drawPoker(texasContext, x, y, cardHeight * cardScale, cards[0]);
    drawPoker(texasContext, x + cardWidth * 0.45 * cardScale, y, cardHeight * cardScale, cards[1]);
}
// 画卡背
function drawCardBack(player) {
    var p = player.position;
    if (player == null || p == null) {
        return;
    }
    var x = p.x;
    var y = p.y;
    x = x - cardWidth * screenScale * 0.8;
    texasContext.globalAlpha = 1;
    drawPokerBack(texasContext, x, y, cardHeight * cardScale);
    drawPokerBack(texasContext, x + cardWidth * 0.45 * cardScale, y, cardHeight * cardScale);
}
// 清除手牌
function clearCards(player) {
    var position = player.position;
    if (position == null) {
        return;
    }
    if (player.cards != null) {
        // 用背景图擦除原来的位置
        clearRectByBackGround(position.x - cardWidth * screenScale * 0.8 - 2, position.y - 2, 1.45 * cardWidth * cardScale + 4,
            cardHeight * cardScale + 4);
    } else {
        // 用背景图擦除原来的位置
        clearRectByBackGround(position.x - cardWidth * screenScale * 0.8 - 2, position.y - 2, 1.45 * cardWidth * cardScale + 4,
            cardHeight * cardScale + 4);
    }
}
var commonCards = ['Ts', 'Js', 'Qs', 'Ks', 'As'];
var commonCardsDrawed = false;
// 绘制公共牌
function drawCommonCards() {
    if (commonCardsDrawed) {
        return;
    }
    commonCardsDrawed = true;
    var cards = commonCards;
    if (cards == null) {
        return;
    }
    texasContext.globalAlpha = 1;
    for (var i in cards) {
        drawPoker(texasContext, screenWidth / 3 + i * cardWidth * cardScale, screenHeight / 2.4, cardHeight * cardScale, cards[i]);
    }
}
// 清除公共牌
function clearCommonCards() {
    commonCards = [];
    clearRectByBackGround(screenWidth / 3, screenHeight / 2.4 - 2, 5 * cardWidth * cardScale + 2, cardHeight * cardScale + 2);
}
// 设置玩家头像前后缀
function setPlayerPicUrl(player) {
    player.picLogo = getLocalPlayerAvatarUrl(player);
}

function getLocalPlayerAvatarUrl(player) {
    if (player == null) {
        return "texasImages/player.png";
    }
    var avatarKey = "";
    if (player.id != null && String(player.id).trim() !== "") {
        avatarKey = String(player.id);
    } else if (player.userName != null && String(player.userName).trim() !== "") {
        avatarKey = String(player.userName);
    } else if (player.seatNum != null && !isNaN(player.seatNum)) {
        avatarKey = String(player.seatNum);
    }
    if (avatarKey === "") {
        return "texasImages/player.png";
    }
    var hash = 0;
    for (var i = 0; i < avatarKey.length; i++) {
        hash = ((hash << 5) - hash) + avatarKey.charCodeAt(i);
        hash = hash & hash;
    }
    var avatarIndex = Math.abs(hash) % 5;
    return "texasImages/player" + avatarIndex + ".png";
}

function getPlayerAvatarUrl(player) {
    if (player == null) {
        return "texasImages/player.png";
    }
    if (player.picLogo == null || player.picLogo == undefined || String(player.picLogo).trim() === "") {
        setPlayerPicUrl(player);
    }
    return player.picLogo;
}

function getPlayerAvatarErrorHandler() {
    return "this.onerror=null;this.src='texasImages/player.png';";
}
var winnerList = {4: 500, 0: 100, 1: 200, 2: 300, 3: 400, 5: 600};
// 提示获胜玩家
function drawWinners() {
    for (var i in winnerList) {
        drawWinner(i, winnerList[i]);
    }
}
//胜利玩家赢得筹码数量提示
function drawWinner(seatNum, chips) {
    var player = getPlayerBySeatNum(seatNum);
    if (player == null || player.position == null) {
        return;
    }
    var point = player.position;
    // 获胜玩家 设置字体，颜色
    texasContext.font = screenScale * 15 + "px" + " 楷体";
    texasContext.fillStyle = "#F0E68C";
    texasContext.fillText("Win " + chips, point.x + playerRecWidth + 5, point.y - screenScale * 5);
}
//清除胜利玩家赢得筹码数量提示
function clearWinner() {
    for (var i in players) {
        var player = players[i];
        if (player == null || player.position == null) {
            continue;
        }
        var point = player.position;
        clearRectByBackGround(point.x + playerRecWidth, point.y - screenScale * 40, playerRecWidth + screenScale * 40, screenScale * 40);
    }
}

//玩家使用技能提示
function drawSkillMessage(message) {
    clearSkillMessage();
    // 获胜玩家 设置字体，颜色
    texasContext.font = screenScale * 15 + "px" + " 楷体";
    texasContext.fillStyle = "#F0E68C";
    texasContext.fillText(message, 290, 138);
}
//清除胜利玩家赢得筹码数量提示
function clearSkillMessage() {
    clearRectByBackGround(285, 110, 430, 40);
}

//大厅画排行榜
function drawRankList(rankList) {
    for (var i in rankList) {
        if(i>4){
            break;
        }
        var player = rankList[i];
        var point = getRankPosition(i);
        var x = point.x;
        var y = point.y;
        // 用背景图擦除原来的位置
        clearRectByBackGround(x + playerRecWidth, y, playerRecWidth, playerRecHeight);
        // 玩家名 设置字体，颜色
        texasContext.font = screenScale * 10 + "px" + " 楷体";
        texasContext.fillStyle = "#fff";
        texasContext.fillText(parseInt(i)+1, x - 20, y+playerRecHeight/2);
        drawPlayer(x, y, player);
        // 玩家信息背景灰色
        /* 设置填充颜色 */
        texasContext.fillStyle = "#604D4F";
        texasContext.fillRect(x + playerRecWidth, y, playerRecWidth, playerRecHeight);
        // 玩家名 设置字体，颜色
        texasContext.font = screenScale * 10 + "px" + " 楷体";
        texasContext.fillStyle = "#fff";
        texasContext.fillText(player.userName, x + playerRecWidth + 10, y + screenScale * 10);
        // 玩家剩余筹码 设置字体，颜色
        texasContext.font = screenScale * 10 + "px" + " 楷体";
        texasContext.fillStyle = "#fff";
        texasContext.fillText("$" + player.chips, x + playerRecWidth + 5, y + screenScale * 40);
    }
}
function getRankPosition(rank) {
    var rankHeight = playerRecHeight * rank;
    var point = {};
    point.x = screenWidth * 0.7;
    point.y = screenHeight * 0.2 + rankHeight;
    return point;
}
