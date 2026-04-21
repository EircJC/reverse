package com.yulink.texas.controller;

import com.yulink.texas.config.WebSocketClientProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/13
 * @Copyright (c) bitmain.com All Rights Reserved
 */
@Controller
public class IndexController {

    private final WebSocketClientProperties webSocketClientProperties;

    public IndexController(WebSocketClientProperties webSocketClientProperties) {
        this.webSocketClientProperties = webSocketClientProperties;
    }

    @RequestMapping("/texasIndex")
    public String texasIndex(Model model) {
        model.addAttribute("wsEnabled", webSocketClientProperties.isEnabled());
        model.addAttribute("wsHost", webSocketClientProperties.getHost());
        model.addAttribute("wsPort", webSocketClientProperties.getPort());
        model.addAttribute("wsPath", webSocketClientProperties.normalizedPath());
        model.addAttribute("wsReconnectEnabled", webSocketClientProperties.isReconnectEnabled());
        model.addAttribute("wsReconnectInitialDelayMillis", webSocketClientProperties.getReconnectInitialDelayMillis());
        model.addAttribute("wsReconnectMaxDelayMillis", webSocketClientProperties.getReconnectMaxDelayMillis());
        model.addAttribute("wsReconnectMaxAttempts", webSocketClientProperties.getReconnectMaxAttempts());
        return "texasIndex";
    }
}
