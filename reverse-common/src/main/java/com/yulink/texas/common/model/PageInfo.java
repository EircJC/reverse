package com.yulink.texas.common.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PageInfo<E> {

    /**
     * 页码，从1开始
     */
    private int pageNum;
    /**
     * 页面大小
     */
    private int pageSize;
    /**
     * 总数
     */
    private long total;
    /**
     * 总页数
     */
    private int pages;
    /**
     * 当前页信息
     */
    private List<E> list = new ArrayList<>();

    public PageInfo(com.github.pagehelper.PageInfo<E> pageInfo) {
        pageNum = pageInfo.getPageNum();
        pageSize = pageInfo.getPageSize();
        total = pageInfo.getTotal();
        pages = pageInfo.getPages();
        if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
            list.addAll(pageInfo.getList());
        }
    }
}
