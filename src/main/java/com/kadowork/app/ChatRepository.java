package com.kadowork.app;

import com.kadowork.app.entity.*;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.*;
import java.util.stream.*;

import static software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo;

@AllArgsConstructor
public class ChatRepository {
    private final DynamoDbEnhancedClient client;

    private static final String TABLE_NAME = "chatgpt-support-line--chat-history";

    public void save(Chat chat) {
        DynamoDbTable<Chat> table = client.table(TABLE_NAME, TableSchema.fromBean(Chat.class));
        table.putItem(chat);
    }

    public List<Chat> scan() {
        DynamoDbTable<Chat> table = client.table(TABLE_NAME, TableSchema.fromBean(Chat.class));
        return table.scan().items().stream().collect(Collectors.toList());
    }

    public List<Chat> scanLimit(int limit) {
        DynamoDbTable<Chat> table = client.table(TABLE_NAME, TableSchema.fromBean(Chat.class));
        return table.query(QueryEnhancedRequest.builder()
                                               .scanIndexForward(false)
                                               .limit(limit)
                                               .build())
                    .items().stream().collect(Collectors.toList());
    }

    // index で引くとき
    public <T> T getByXXX(int key, Class<T> recordClass) {
        String tableName = recordClass.getSimpleName().toLowerCase(Locale.ENGLISH);
        String indexName = recordClass.getSimpleName().toLowerCase(Locale.ENGLISH) + "_index"; // 適当
        DynamoDbTable<T> table = client.table(tableName, TableSchema.fromBean(recordClass));
        DynamoDbIndex<T> index = table.index(indexName);
        return index.query(r -> r.queryConditional(keyEqualTo(k -> k.partitionValue(key))))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("exception")) // 適当
                    .items().get(0);
    }
}
