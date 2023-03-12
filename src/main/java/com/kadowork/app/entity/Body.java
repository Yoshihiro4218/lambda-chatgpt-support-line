package com.kadowork.app.entity;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Body {
    private Event[] events;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Event {
        private String replyToken;
        private String type;
        private Long timestamp;
        private Source source;
        private Message message;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Source {
            private String type;
            private String userId;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Message {
            private String id;
            private String type;
            private String text;
        }
    }
}
