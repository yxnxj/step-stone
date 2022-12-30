package com.likelion.stepstone.chat;

import com.likelion.stepstone.chat.model.ChatEntity;
import com.likelion.stepstone.chatroom.model.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    List<ChatEntity> findByChatRoomEntity(ChatRoomEntity chatRoomEntity);
    @Query(nativeQuery = true, value="SELECT * FROM chats as chat WHERE chat.chatRoomEntity_chat_room_cid = :roomCid ORDER BY created_at DESC LIMIT :start OFFSET :end")
    List<ChatEntity> findRecentChats(@Param("roomCid") Long roomCid, @Param("start") Integer start, @Param("end") Integer end);

//    ChatEntity findByChatId(String chatId);
}
