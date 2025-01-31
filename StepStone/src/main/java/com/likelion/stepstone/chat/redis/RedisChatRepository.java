package com.likelion.stepstone.chat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.stepstone.chat.ChatRepository;
import com.likelion.stepstone.chat.model.ChatDto;
import com.likelion.stepstone.chat.model.ChatEntity;
import com.likelion.stepstone.chat.model.RedisChatEntity;
import com.likelion.stepstone.chatroom.ChatRoomJoinRepository;
import com.likelion.stepstone.chatroom.ChatRoomRepository;
import com.likelion.stepstone.chatroom.exception.DataNotFoundException;
import com.likelion.stepstone.chatroom.model.ChatRoomEntity;
import com.likelion.stepstone.chatroom.model.ChatRoomUserJoinEntity;
import com.likelion.stepstone.config.CacheNames;
import com.likelion.stepstone.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RedisChatRepository {
    // Redis CacheKeys
    private static final String CHAT_ROOMS = "CHAT_ROOM"; // 채팅룸 저장
    private static final String NEW_CHAT = "CHAT"; //새로운 채팅
    public static final String USER_COUNT = "USER_COUNT"; // 채팅룸에 입장한 클라이언트수 저장
    public static final String ENTER_INFO = "ENTER_INFO"; // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장
    private static final int CHAT_SIZE = 1000;
    private static final String chatRoomImageUrl = "https://www.bootdey.com/app/webroot/img/Content/icons/64/PNG/64/";
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final RedisChatCrudRepository redisChatCrudRepository;

    private final ChatRoomJoinRepository chatRoomJoinRepository;
    private final RedisTemplate<String, ChatDto> chatRoomRedisTemplate;
    private final RedisTemplate<String, Integer> cutIdxRedisTemplate;
    private final RedisCacheManager cacheManager;

    public ChatDto createChat(ChatDto chatDto) {
        addChat(chatDto, chatDto.getChatRoomId());
//        ChatEntity chatEntity = fromDtoToEntity(chatDto);
//        UserEntity userEntity = userRepository.findByUserId(chatDto.getSenderId()).orElseThrow(()-> new DataNotFoundException("user not found"));
//        chatEntity.setSender(userEntity);
//        chatEntity.setChatId(UUID.randomUUID().toString());
//
//        RedisChatEntity redisChatEntity = convert2RedisChatEntity(chatEntity);
//        createRedisChat(redisChatEntity);
//        hashOpsChat.put(NEW_CHAT, chatEntity.getChatId(), redisChatEntity);
        return chatDto;
    }
    public ChatEntity fromDtoToEntity(ChatDto chatDto){
        ChatEntity chatEntity = ChatEntity.toEntity(chatDto);
        ChatRoomEntity chatRoomEntity = chatRoomRepository.findByChatRoomId(chatDto.getChatRoomId()).orElseThrow(() -> new DataNotFoundException("Chat Room Not Exist"));
        chatEntity.setChatRoomEntity(chatRoomEntity);

        return chatEntity;
    }

    public RedisChatEntity createRedisChat(RedisChatEntity redisChatEntity){
        return redisChatCrudRepository.save(redisChatEntity);
    }
    

    public String getChatRoomImageUrl(){
        List<String> imageNames = getListFromRes();

        Random rand = new Random();
        String randomElement = imageNames.get(rand.nextInt(imageNames.size()));
        String imageUrl = chatRoomImageUrl + randomElement;

        return imageUrl;
    }

    public List<String> getListFromRes(){
        String content = "";
        ClassPathResource cpr = new ClassPathResource("static/chatRoomImageUrls.txt");

        try {
            byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
            content = new String(bdata, StandardCharsets.UTF_8);

            String[] urls = content.split(",");

            return Arrays.stream(urls).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RedisChatEntity convert2RedisChatEntity(ChatEntity chatEntity){
        return RedisChatEntity.builder()
                .chatId(chatEntity.getChatId())
                .message(chatEntity.getMessage())
                .chatRoomId(chatEntity.getChatRoomEntity().getChatRoomId())
                .sender(chatEntity.getSender())
                .createdAt(chatEntity.getCreatedAt())
                .build();
    }


    public void addChat(ChatDto chatDto, String chatRoomId){
        String key = ChatRoomCacheKeyGenerator.generateChatRoomKey(chatRoomId);
//        List<ChatDto> chats = findByChatRoomId(chatRoomId);
//        chats.add(chatDto);
        chatRoomRedisTemplate.opsForList().rightPush(key, chatDto);
    }
    @Cacheable(keyGenerator = "chatRoomCacheKeyGenerator", value = CacheNames.CHAT_ROOM)
    public List<ChatDto> findByChatRoomId(String chatRoomId){
        String key = ChatRoomCacheKeyGenerator.generateChatRoomKey(chatRoomId);
        Long len = chatRoomRedisTemplate.opsForList().size(key);
        return len == 0 ? new ArrayList<>() : chatRoomRedisTemplate.opsForList().range(key, 0, len-1);
    }

    @CachePut(keyGenerator = "chatRoomCacheKeyGenerator", value = CacheNames.CHAT_ROOM)
    public List<ChatDto> findPartByChatRoomId(String chatRoomId, int idx){
        String key = ChatRoomCacheKeyGenerator.generateChatRoomKey(chatRoomId);
//        Long len = chatRoomRedisTemplate.opsForList().size(key);
        Long len = chatRepository.count();
        Long roomCid = chatRoomRepository.findByChatRoomId(chatRoomId).get().getChatRoomCid();
        List<ChatEntity> entities = chatRepository.findRecentChats(roomCid, idx * CHAT_SIZE, (idx + 1) * CHAT_SIZE);
//        List<ChatDto> dtos = entities.stream().map(ChatDto::toDto).toList();
        List<ChatRoomUserJoinEntity> chatRoomUserJoinEntities = chatRoomJoinRepository.findByIdChatRoomCid(roomCid);
        List<ChatDto> dtos = entities.stream()
                .map(ChatDto::toDto)
                .peek(chatDto -> {
                    for (ChatRoomUserJoinEntity chatRoomUserJoinEntity : chatRoomUserJoinEntities){
                        if(Objects.equals(chatDto.getSenderId(), chatRoomUserJoinEntity.getUserEntity().getUserId()))
                            chatDto.setProfileImageUrl(chatRoomUserJoinEntity.getProfileImageUrl());
                    }
                })
                .collect(Collectors.toList());
        if (dtos.isEmpty()) return new ArrayList<>();
        Cache cache = cacheManager.getCache(key);

        if(cacheManager.getCache(key) == null){
            return dtos;
        }

//        List<ChatDto> cacheChats =  chatRoomRedisTemplate.opsForList().range(key, 0 ,len);
        String jsonChats = String.valueOf(chatRoomRedisTemplate.opsForValue().get(key));
        if (jsonChats.equals("null")) return dtos;
        ObjectMapper mapper = new ObjectMapper();

        List<ChatDto> resultChats = new ArrayList<>();
        try {
            List<ChatDto> cacheChats = Arrays.asList(mapper.readValue(jsonChats, ChatDto[].class));
            resultChats.addAll(cacheChats);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        resultChats.addAll(dtos);

//        chatRoomRedisTemplate.opsForList().rightPushAll(key, dtos);
//        assert cacheChats != null;
        cacheManager.getCache(CacheNames.CHAT_ROOM).clear();

        return resultChats;
//        int idx = getCutIdx(chatRoomId);
//        int pvt = idx * CHAT_SIZE;
//        return chatRoomRedisTemplate.opsForList().range(key, 0 ,len);
//        return null;
    }

    public void saveAll(List<ChatDto> items, String chatRoomId){
        RedisSerializer keySerializer = chatRoomRedisTemplate.getStringSerializer();
        RedisSerializer valueSerializer = chatRoomRedisTemplate.getValueSerializer();

        chatRoomRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            items.forEach(chatDto -> {
                String key = ChatRoomCacheKeyGenerator.generateChatRoomKey(chatRoomId);
                connection.listCommands().rPush(keySerializer.serialize(key),
                        valueSerializer.serialize(chatDto));
            });
            return null;
        });
    }
    @Cacheable(value = CacheNames.CHAT_ROOM)
    public List<ChatDto> readAll(String chatRoomId){
        String key = ChatRoomCacheKeyGenerator.generateChatRoomKey(chatRoomId);
        Long len = chatRoomRedisTemplate.opsForList().size(key);
        RedisSerializer keySerializer = chatRoomRedisTemplate.getStringSerializer();
        List<Object> results = chatRoomRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for(int i = 0; i < len; i++) {
                connection.listCommands().rPop(keySerializer.serialize(key));
            }
            return null;
        });

        return results.stream().map(object -> (ChatDto) object).collect(Collectors.toList());
    }

    @CachePut(keyGenerator = "cutIdxCacheKeyGenerator", value = CacheNames.CUT_IDX)
    public Integer updateCutIdx(String roomId){
        String key = CutIdxCacheKeyGenerator.generateCutIdxKey(roomId);
        Integer idx = 0;

        if (cutIdxRedisTemplate.opsForValue().get(key) != null)
            idx = cutIdxRedisTemplate.opsForValue().get(key);

//        cutIdxRedisTemplate.opsForValue().set(key, ++idx);
        return idx + 1;
    }

    @Cacheable(keyGenerator = "cutIdxCacheKeyGenerator", value = CacheNames.CUT_IDX)
    public Integer getCutIdx(String roomId){
        String key = CutIdxCacheKeyGenerator.generateCutIdxKey(roomId);
        Integer idx = 1;

        if (cutIdxRedisTemplate.opsForValue().get(key) != null)
            idx = cutIdxRedisTemplate.opsForValue().get(key);

//        cutIdxRedisTemplate.opsForValue().set(key, ++idx);
        return idx;
    }

//    @CacheEvict(value=CacheNames.CUT_IDX, allEntries=true)
    public void evictIdxCache() {
        System.out.println("Evicting all indexes from redis Cache.");
    }
}