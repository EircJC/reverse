package com.yulink.texas.core.mapper;

import com.yulink.texas.core.domain.Order;
import com.yulink.texas.core.domain.OrderExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper {
    long countByExample(OrderExample example);

    int deleteByExample(OrderExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Order record);

    int insertSelective(@Param("record") Order record, @Param("selective") Order.Column ... selective);

    Order selectOneByExample(OrderExample example);

    Order selectOneByExampleSelective(@Param("example") OrderExample example, @Param("selective") Order.Column ... selective);

    List<Order> selectByExampleSelective(@Param("example") OrderExample example, @Param("selective") Order.Column ... selective);

    List<Order> selectByExample(OrderExample example);

    Order selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") Order.Column ... selective);

    Order selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Order record, @Param("example") OrderExample example, @Param("selective") Order.Column ... selective);

    int updateByExample(@Param("record") Order record, @Param("example") OrderExample example);

    int updateByPrimaryKeySelective(@Param("record") Order record, @Param("selective") Order.Column ... selective);

    int updateByPrimaryKey(Order record);

    int batchInsert(@Param("list") List<Order> list);

    int batchInsertSelective(@Param("list") List<Order> list, @Param("selective") Order.Column ... selective);

    int upsert(Order record);

    int upsertSelective(@Param("record") Order record, @Param("selective") Order.Column ... selective);
}
