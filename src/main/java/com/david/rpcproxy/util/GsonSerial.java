package com.david.rpcproxy.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.List;


/**
 * Created by fsdevops on 15-11-27.
 */
public class GsonSerial {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();


    public static <T> String entityToJson(T object) {
        return gson.toJson(object);
    }

    public static <T> T jsonToEntity(String value, Class<T> clz) {
        return gson.fromJson(value, clz);
    }

    public static <T> List<T> jsonToList(String value, Type type) {
        return gson.fromJson(value, type);
    }
}
