
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
  for (var i in roomInfo.ingamePlayers) {
    var playerSkillCards = null;
    var player = roomInfo.ingamePlayers[i];
    if (player.id == myInfo.id) {
      playerSkillCards = roomInfo.playerSkillCards;
      //alert("drawPlayerSkillInfos:"+player.isSkillUsed);
    }
    drawPlayerSkillInfo(player, playerSkillCards);
  }
}

// 绘制玩家卡牌相关信息
function drawPlayerSkillInfo(player, playerSkillCards) {
  var skill_player_info = "<tr id='skill_player_info_"+player.id+"'>";
    skill_player_info += "<td><input type='image' src='"+player.picLogo+"' style='width: 70px;height: 70px;'></td>";
    skill_player_info += "<td>玩家："+player.userName+"</td>";
    skill_player_info += "<td>能量："+player.power+"</td>";
    skill_player_info += "<td>卡牌数量："+player.playerSkillCardsCount+"</td>";
    skill_player_info += "<td>防御状态数量："+player.playerSkillDefenseCount+"</td>";
    skill_player_info +="</tr>";

  if(playerSkillCards != null && playerSkillCards.length > 0) {
    skill_player_info += "<tr id='skill_player_cardlist_"+player.id+"'><td colSpan='5'>";
    skill_player_info += "<table>";

    var isSkillUsed = player.isSkillUsed;
    var index = 0; //6个卡牌循环一次计数，用于补充tr换行
    // skill_player_info += "<tr>";
    for(var i in playerSkillCards) {
      var item = playerSkillCards[i];
      if(item == null || item == undefined) {
          continue;
      }
      var skillDictionaryNo = item.skillDictionaryNo;
      var skillNameZh = item.skillNameZh;
      var power = item.power;
      var image = item.image;
      var type = item.type;
      var pointTo = item.pointTo;
      var level = item.level;
      var useRound = item.useRound;
      var description = item.description;
      var constrains = item.constrains == '' || item.constrains == undefined?'无':item.constrains;
      var count = item.count == -1?'无限':item.count;
      var tokenId = item.tokenId == '' || item.tokenId == undefined?'':item.tokenId;
      // if(index == 0) {
      //   skill_player_info += "<tr>";
      // }
      var typeColor = 'red';
      if(type == '2') {
        typeColor = 'blue';
      }
      if(type == '3') {
        typeColor = 'green';
      }
      var typeStr = "主动";
      if(type == '2') {
        typeStr = "防御";
      }
      if(type == '3') {
        typeStr = "陷阱";
      }
      skill_player_info += "<td style=\"border: 1px solid red\" id=\"skill_player_card_"+player.id+"_"+skillDictionaryNo+"\" " ;
      if(isSkillUsed) { // 当前回合玩家可以使用技能时（未被冻结等技能卡攻击）
        if(player.power >= power ) {
          if(type == '1' && pointTo == '1') {
            skill_player_info += " onclick=\"selectPlayerSkill('"+skillDictionaryNo+"', '"+tokenId+"')\" ";
          } else if(skillDictionaryNo == 'SDN_ACTIVE_11'){
            skill_player_info += " onclick=\"selectReverseRound('"+skillDictionaryNo+"', '"+tokenId+"')\" ";
          } else {
            skill_player_info += " onclick=\"useSkill('"+skillDictionaryNo+"','', '"+tokenId+"')\" ";
          }

        }
      }

      skill_player_info += " onmouseover=\"$('#skill_player_cardInfo_"+player.id+"_"+skillDictionaryNo+"').show()\" onmouseout=\"$('#skill_player_cardInfo_"+player.id+"_"+skillDictionaryNo+"').hide()\">";
      skill_player_info += "<div id=\"skill_player_cardInfo_"+player.id+"_"+skillDictionaryNo+"\" style=\"display: none; height: 300px; width: 160px; background-color: black; position:absolute; z-index:9;\">";
      skill_player_info += "<p style=\"color: white\">技能描述："+description+"</p>";
      skill_player_info += "<p style=\"color: "+typeColor+"\">类型："+typeStr+"</p>";
      skill_player_info += "<p style=\"color: white\">使用回合："+useRound+"</p>";
      skill_player_info += "<p style=\"color: white\">限制条件："+constrains+"</p>";
      skill_player_info += "</div>";

      // 单独处理图片滤镜
      skill_player_info += "<input type=\"image\" src=\""+image+"\"" ;
      skill_player_info += " style=\"width: 100px;height: 130px;" ;
      if(power > player.power || !isSkillUsed) {
        skill_player_info += "filter: grayscale(1);";
      }
      skill_player_info += "\">";
      skill_player_info += "<br><p style=\"color: red\">名称："+skillNameZh+"</p> <br>";
      skill_player_info += "能量消耗："+power+"<br>";
      if(roomInfo.type == 2) {
        skill_player_info += "剩余数量："+count+"<br>";
      }
      skill_player_info += "级别："+getSkillCardLevel(level);
      skill_player_info += "</td>";

      if(index == 5) { // 够6个卡牌就结束一次tr，然后index重新置位0
        skill_player_info += "</tr>";
        index = 0;
      } else {
        index ++;
      }
      if(playerSkillCards.length %6 !=0 && i == playerSkillCards.length-1 ) {
        skill_player_info += "</tr>";
      }

    }
    // alert(isSkillUsed+"   "+playerSkillCards.length %3+"   "+(playerSkillCards.length-1));
    // skill_player_info += "</tr>";
    skill_player_info += "</table></td></tr>";

  }
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
  $("#selectPlayerContent").html("");
  var skill_player_info = "";
  for (var i in roomInfo.ingamePlayers) {
    var playerSkillCards = null;
    var player = roomInfo.ingamePlayers[i];
    if (!player.isFold) {
      skill_player_info += "<tr>";
      skill_player_info += "<td><input type='image' src='"+player.picLogo+"' style='width: 70px;height: 70px;'></td>";
      skill_player_info += "<td>玩家："+player.userName+"</td>";
      skill_player_info += "<td>能量："+player.power+"</td>";
      skill_player_info += "<td>卡牌数量："+player.playerSkillCardsCount+"</td>";
      skill_player_info += "<td>防御状态数量："+player.playerSkillDefenseCount+"</td>";
      skill_player_info += "<td>";
      if(player.id != myInfo.id || skillDictionaryNo == 'SDN_ACTIVE_25') {
        skill_player_info +="<input type='button' value='选择' onclick=\"useSkill('"+skillDictionaryNo+"','"+player.id+"', '"+tokenId+"','')\">";
      }
      skill_player_info +="</td></tr>";
    }
  }
  $("#selectPlayerContent").html(skill_player_info);
  $("#selectPlayer").show();
}

function selectReverseRound(skillDictionaryNo, tokenId) {
  $("#selectReverseRoundContent").html("");
  var skill_round_info = "";
    skill_round_info += "<tr>";
    if(roomInfo.currentRound == "F") {
      skill_round_info += "<td><input type='button' value='翻牌' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',1)\"></td>";
    }
    if(roomInfo.currentRound == "T") {
      skill_round_info += "<td><input type='button' value='翻牌' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',1)\"></td>";
      skill_round_info += "<td><input type='button' value='转牌' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',2)\"></td>";
    }
    if(roomInfo.currentRound == "R") {
      skill_round_info += "<td><input type='button' value='翻牌' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',1)\"></td>";
      skill_round_info += "<td><input type='button' value='转牌' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',2)\"></td>";
      skill_round_info += "<td><input type='button' value='河牌' onclick=\"useSkill('"+skillDictionaryNo+"','"+myInfo.id+"', '"+tokenId+"',3)\"></td>";
    }
    skill_round_info +="</tr>";
  $("#selectReverseRoundContent").html(skill_round_info);
  $("#selectReverseRound").show();
}
function assignChipsInfo(skillDictionaryNo, tokenId) {
  $("#assignChipsInfo").show();
}

function clearSkillDiv() {
  $("#skill_play_table").html("");
}