package com.cy.ns.common.utils;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;

public class Serializer {

    // 反序列化方法
    public static <T> T deserialize(Class<T> clazz, byte[] bytes){
        return new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), clazz);
    }

    // 序列化方法
    public static <T> byte[] serialize(T object){
        return new Gson().toJson(object).getBytes(StandardCharsets.UTF_8);
    }

}