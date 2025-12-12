package com.yulink.texas.core.mapper;

import com.yulink.texas.core.domain.TransactionLog;
import com.yulink.texas.core.domain.TransactionLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TransactionLogMapper {
    long countByExample(TransactionLogExample example);

    int deleteByExample(TransactionLogExample example);

    int deleteByPrimaryKey(String transactionhash);

    int insert(TransactionLog record);

    int insertSelective(@Param("record") TransactionLog record, @Param("selective") TransactionLog.Column ... selective);

    TransactionLog selectOneByExample(TransactionLogExample example);

    TransactionLog selectOneByExampleSelective(@Param("example") TransactionLogExample example, @Param("selective") TransactionLog.Column ... selective);

    List<TransactionLog> selectByExampleSelective(@Param("example") TransactionLogExample example, @Param("selective") TransactionLog.Column ... selective);

    List<TransactionLog> selectByExample(TransactionLogExample example);

    TransactionLog selectByPrimaryKeySelective(@Param("transactionhash") String transactionhash, @Param("selective") TransactionLog.Column ... selective);

    TransactionLog selectByPrimaryKey(String transactionhash);

    int updateByExampleSelective(@Param("record") TransactionLog record, @Param("example") TransactionLogExample example, @Param("selective") TransactionLog.Column ... selective);

    int updateByExample(@Param("record") TransactionLog record, @Param("example") TransactionLogExample example);

    int updateByPrimaryKeySelective(@Param("record") TransactionLog record, @Param("selective") TransactionLog.Column ... selective);

    int updateByPrimaryKey(TransactionLog record);

    int batchInsert(@Param("list") List<TransactionLog> list);

    int batchInsertSelective(@Param("list") List<TransactionLog> list, @Param("selective") TransactionLog.Column ... selective);

    int upsert(TransactionLog record);

    int upsertSelective(@Param("record") TransactionLog record, @Param("selective") TransactionLog.Column ... selective);
}