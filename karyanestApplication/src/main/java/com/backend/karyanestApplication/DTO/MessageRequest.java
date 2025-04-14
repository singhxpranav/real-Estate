package com.backend.karyanestApplication.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    private Long conversationId; // The conversation ID
    private String message;      // Message content
}
