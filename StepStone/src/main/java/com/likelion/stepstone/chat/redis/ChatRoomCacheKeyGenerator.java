package com.likelion.stepstone.chat.redis;

import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;

public class ChatRoomCacheKeyGenerator implements KeyGenerator {
    private static final String CHAT_ROOM_KEY = "CHAT_ROOM_CACHE";

    public static String generateChatRoomKey(Long chatRoomCid){
        return CHAT_ROOM_KEY + "::" + chatRoomCid;
    }

    public static String generateChatRoomKey(String chatRoomId){
        return CHAT_ROOM_KEY + "::" + chatRoomId;
    }
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return params[0];
    }
}
