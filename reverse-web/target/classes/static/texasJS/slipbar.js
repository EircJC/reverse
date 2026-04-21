/**
 * Created by xll on 2016/11/12.
 */
var DrawSlipBar = {
    betChips: 0,
    betChipsMax: 0,
    betChipsMin: 0,
    p: {
        isDown:false
    },
    toolbar: {w: 180},
    initEvents: false,
    position: {
        startx: screenWidth * 0.06,
        endx: screenWidth * 0.06 + 180 - 25,
        x: screenWidth * 0.06,
        y: screenHeight * 0.87,
        w: 25,
        h: 25
    },
    getTrackWidth: function () {
        return this.position.endx - this.position.startx;
    },
    getBetRatio: function () {
        var range = this.betChipsMax - this.betChipsMin;
        if (range <= 0) {
            return 0;
        }
        return (this.betChips - this.betChipsMin) / range;
    },
    syncPositionFromBet: function () {
        var ratio = this.getBetRatio();
        this.position.x = this.position.startx + this.getTrackWidth() * ratio;
    },
    syncBetFromPosition: function () {
        var currentBet = myInfo != null && myInfo.betChips != null ? myInfo.betChips : 0;
        var range = this.betChipsMax - this.betChipsMin;
        if (range <= 0) {
            this.betChips = this.betChipsMax;
        } else {
            this.betChips = range * ((this.position.x - this.position.startx) / this.getTrackWidth());
            this.betChips = this.betChips + this.betChipsMin;
        }
        this.betChips = Math.ceil(this.betChips);
        if (roomInfo.bigBet != null && roomInfo.bigBet > 0) {
            this.betChips = Math.floor((this.betChips + currentBet) / roomInfo.bigBet) * roomInfo.bigBet - currentBet;
        }
        if (this.betChips < this.betChipsMin) {
            this.betChips = this.betChipsMin;
        }
        if (this.betChips > this.betChipsMax) {
            this.betChips = this.betChipsMax;
        }
    },
    init: function () {
        if (!this.initEvents) {
            this.event();
            this.initEvents = true
        }
        DrawSlipBar.draw(DrawSlipBar.position.x, DrawSlipBar.position.y, DrawSlipBar.position.w, DrawSlipBar.position.h, "blue");
    },
    draw: function (x, y, w, h, color) {
        if (x == null || x == undefined) {
            this.syncPositionFromBet();
            x = this.position.x;
        }
        if (y == null || y == undefined) {
            y = this.position.y;
        }
        if (w == null || w == undefined) {
            w = this.position.w;
        }
        if (h == null || h == undefined) {
            h = this.position.h;
        }
        var panelX = DrawSlipBar.position.startx - 24;
        var panelY = DrawSlipBar.position.y - 22;
        var panelWidth = DrawSlipBar.toolbar.w + 118;
        var panelHeight = 48;

        clearRectByBackGround(panelX - 8, panelY - 12, panelWidth + 16, panelHeight + 34);
        if (x < DrawSlipBar.position.startx) {
            x = DrawSlipBar.position.startx;
        }
        if (x > DrawSlipBar.position.endx + 2) {
            x = DrawSlipBar.position.endx + 2;
        }
        DrawSlipBar.position.x = x;
        DrawSlipBar.position.y = y;
        DrawSlipBar.position.w = w;
        DrawSlipBar.position.h = h;
        DrawSlipBar.syncBetFromPosition();

        texasContext.globalAlpha = 1;
        texasContext.fillStyle = "rgba(8, 14, 28, 0.88)";
        drawRoundedRectPath(panelX, panelY, panelWidth, panelHeight, 16);
        texasContext.fill();
        texasContext.lineWidth = 1;
        texasContext.strokeStyle = "rgba(255,255,255,0.08)";
        texasContext.stroke();

        texasContext.font = "600 " + (screenScale * 9) + "px PingFang SC";
        texasContext.fillStyle = "rgba(216, 226, 255, 0.78)";
        texasContext.fillText("RAISE", panelX + 14, panelY + 14);

        var trackX = DrawSlipBar.position.startx;
        var trackY = DrawSlipBar.position.y + 9;
        var trackHeight = 8;
        var trackRadius = 6;
        var filledWidth = Math.max(0, DrawSlipBar.position.x - DrawSlipBar.position.startx + DrawSlipBar.position.w / 2);

        texasContext.fillStyle = "rgba(255,255,255,0.10)";
        drawRoundedRectPath(trackX, trackY, DrawSlipBar.toolbar.w, trackHeight, trackRadius);
        texasContext.fill();

        var trackGradient = texasContext.createLinearGradient(trackX, trackY, trackX + DrawSlipBar.toolbar.w, trackY);
        trackGradient.addColorStop(0, "#ffd36f");
        trackGradient.addColorStop(1, "#ff8f4f");
        texasContext.fillStyle = trackGradient;
        drawRoundedRectPath(trackX, trackY, Math.min(filledWidth, DrawSlipBar.toolbar.w), trackHeight, trackRadius);
        texasContext.fill();

        texasContext.font = screenScale * 8 + "px" + " PingFang SC";
        texasContext.fillStyle = "rgba(214, 223, 250, 0.62)";
        texasContext.fillText("MIN " + DrawSlipBar.betChipsMin, trackX, panelY + panelHeight - 6);
        texasContext.fillText("MAX " + DrawSlipBar.betChipsMax, trackX + DrawSlipBar.toolbar.w - 52, panelY + panelHeight - 6);

        texasContext.fillStyle = "rgba(255, 211, 111, 0.16)";
        drawRoundedRectPath(trackX + DrawSlipBar.toolbar.w + 12, panelY + 10, 76, 28, 14);
        texasContext.fill();
        texasContext.strokeStyle = "rgba(255, 211, 111, 0.26)";
        texasContext.stroke();

        texasContext.font = "600 " + (screenScale * 12) + "px PingFang SC";
        texasContext.fillStyle = "#ffe4a0";
        texasContext.fillText("$" + DrawSlipBar.betChips, trackX + DrawSlipBar.toolbar.w + 22, panelY + 29);

        //拖动滑块
        texasContext.shadowColor = "rgba(255, 176, 71, 0.34)";
        texasContext.shadowBlur = 16;
        texasContext.drawImage(chipsImage, 0, 0, 160,
            160, x, y, w, h);
        texasContext.shadowBlur = 0;
    },
    OnMouseMove: function (evt) {
        var point = getCanvasPointerPosition(evt);
        if (!DrawSlipBar.p.isDown && findCanvasHotspot(point.x, point.y) != null) {
            return;
        }
        if (DrawSlipBar.p.isDown) {
            var x = point.x - DrawSlipBar.position.w / 2;
            if (x < DrawSlipBar.position.startx) {
                x = DrawSlipBar.position.startx;
            }
            if (x > DrawSlipBar.position.endx + 2) {
                x = DrawSlipBar.position.endx + 2;
            }
            DrawSlipBar.position.x = x;
            $(texasCanvas).css("cursor", "grabbing");
        }
        // 如果事件位置在矩形区域中,调试会导致的浏览器坐标错误
        if (point.x >= DrawSlipBar.position.x && point.x <= DrawSlipBar.position.x + DrawSlipBar.position.w
            && point.y >= DrawSlipBar.position.y && point.y <= DrawSlipBar.position.y + DrawSlipBar.position.h
        ) {
            $(texasCanvas).css("cursor", DrawSlipBar.p.isDown ? "grabbing" : "grab");
        }
    },
    OnMouseDown: function (evt) {
        var point = getCanvasPointerPosition(evt);
        var X = point.x;
        var Y = point.y;
        if (findCanvasHotspot(X, Y) != null) {
            DrawSlipBar.p.isDown = false;
            return;
        }
        if (X < DrawSlipBar.position.x + DrawSlipBar.position.w && X > DrawSlipBar.position.x) {
            if (Y < DrawSlipBar.position.y + DrawSlipBar.position.h && Y > DrawSlipBar.position.y) {
                DrawSlipBar.p.isDown = true;
                $(texasCanvas).css("cursor", "grabbing");
            }
        }
        else {
            DrawSlipBar.p.isDown = false;
        }
    },
    OnMouseUp: function () {
        DrawSlipBar.p.isDown = false
    },
    OnMouseLeave: function () {
        DrawSlipBar.p.isDown = false;
    },
    event: function () {
        var canvas = texasCanvas;
        canvas.addEventListener("mousedown", this.OnMouseDown.bind(this), false);
        canvas.addEventListener("mousemove", this.OnMouseMove.bind(this), false);
        canvas.addEventListener("mouseup", this.OnMouseUp.bind(this), false);
        canvas.addEventListener("mouseleave", this.OnMouseLeave.bind(this), false);
    }
}
