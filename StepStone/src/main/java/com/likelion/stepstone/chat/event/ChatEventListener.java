package com.likelion.stepstone.chat.event;

import com.likelion.stepstone.chat.model.ChatDto;
import com.likelion.stepstone.chat.model.ChatEntity;
import com.likelion.stepstone.chatroom.ChatRoomRepository;
import com.likelion.stepstone.chatroom.exception.DataNotFoundException;
import com.likelion.stepstone.chatroom.model.ChatRoomEntity;
import com.likelion.stepstone.notification.handler.ChatNotificationHandler;
import com.likelion.stepstone.notification.model.*;
import com.likelion.stepstone.notification.repository.ChatNotificationRepository;
import com.likelion.stepstone.notification.repository.NotificationRepository;
import com.likelion.stepstone.user.model.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Async
@Transactional
@Component
@RequiredArgsConstructor
public class ChatEventListener {
    private final ChatNotificationRepository chatNotificationRepository;
    private final ChatNotificationHandler chatNotificationHandler;
    @EventListener
    public void handleChatSendEvent(ChatSendEvent chatSendEvent){ // EventPublisher를 통해 이벤트가 발생될 때 전달한 파라미터가 StudyCreatedEvent일 때 해당 메서드가 호출됩니다.

        ChatRoomEntity chatRoomEntity = chatSendEvent.getChatEntity().getChatRoomEntity();

        log.info(chatRoomEntity.getRoomName() + ": new message arrived");

        ChatNotificationEntity chatNotificationEntity = createNotification(chatRoomEntity , chatSendEvent.getUserEntity());
        // TODO DB에 Notification 정보 저장
        chatNotificationHandler.send(chatSendEvent.getUserEntity().getUserId(), ChatNotificationDto.toDto(saveNotification(chatNotificationEntity)));
    }

    private ChatNotificationEntity createNotification(ChatRoomEntity chatRoomEntity, UserEntity userEntity){

        ChatNotificationEntity chatNotificationEntity = ChatNotificationEntity.builder()
                .title("새로운 채팅")
                .message(chatRoomEntity.getRoomName() + "채팅방에 새로운 채팅이 도착했습니다.")
                .checked(false)
                .notificationType(NotificationType.CHAT_SEND)
                .userEntity(userEntity)
                .publisher(userEntity)
                .chatRoomEntity(chatRoomEntity)
                .build();

        return chatNotificationEntity;
    }

    private ChatNotificationEntity saveNotification(ChatNotificationEntity chatNotificationEntity){

        return chatNotificationRepository.save(chatNotificationEntity);
    }
}
