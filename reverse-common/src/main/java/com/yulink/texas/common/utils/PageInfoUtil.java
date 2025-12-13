package com.yulink.texas.common.utils;

import com.yulink.texas.common.model.PageInfo;
import java.util.ArrayList;
import java.util.List;

public class PageInfoUtil {

    /**
     * 转换分页结果集
     */
    public static <T> PageInfo<T> convertPageInfo(List<T> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        return new PageInfo<>(new com.github.pagehelper.PageInfo<>(list));
    }

}
