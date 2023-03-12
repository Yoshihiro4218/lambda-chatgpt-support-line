package com.kadowork.app;

import lombok.*;

import java.util.*;

@Data
public class Output {
    private String replyToken;
    private List<Messages> messages = new ArrayList<>();
}
