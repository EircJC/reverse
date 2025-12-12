package com.yulink.texas.admin.api;

import com.yulink.texas.admin.bean.SampleInputOption;
import com.yulink.texas.admin.dto.Sample;
import com.yulink.texas.admin.dto.SampleDto;
import com.yulink.texas.common.web.annotations.ResponseResult;
import com.yulink.texas.common.admin.bean.InputItemList;
import com.yulink.texas.common.admin.bean.InputOptionMap;
import com.yulink.texas.common.admin.bean.QueryResult;
import com.yulink.texas.common.admin.constant.StatusEnum;
import com.yulink.texas.common.admin.controller.BaseController;
import com.yulink.texas.common.admin.support.LogMainType;
import com.yulink.texas.common.admin.support.LogType;
import com.yulink.texas.common.admin.support.Loggable;
import com.yulink.texas.common.model.EmptyResponse;
import com.yulink.texas.common.model.PageInfo;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@ResponseResult
@RestController
@Timed(percentiles = {0.9, 0.95, 0.99})
@RequestMapping(value = "/api/v1/sample")
public class AdminSampleController extends BaseController<SampleDto, Sample> {

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @RequiresPermissions("SampleList.add")
    @Loggable(mainType = LogMainType.ADMIN, type = LogType.ModifyEntity, isSysLog = false)
    public EmptyResponse add(
        @RequestBody SampleDto dto
    ) {
        // TODO: 2020/11/18 impl
        return EmptyResponse.create();
    }

    @RequestMapping(value = "/modify", method = RequestMethod.POST)
    @RequiresPermissions("SampleList.modify")
    @Loggable(mainType = LogMainType.ADMIN, type = LogType.ModifyEntity, isSysLog = false)
    public EmptyResponse modifyById(
        @RequestBody SampleDto dto
    ) {
        // TODO: 2020/11/18 impl
        return EmptyResponse.create();
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @RequiresPermissions("SampleList")
    public QueryResult<SampleDto> queryPager(
        // 分页类的数据默认即可
        @RequestParam("page") int page,
        @RequestParam("pageCount") int pageSize,
        @RequestParam(value = "sortBy", required = false) String sortBy,
        @RequestParam(value = "order", required = false) String order,
        // 此处参数使用预先定义好的dto字段
        @RequestParam(value = SampleDto.columnStatus, required = false) String status
    ) {
        // PageAndOrder pageAndOrder = checkPageAndOrder(page, pageSize, sortBy, order, "id");
        // pageAndOrder TODO 可以用于与service交互的后续查询
        PageInfo<Sample> pageInfo = new PageInfo<>();
        return pageResult(pageInfo);
    }

    @RequestMapping(value = "/searchItems", method = RequestMethod.GET)
    @RequiresPermissions("SampleList")
    public InputItemList searchItems() {
        InputOptionMap selectMap = InputOptionMap.create();
        // 多选类的数据需要手动设置
        selectMap.put(SampleDto.columnStatus, SampleInputOption.createStatusEnum(StatusEnum.ENABLED.getStatus()));
        return toSearchInputItemList(selectMap);
    }

    @RequestMapping(value = "/toAdd", method = RequestMethod.GET)
    @RequiresPermissions("SampleList.add")
    public InputItemList toAdd() {
        InputOptionMap selectMap = InputOptionMap.create();
        selectMap.put(SampleDto.columnStatus, SampleInputOption.createStatusEnum(StatusEnum.ENABLED.getStatus()));
        return toCreateInputItemList(selectMap);
    }

    @RequestMapping(value = "/toModify", method = RequestMethod.GET)
    @RequiresPermissions("SampleList.modify")
    public InputItemList toMotify(
        @ApiParam(value = "编号", required = true) @RequestParam("id") Long id
    ) {
        SampleDto dto = new SampleDto();
        InputOptionMap selectMap = InputOptionMap.create();
        selectMap.put(SampleDto.columnStatus, SampleInputOption.createStatusEnum(StatusEnum.ENABLED.getStatus()));
        return toModifyInputItemList(dto, selectMap);
    }

    @Override
    public SampleDto convert(Sample source) {
        // TODO: 2020/11/18 一般需要自定义实现
        return super.convert(source);
    }

    @Override
    public Class<Sample> sourceClass() {
        return Sample.class;
    }

    @Override
    public Class<SampleDto> dtoClass() {
        return SampleDto.class;
    }
}
