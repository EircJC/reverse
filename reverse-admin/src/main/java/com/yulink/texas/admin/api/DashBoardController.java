package com.yulink.texas.admin.api;

import com.yulink.texas.common.web.annotations.ResponseResult;
import com.yulink.texas.common.admin.bean.BaseResult;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "DashBoard")
@RestController
@Timed(percentiles = {0.9, 0.95, 0.99})
@ResponseResult
public class DashBoardController {

    @ApiOperation(value = "DashBoard", response = BaseResult.class)
    @RequestMapping(value = "/api/v1/dashBoard", method = RequestMethod.GET)
    public BaseResult dashBoard() {
        return new BaseResult();
    }

}
