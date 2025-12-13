package com.yulink.texas.common.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JacksonMapper {

    private static final ObjectMapper objectMapper = ObjectMapperFactory.objectMapper();

    private JacksonMapper() {
    }

    /**
     * Object => String
     */
    public static <T> String obj2String(T src) {
        if (src == null) {
            return null;
        }

        try {
            return src instanceof String ? (String) src : objectMapper.writeValueAsString(src);
        } catch (Exception e) {
            log.debug("parse Object to String error", e);
            return null;
        }
    }

    public static <T> String obj2JsonP(String callback, T src) {
        if (src == null) {
            return null;
        }

        if (callback == null) {
            return obj2String(src);
        } else {
            return obj2String(new JSONPObject(callback, src));
        }
    }

    /**
     * Object => byte[]
     */
    public static <T> byte[] obj2Byte(T src) {
        if (src == null) {
            return new byte[0];
        }

        try {
            return src instanceof byte[] ? (byte[]) src : objectMapper.writeValueAsBytes(src);
        } catch (Exception e) {
            log.debug("parse Object to byte[] error", e);
            return new byte[0];
        }
    }

    /**
     * String => Object
     */
    @SuppressWarnings("unchecked")
    public static <T> T string2Obj(String str, Class<T> clazz) {
        if (str == null || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (Exception e) {
            log.debug("parse String to Object error, String:{}, Class<T>:{}, error:{}", str, clazz.getName(), e);
            return null;
        }
    }

    /**
     * byte[] => Object
     */
    public static <T> T byte2Obj(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(byte[].class) ? (T) bytes : objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            log.debug("parse byte[] to Object error, byte[]:{}, Class<T>:{}, error:{}", bytes, clazz.getName(), e);
            return null;
        }
    }

    public static <T> T readValue(String json, com.fasterxml.jackson.core.type.TypeReference<T> typeReference) {
        if (json == null || json.length() == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.warn("parse json to obj failed, json:{},ex:{}", json, e.getMessage(), e);
            return null;
        }
    }

    public static <T> T readValue(String jsonStr, Class<T> clazz) {
        return readValue(jsonStr, clazz, "");
    }

    public static <T> T readValue(String json, Class<T> clazz, String additionalReadErrLog) {
        if (json == null || json.length() == 0) {
            return null;
        }
        T t = null;
        try {
            t = objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.warn("{}, {}", e.getMessage(), additionalReadErrLog, e);
        }
        return t;
    }


}
