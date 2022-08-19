package com.likelion.stepstone.like.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LikeDto {

    /**
     * 데이터 전달 모델 클래스
     * Data Transfer Object
     */

    private Long likeId;

    @Setter
    private Long userCid;

    @Setter
    private Long postCid;

    @Setter
    private LocalDateTime createdAt;

    //toDto로 이름 변경
    public static LikeDto toDto(LikeEntity entity) {
        LikeDto dto = LikeDto.builder()
                .likeId(entity.getLikeId())
                .userCid(entity.getUserCid())
                .postCid(entity.getPostCid())
                .createdAt(entity.getCreatedAt())
                .build();

        return dto;
    }
}
