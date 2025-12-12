package com.yulink.texas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/13
 * @Copyright (c) bitmain.com All Rights Reserved
 */
@Controller
public class IndexController {

    @RequestMapping("/texasIndex")
    public String texasIndex() {
        return "texasIndex";
    }
}
