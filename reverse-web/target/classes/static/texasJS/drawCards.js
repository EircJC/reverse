/**
 * Created by lxr on 2016/11/21.
 */
/**
 * h: 红桃
 * s: 黑桃
 * d: 方块
 * c: 梅花
 * @param index
 * @returns {string}
 */
function getCardsColor(index) {
    var color = index.substr(1,1);
    console.log(color);
    return color;
}
//获取A到K
function getCardsValue(index) {
    var value = index.substr(0,1);
    if (value == 'T') {
        value = "10";
    }
    return value;
}
function drawPoker(texasContext, x, y, height, index) {
    var color = getCardsColor(index);
    var value = getCardsValue(index);
    texasContext.drawPokerCard(x, y, height, color, value);
}
function drawPokerBack(texasContext, x, y, height) {
    texasContext.drawPokerBack(x, y, height, '#E71159', '#F97CA6');
}