package com.kadowork.app;

import com.amazonaws.services.lambda.runtime.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.*;
import com.google.gson.*;
import com.kadowork.app.entity.*;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.*;
import lombok.*;
import org.apache.http.impl.client.*;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.*;
import software.amazon.awssdk.enhanced.dynamodb.*;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo;

public class ChatGptSupportLine implements RequestHandler<Map<String, Object>, Object> {
    private final RestTemplate restTemplate = restTemplate();

    private static final String LINE_ACCESS_TOKEN = System.getenv("LINE_ACCESS_TOKEN");
    private static final String OPENAI_GPT3_TOKEN = System.getenv("OPENAI_GPT3_TOKEN");
    private static final String LINE_REPLY_POST_URL = "https://api.line.me/v2/bot/message/reply";

    @Override
    public Object handleRequest(Map<String, Object> map, Context context) {
        // test
        System.out.println("I am Lambda!");
        map.forEach((key, value) -> System.out.println(key + ":" + value));

        // 暫定対応
        ObjectMapper mapper = new ObjectMapper();
        try {
            Body body = mapper.readValue(map.get("body").toString(), Body.class);
            System.out.println(body);
            Output output = new Output();
            output.setReplyToken(body.getEvents()[0].getReplyToken());
            Output.Messages outMessage = new Output.Messages();
            outMessage.setType("text");
            String message = body.getEvents()[0].getMessage().getText();

            outMessage.setText(chatOpenAI(message));
            output.getMessages().add(outMessage);
            System.out.println("Output 作成！");
            System.out.println(output);

            Gson gson = new Gson();
            context.getLogger().log(gson.toJson(output));
            ResponseEntity<String> response = postLineNotify(gson.toJson(output));
            System.out.println(response.getBody());

            return null;
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("処理終了〜");
        return null;
    }

    private String chatOpenAI(String message) {
        final var service = new OpenAiService(OPENAI_GPT3_TOKEN, Duration.ofSeconds(150000L));
        System.out.println("\nCreating completion...");
        final var chatMessages = List.of(
                new ChatMessage("system", "秘書のような口調で会話してください。性別は女性です。"),
                new ChatMessage("user", message));
        final var chatCompletionRequest = ChatCompletionRequest.builder()
                                                               .model("gpt-3.5-turbo")
                                                               .maxTokens(1024)
                                                               .messages(chatMessages)
                                                               .build();
        var result = service.createChatCompletion(chatCompletionRequest);
        for (final ChatCompletionChoice choice : result.getChoices()) {
            System.out.println(choice);
        }
        return result.getChoices().get(0).getMessage().getContent();
    }

    private ResponseEntity<String> postLineNotify(String bodyJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + LINE_ACCESS_TOKEN);
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(bodyJson, headers);
        return restTemplate.exchange(LINE_REPLY_POST_URL, POST, httpEntity, String.class);
    }

    private RestTemplate restTemplate() {
        var clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
        // 接続が確立するまでのタイムアウト
        clientHttpRequestFactory.setConnectTimeout(5000);
        // コネクションマネージャーからの接続要求のタイムアウト
        clientHttpRequestFactory.setConnectionRequestTimeout(5000);
        // ソケットのタイムアウト（パケット間のタイムアウト）
        clientHttpRequestFactory.setReadTimeout(5000);
        return new RestTemplate(clientHttpRequestFactory);
    }
}