package com.likelion.stepstone.chat.redis;

import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;

public class CutIdxCacheKeyGenerator implements KeyGenerator {
    private static final String CUT_IDX = "CUT_IDX_CACHE";

    public static String generateCutIdxKey(String chatRoomId){
        return CUT_IDX + "::" + chatRoomId;
    }


    @Override
    public Object generate(Object target, Method method, Object... params) {
        return params[0];
    }
}
