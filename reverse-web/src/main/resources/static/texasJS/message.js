/**
 * Created by lxr on 2016/10/30.
 */
var websocket = null;
var texasWsState = {
    manualClose: false,
    reconnectTimer: null,
    reconnectAttempts: 0,
    reconnecting: false,
    credentials: null,
    room: null,
    rejoinAfterLogin: false
};
var wsConfig = buildWsConfig(window.TEXAS_WS_CONFIG || {});
var wsip = buildWsUrl();
var wsReconnectEnabled = wsConfig.enabled && wsConfig.reconnectEnabled;
var texasSessionKeys = {
    credentials: "texas_ws_credentials",
    room: "texas_ws_room"
};

loadSessionState();

function getMessageStream() {
    return document.getElementById("messageStream");
}

function getMessageStatus() {
    return document.getElementById("messagesStatus");
}

function escapeMessageHtml(value) {
    if (value == null || value == undefined) {
        return "";
    }
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function getCurrentTimeLabel() {
    var now = new Date();
    var hours = now.getHours();
    var minutes = now.getMinutes();
    var seconds = now.getSeconds();
    return (hours < 10 ? "0" + hours : hours) + ":"
        + (minutes < 10 ? "0" + minutes : minutes) + ":"
        + (seconds < 10 ? "0" + seconds : seconds);
}

function normalizeMessageTone(tone) {
    if (tone == "success" || tone == "warning" || tone == "danger" || tone == "accent") {
        return tone;
    }
    return "info";
}

function guessMessageTone(message) {
    var text = String(message || "");
    if (text.indexOf("成功") >= 0 || text.indexOf("开始") >= 0 || text.indexOf("已连接") >= 0 || text.indexOf("连接成功") >= 0) {
        return "success";
    }
    if (text.indexOf("失败") >= 0 || text.indexOf("异常") >= 0 || text.indexOf("断开") >= 0) {
        return "danger";
    }
    if (text.indexOf("重连") >= 0 || text.indexOf("等待") >= 0 || text.indexOf("未启用") >= 0) {
        return "warning";
    }
    return "info";
}

function updateEmptyMessageState() {
    var messageStream = getMessageStream();
    if (messageStream == null) {
        return;
    }
    if (messageStream.children.length === 0) {
        messageStream.innerHTML = "<div class='message-empty'>连接建立后，这里会自动滚动展示本局的每一步关键动态。</div>";
    } else if (messageStream.children.length === 1 && messageStream.firstChild.className == "message-empty") {
        return;
    }
}

function setMessageStatus(message, tone) {
    var statusNode = getMessageStatus();
    if (statusNode == null) {
        return;
    }
    var finalTone = normalizeMessageTone(tone || guessMessageTone(message));
    statusNode.className = "messages-status" + (finalTone == "info" ? "" : " " + finalTone);
    statusNode.textContent = message;
}

function clearMessageStream() {
    var messageStream = getMessageStream();
    if (messageStream == null) {
        return;
    }
    messageStream.innerHTML = "";
    updateEmptyMessageState();
}

function appendGameMessage(message, tone, allowHtml) {
    var messageStream = getMessageStream();
    if (messageStream == null) {
        return;
    }
    if (messageStream.children.length === 1 && messageStream.firstChild.className == "message-empty") {
        messageStream.innerHTML = "";
    }
    var finalTone = normalizeMessageTone(tone || guessMessageTone(message));
    var entry = document.createElement("div");
    entry.className = "message-entry" + (finalTone == "info" ? "" : " " + finalTone);

    var meta = document.createElement("div");
    meta.className = "message-meta";

    var time = document.createElement("span");
    time.className = "message-time";
    time.textContent = getCurrentTimeLabel();

    var text = document.createElement("div");
    text.className = "message-text";
    text.innerHTML = allowHtml ? String(message) : escapeMessageHtml(message);

    meta.appendChild(time);
    meta.appendChild(text);
    entry.appendChild(meta);
    messageStream.appendChild(entry);
    messageStream.scrollTop = messageStream.scrollHeight;
}

function setGameMessage(message, tone, allowHtml) {
    clearMessageStream();
    appendGameMessage(message, tone, allowHtml);
}

// 发送消息映射
var mapping = {
    // 注册
    regist: 0,
    // 登陆
    login: 1,
    // 进入房间
    enterRoom: 2,
    // 退出房间
    exitRoom: 3,
    // 坐下
    sitDown: 4,
    // 站起
    standUp: 5,
    // 过牌
    check: 6,
    // 下注
    betChips: 7,
    // 弃牌
    fold: 8,
    //获取排行榜
    getRankList: 9,
    // 使用技能
    useSkill: 10,
    // 补充筹码
    assignChips: 11,
    // 查询盲注级别统计
    getRoomLevelStats: 12,
    // 查询指定盲注房间列表
    getRoomList: 13,
    // 按房间号进入房间
    joinRoomByNo: 14,
    // 创建房间并进入
    createRoom: 15,
    // 发送注册邮箱验证码
    sendRegisterCode: 16,
    // 退出登录
    logout: 17
};

function buildWsConfig(rawConfig) {
    return {
        enabled: rawConfig.enabled !== false,
        host: rawConfig.host || "",
        port: rawConfig.port || 9000,
        path: normalizeWsPath(rawConfig.path || "/ws/texas"),
        reconnectEnabled: rawConfig.reconnectEnabled !== false,
        reconnectInitialDelayMillis: rawConfig.reconnectInitialDelayMillis || 1000,
        reconnectMaxDelayMillis: rawConfig.reconnectMaxDelayMillis || 10000,
        reconnectMaxAttempts: rawConfig.reconnectMaxAttempts || 0
    };
}

function normalizeWsPath(path) {
    if (!path) {
        return "/ws/texas";
    }
    return path.charAt(0) === "/" ? path : "/" + path;
}

function buildWsUrl() {
    var protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
    var host = wsConfig.host || window.location.hostname || "127.0.0.1";
    return protocol + host + ":" + wsConfig.port + wsConfig.path;
}

function wsInit() {
    if (!wsConfig.enabled) {
        updateWsStatus("WebSocket 未启用");
        return;
    }
    if (websocket && (websocket.readyState === WebSocket.OPEN || websocket.readyState === WebSocket.CONNECTING)) {
        return;
    }
    clearReconnectTimer();
    wsip = buildWsUrl();
    texasWsState.manualClose = false;
    if ("WebSocket" in window) {
        websocket = new WebSocket(wsip);
    } else if ("MozWebSocket" in window) {
        websocket = new MozWebSocket(wsip);
    } else {
        updateWsStatus("当前浏览器不支持 WebSocket");
        return;
    }
    bindWsFunction(websocket);
}

function bindWsFunction(ws) {
    ws.onerror = function (event) {
        onError(event);
    };
    ws.onopen = function (event) {
        onOpen(event);
    };
    ws.onmessage = function (event) {
        onMessage(event);
    };
    ws.onclose = function (event) {
        onClose(event);
    };
}

/**
 * 接收服务器消息
 */
function onMessage(event) {
    if (event.data != null) {
        var dataJson = JSON.parse(event.data);
        var func = eval(dataJson.action);
        if (typeof func !== "function") {
            console.log("unknown server action: " + dataJson.action);
            return;
        }
        new func(null, dataJson);
        console.log(dataJson.action + " is call by server!");
    }
}

/**
 * 建立连接
 */
function onOpen() {
    texasWsState.reconnecting = false;
    texasWsState.reconnectAttempts = 0;
    clearReconnectTimer();
    updateWsStatus("连接成功 " + wsip);
    if (texasWsState.credentials != null) {
        texasWsState.rejoinAfterLogin = texasWsState.room != null;
        sendWsPayload(buildLoginPayload(texasWsState.credentials.userName, texasWsState.credentials.userpwd));
    }
}

function onError(event) {
    console.log(event);
    updateWsStatus("连接异常，准备重连...");
}

function onClose() {
    websocket = null;
    if (texasWsState.manualClose) {
        updateWsStatus("连接已关闭");
        return;
    }
    scheduleReconnect();
}

function scheduleReconnect() {
    if (!wsReconnectEnabled) {
        updateWsStatus("连接断开");
        return;
    }
    if (texasWsState.reconnectTimer != null) {
        return;
    }
    texasWsState.reconnectAttempts++;
    if (wsConfig.reconnectMaxAttempts > 0 && texasWsState.reconnectAttempts > wsConfig.reconnectMaxAttempts) {
        updateWsStatus("连接断开，已停止重连");
        return;
    }
    texasWsState.reconnecting = true;
    var exponent = Math.max(texasWsState.reconnectAttempts - 1, 0);
    var delay = wsConfig.reconnectInitialDelayMillis * Math.pow(2, exponent);
    delay = Math.min(delay, wsConfig.reconnectMaxDelayMillis);
    updateWsStatus("连接断开，" + Math.round(delay / 1000) + " 秒后重连...");
    texasWsState.reconnectTimer = window.setTimeout(function () {
        texasWsState.reconnectTimer = null;
        wsInit();
    }, delay);
}

function clearReconnectTimer() {
    if (texasWsState.reconnectTimer != null) {
        window.clearTimeout(texasWsState.reconnectTimer);
        texasWsState.reconnectTimer = null;
    }
}

function updateWsStatus(message) {
    setMessageStatus(message, guessMessageTone(message));
}

function rememberCredentials(userName, userpwd) {
    texasWsState.credentials = {
        userName: userName,
        userpwd: userpwd
    };
    saveSessionJson(texasSessionKeys.credentials, texasWsState.credentials);
}

function clearCredentials() {
    texasWsState.credentials = null;
    texasWsState.rejoinAfterLogin = false;
    removeSessionItem(texasSessionKeys.credentials);
}

function rememberRoom(type, level, roomNo) {
    texasWsState.room = {
        type: type,
        level: level,
        roomNo: roomNo || null
    };
    saveSessionJson(texasSessionKeys.room, texasWsState.room);
}

function clearRoomMemory() {
    texasWsState.room = null;
    texasWsState.rejoinAfterLogin = false;
    removeSessionItem(texasSessionKeys.room);
}

function shouldRejoinRoom() {
    return texasWsState.rejoinAfterLogin && texasWsState.room != null;
}

function markRoomRejoinCompleted() {
    texasWsState.rejoinAfterLogin = false;
}

function buildLoginPayload(userName, userpwd) {
    return {
        action: mapping.login,
        userName: userName,
        userpwd: userpwd
    };
}

function loadSessionState() {
    texasWsState.credentials = readSessionJson(texasSessionKeys.credentials);
    texasWsState.room = readSessionJson(texasSessionKeys.room);
}

function readSessionJson(key) {
    try {
        if (window.sessionStorage == null) {
            return null;
        }
        var rawValue = window.sessionStorage.getItem(key);
        if (rawValue == null || rawValue === "") {
            return null;
        }
        return JSON.parse(rawValue);
    } catch (e) {
        return null;
    }
}

function saveSessionJson(key, value) {
    try {
        if (window.sessionStorage == null) {
            return;
        }
        window.sessionStorage.setItem(key, JSON.stringify(value));
    } catch (e) {
    }
}

function removeSessionItem(key) {
    try {
        if (window.sessionStorage == null) {
            return;
        }
        window.sessionStorage.removeItem(key);
    } catch (e) {
    }
}

function sendWsPayload(data) {
    if (data == null) {
        return false;
    }
    if (websocket == null || websocket.readyState !== WebSocket.OPEN) {
        updateWsStatus("连接尚未建立，正在尝试重连...");
        if (websocket == null || websocket.readyState === WebSocket.CLOSED) {
            scheduleReconnect();
        }
        return false;
    }
    websocket.send(JSON.stringify(data));
    return true;
}

function closeWsConnection() {
    texasWsState.manualClose = true;
    clearReconnectTimer();
    if (websocket != null) {
        websocket.close();
    }
}

// 发送消息
function sendMessage() {
    var data = {};
    data.c = mapping.sendMessage;
    sendWsPayload(data);
}

// 错误消息返回
function onException(e, data) {
    console.log(data.message);
}
