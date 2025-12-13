package com.yulink.texas.common.web.result;

import com.yulink.texas.common.model.PageInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Need class description here...
 *
 * @Author: liupanpan
 * @Date: 2019/1/12
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */

@ApiModel("分页数据")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageInfoResult<E> {

    @ApiModelProperty(name = "页码", dataType = "int", example = "1")
    private int pageNum;

    @ApiModelProperty(value = "页面大小", dataType = "int", example = "50")
    private int pageSize;

    @ApiModelProperty(value = "总数", dataType = "long", example = "500")
    private long total;

    @ApiModelProperty(value = "总页数", dataType = "int", example = "10")
    private int pages;

    @ApiModelProperty(value = "数据")
    private List<E> list;

    /**
     * 只是把分页数据转换, 列表数据需要外部实现
     */
    public static <T, S> PageInfoResult<T> create(PageInfo<S> pageInfo) {
        PageInfoResult<T> result = new PageInfoResult<>();
        if (pageInfo == null) {
            return result;
        }
        result.setPageNum(pageInfo.getPageNum());
        result.setPages(pageInfo.getPages());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setList(new ArrayList<>());
        return result;
    }

}
