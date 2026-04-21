
function escapeHtml(value) {
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

function getSkillTypeMeta(type) {
  if(type == '2') {
    return {label: "防御", cssClass: "type-2"};
  }
  if(type == '3') {
    return {label: "陷阱", cssClass: "type-3"};
  }
  return {label: "主动", cssClass: "type-1"};
}

function getPlayerSkillBadge(player) {
  if (player.id == myInfo.id) {
    return "<span class='player-badge self'>你自己</span>";
  }
  if (player.isFold) {
    return "<span class='player-badge fold'>已弃牌</span>";
  }
  return "<span class='player-badge wait'>" + (player.isSkillUsed ? "技能可用" : "等待时机") + "</span>";
}

function buildPlayerMetric(label, value) {
  return "<div class='player-metric'><span class='player-metric-label'>" + label + "</span><div class='player-metric-value'>" + value + "</div></div>";
}

function buildSkillCardAction(item, player) {
  var skillDictionaryNo = item.skillDictionaryNo;
  var tokenId = item.tokenId == '' || item.tokenId == undefined ? '' : item.tokenId;
  if(item.type == '1' && item.pointTo == '1') {
    return "selectPlayerSkill('" + skillDictionaryNo + "', '" + tokenId + "')";
  }
  if(skillDictionaryNo == 'SDN_ACTIVE_11') {
    return "selectReverseRound('" + skillDictionaryNo + "', '" + tokenId + "')";
  }
  return "useSkill('" + skillDictionaryNo + "','', '" + tokenId + "')";
}

function buildSkillCard(item, player) {
  if(item == null || item == undefined) {
    return "";
  }
  var skillDictionaryNo = item.skillDictionaryNo;
  var skillNameZh = item.skillNameZh;
  var power = item.power;
  var image = item.image;
  var type = item.type;
  var level = item.level;
  var useRound = item.useRound;
  var description = item.description;
  var constrains = item.constrains == '' || item.constrains == undefined ? '无' : item.constrains;
  var count = item.count == -1 ? '无限' : item.count;
  var typeMeta = getSkillTypeMeta(type);
  var canUse = player.isSkillUsed && player.power >= power;
  var clickableClass = canUse ? " clickable" : " disabled";
  var onclickAttr = canUse ? " onclick=\"" + buildSkillCardAction(item, player) + "\"" : "";

  var skillCardHtml = "<div class='skill-card-item " + typeMeta.cssClass + clickableClass + "'" + onclickAttr + ">";
  skillCardHtml += "<div class='skill-card-tooltip'>";
  skillCardHtml += "<div>技能描述：" + escapeHtml(description) + "</div>";
  skillCardHtml += "<div>使用回合：" + escapeHtml(useRound) + "</div>";
  skillCardHtml += "<div>限制条件：" + escapeHtml(constrains) + "</div>";
  skillCardHtml += "</div>";
  skillCardHtml += "<div class='skill-card-top'>";
  skillCardHtml += "<span class='skill-type-pill " + typeMeta.cssClass + "'>" + typeMeta.label + "</span>";
  skillCardHtml += "<span class='skill-level'>" + escapeHtml(getSkillCardLevel(level)) + "</span>";
  skillCardHtml += "</div>";
  skillCardHtml += "<img class='skill-card-thumb' src='" + escapeHtml(image) + "' alt='" + escapeHtml(skillNameZh) + "'>";
  skillCardHtml += "<div class='skill-card-name'>" + escapeHtml(skillNameZh) + "</div>";
  skillCardHtml += "<div class='skill-card-stats'>";
  skillCardHtml += "<div class='skill-stat'>能量消耗<strong>" + escapeHtml(power) + "</strong></div>";
  if(roomInfo.type == 2) {
    skillCardHtml += "<div class='skill-stat'>剩余数量<strong>" + escapeHtml(count) + "</strong></div>";
  } else {
    skillCardHtml += "<div class='skill-stat'>可用状态<strong>" + (canUse ? "可释放" : "不可用") + "</strong></div>";
  }
  skillCardHtml += "</div>";
  skillCardHtml += "</div>";
  return skillCardHtml;
}

// 画玩家信息
function drawPlayerSkillInfos() {
  // 只有type为1、2两种房间支持卡牌
  if(roomInfo.type == 0) {
    return;
  }
  if(commonSkillDrawed) {
    return;
  }
  commonSkillDrawed = true;
  $("#skill_play_table").html("");
  $("#trapCount").html(roomInfo.skillTrapCount);
  var orderedPlayers = [];
  for (var i in roomInfo.ingamePlayers) {
    var currentPlayer = roomInfo.ingamePlayers[i];
    if (currentPlayer.id == myInfo.id) {
      orderedPlayers.unshift(currentPlayer);
    } else {
      orderedPlayers.push(currentPlayer);
    }
  }
  for (var j in orderedPlayers) {
    var playerSkillCards = null;
    var player = orderedPlayers[j];
    if (player.id == myInfo.id) {
      playerSkillCards = roomInfo.playerSkillCards;
      //alert("drawPlayerSkillInfos:"+player.isSkillUsed);
    }
    drawPlayerSkillInfo(player, playerSkillCards);
  }
}

// 绘制玩家卡牌相关信息
function drawPlayerSkillInfo(player, playerSkillCards) {
  var isSelf = player.id == myInfo.id;
  var cardClass = "player-skill-card compact" + (isSelf ? " self" : "");
  var skill_player_info = "<div id='skill_player_info_"+player.id+"' class='" + cardClass + "'>";
  skill_player_info += "<div class='player-skill-head'>";
  skill_player_info += "<img class='player-avatar' src='" + escapeHtml(getPlayerAvatarUrl(player)) + "' alt='" + escapeHtml(player.userName) + "' onerror=\"" + getPlayerAvatarErrorHandler() + "\">";
  skill_player_info += "<div class='player-main'>";
  skill_player_info += "<div class='player-name-row'><div class='player-name'>" + escapeHtml(player.userName) + "</div>" + getPlayerSkillBadge(player) + "</div>";
  skill_player_info += "<div class='player-status-line'>当前状态：" + (player.isFold ? "已弃牌，本局不再操作" : (player.isSkillUsed ? "本回合可以使用技能" : "暂时无法使用技能")) + "</div>";
  skill_player_info += "</div></div>";

  skill_player_info += "<div class='player-metrics'>";
  skill_player_info += buildPlayerMetric("能量", escapeHtml(player.power));
  skill_player_info += buildPlayerMetric("技能卡", escapeHtml(player.playerSkillCardsCount));
  skill_player_info += buildPlayerMetric("防御状态", escapeHtml(player.playerSkillDefenseCount));
  skill_player_info += buildPlayerMetric("座位号", escapeHtml(player.seatNum));
  skill_player_info += "</div>";

  if(playerSkillCards != null) {
    skill_player_info += "<div class='skill-hand-title'><span>我的技能手牌</span><div class='skill-hand-tip'>悬停查看说明，亮色卡牌可直接点击释放</div></div>";
    if (playerSkillCards.length > 0) {
      skill_player_info += "<div class='skill-card-grid'>";
      for(var i in playerSkillCards) {
        skill_player_info += buildSkillCard(playerSkillCards[i], player);
      }
      skill_player_info += "</div>";
    } else {
      skill_player_info += "<div class='skill-empty-hand'>当前还没有可展示的技能手牌，回合推进后会自动补充。</div>";
    }
  }

  skill_player_info += "</div>";
  $("#skill_play_table").append(skill_player_info);
}

function getSkillCardLevel(level) {
  if(level == '0') {
    return "N";
  }
  if(level == '1') {
    return "R";
  }
  if(level == '2') {
    return "SR";
  }
  if(level == '3') {
    return "SSR";
  }
  if(level == '4') {
    return "UR";
  }
}


function selectPlayerSkill(skillDictionaryNo, tokenId) {
  $("#selectPlayer").appendTo("body");
  $("#selectPlayerContent").html("");
  var skill_player_info = "";
  for (var i in roomInfo.ingamePlayers) {
    var playerSkillCards = null;
    var player = roomInfo.ingamePlayers[i];
    if (!player.isFold) {
      skill_player_info += "<div class='selection-item'>";
      skill_player_info += "<img class='player-avatar' src='" + escapeHtml(getPlayerAvatarUrl(player)) + "' alt='" + escapeHtml(player.userName) + "' onerror=\"" + getPlayerAvatarErrorHandler() + "\">";
      skill_player_info += "<div class='selection-main'>";
      skill_player_info += "<div class='selection-name'>玩家：" + escapeHtml(player.userName) + "</div>";
      skill_player_info += "<div class='selection-meta'>能量：" + escapeHtml(player.power) + " ｜ 技能卡：" + escapeHtml(player.playerSkillCardsCount) + " ｜ 防御状态：" + escapeHtml(player.playerSkillDefenseCount) + "</div>";
      skill_player_info += "</div>";
      if(player.id != myInfo.id || skillDictionaryNo == 'SDN_ACTIVE_25') {
        skill_player_info +="<button class='selection-btn' onclick=\"useSkill('"+skillDictionaryNo+"','"+player.id+"', '"+tokenId+"','')\">选择</button>";
      }
      skill_player_info +="</div>";
    }
  }
  $("#selectPlayerContent").html("<div class='selection-list'>" + skill_player_info + "</div>");
  $("#selectPlayer").show();
}

function selectReverseRound(skillDictionaryNo, tokenId) {
  $("#selectReverseRound").appendTo("body");
  $("#selectReverseRoundContent").html("");
  var skill_round_info = "<div class='selection-actions'>";
    if(roomInfo.currentRound == "F") {
      skill_round_info += "<button class='selection-btn' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',1)\">翻牌</button>";
    }
    if(roomInfo.currentRound == "T") {
      skill_round_info += "<button class='selection-btn' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',1)\">翻牌</button>";
      skill_round_info += "<button class='selection-btn' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',2)\">转牌</button>";
    }
    if(roomInfo.currentRound == "R") {
      skill_round_info += "<button class='selection-btn' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',1)\">翻牌</button>";
      skill_round_info += "<button class='selection-btn' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',2)\">转牌</button>";
      skill_round_info += "<button class='selection-btn' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',3)\">河牌</button>";
    }
    skill_round_info +="</div>";
  $("#selectReverseRoundContent").html(skill_round_info);
  $("#selectReverseRound").show();
}
function assignChipsInfo(skillDictionaryNo, tokenId) {
  $("#assignChipsInfo").show();
}

function clearSkillDiv() {
  $("#skill_play_table").html("");
}
