package com.mrokga.carrot_server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "자주 쓰는 문구 추가 결과 응답 DTO")
public class QuickReplyAddResponseDto {
    @Schema(
            description = "결과 상태",
            example = "CREATED",
            allowableValues = {"CREATED", "ALREADY_EXISTS"}
    )
    private String status;

    @Schema(description = "메세지 ID(이미 존재했을 경우 해당 ID 반환)", example = "123")
    private Integer id;

    /** 팩토리 메서드: 생성됨 */
    public static QuickReplyAddResponseDto created(Integer id) {
        return QuickReplyAddResponseDto.builder()
                .status("CREATED")
                .id(id)
                .build();
    }

    /** 팩토리 메서드: 이미 존재 */
    public static QuickReplyAddResponseDto alreadyExists(Integer id) {
        return QuickReplyAddResponseDto.builder()
                .status("ALREADY_EXISTS")
                .id(id)
                .build();
    }
}
