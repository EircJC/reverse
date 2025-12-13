package com.yulink.texas.common.admin.model;

import com.yulink.texas.common.admin.constant.OrderType;
import lombok.Data;

@Data
public class PageAndOrder {

    /** 页码 **/
    private int page;
    /** 分页大小 **/
    private int pageSize;
    /** 排序字段 **/
    private String orderColumn;
    /** 排序方式asc/desc **/
    private OrderType order;

    public String getOrderBy() {
        return orderColumn + " " + order.getClause();
    }
}
