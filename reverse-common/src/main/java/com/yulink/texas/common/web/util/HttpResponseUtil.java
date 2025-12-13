package com.yulink.texas.common.web.util;

import com.yulink.texas.common.utils.mapper.JsonMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class HttpResponseUtil {

    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";
    public static final String UTF_8 = "utf-8";

    /**
     * 将对象转换成json发送给客户端
     * @param response
     * @param data
     * @throws IOException
     */
    public static void sendJsonMessage(HttpServletResponse response, Object data) throws IOException {
        response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
        String value = JsonMapper.toJsonNotNull(data);
        response.setCharacterEncoding(UTF_8);
        response.getWriter().write(value);

    }
}