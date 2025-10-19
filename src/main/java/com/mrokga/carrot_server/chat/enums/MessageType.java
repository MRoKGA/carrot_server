package com.mrokga.carrot_server.chat.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    TEXT("텍스트"),
    IMAGE("이미지"),
    APPOINTMENT("약속");

    private final String type;

}
