package com.yulink.texas.common.web.util;

import com.yulink.texas.common.web.constants.HeaderConstants;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * description ...
 *
 * @Author ruofei.wang
 * @Date 2022/2/23 8:16 下午
 * @Copyright ©2021 XXX .Inc
 */
public class LogUtils {

    public static String fillLogId() {
        String logId = UUID.randomUUID().toString();
        MDC.put(HeaderConstants.X_LOG_ID, logId);
        return logId;
    }

}
