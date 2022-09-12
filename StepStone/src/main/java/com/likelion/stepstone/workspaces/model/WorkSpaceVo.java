package com.likelion.stepstone.workspaces.model;

import com.likelion.stepstone.user.model.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@ToString
public class WorkSpaceVo {

    /**
     * Vo 모델 클래스는 변경 하지 않는다.
     * Value Object
     */



    private UUID workspaceId;

    @Setter
    private String title;

    @Setter
    private String body;

    private final UserEntity user;


    @Setter
    private LocalDateTime createdAt;

    @Setter
    private LocalDateTime updatedAt;


    public static WorkSpaceDto toVo(WorkSpaceDto dto) {
        WorkSpaceVo vo = WorkSpaceVo.builder()
                .workspaceId(dto.getWorkspaceId())
                .title(dto.getTitle())
                .body(dto.getBody())
//                .userCid(dto.getUserCid())
                .user(dto.getUser())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();

        return dto;
    }
}