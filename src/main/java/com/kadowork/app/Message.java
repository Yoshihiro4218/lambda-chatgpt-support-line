package com.kadowork.app;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String id;
    private String type;
    private String text;
}
