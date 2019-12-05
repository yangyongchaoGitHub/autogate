package com.dataexpo.autogate.comm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    private final String TAG = JsonUtil.class.getSimpleName();
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private static class HolderClass {
        private static final JsonUtil instance = new JsonUtil();
    }

    /**
     * 单例模式
     */
    public static JsonUtil getInstance() {
        return JsonUtil.HolderClass.instance;
    }

    public String obj2json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    public <T> T json2obj(String jsonStr, Class<T> clazz)
            throws Exception {
        return objectMapper.readValue(jsonStr, clazz);
    }

    public <T> Map<String, Object> json2map(String jsonStr)
            throws Exception {
        return objectMapper.readValue(jsonStr, Map.class);
    }

    public <T> Map<String, T> json2map(String jsonStr, Class<T> clazz)
            throws Exception {
        Map<String, Map<String, Object>> map = objectMapper.readValue(jsonStr,
                new TypeReference<Map<String, T>>() {
                });
        Map<String, T> result = new HashMap<String, T>();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            result.put(entry.getKey(), map2obj(entry.getValue(), clazz));
        }
        return result;
    }

    public <T> List<T> json2list(String jsonArrayStr, Class<T> clazz)
            throws Exception {
        List<Map<String, Object>> list = objectMapper.readValue(jsonArrayStr,
                new TypeReference<List<T>>() {
                });
        List<T> result = new ArrayList<T>();
        for (Map<String, Object> map : list) {
            result.add(map2obj(map, clazz));
        }
        return result;
    }

    public <T> T map2obj(Map map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);
    }
}
