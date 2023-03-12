package com.kadowork.app;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Events {
    private String replyToken;
    private String type;
    private Long timestamp;
    private Source source;
    private Message message;
}