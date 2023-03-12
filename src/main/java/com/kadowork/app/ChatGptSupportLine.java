package com.kadowork.app;

import com.amazonaws.services.lambda.runtime.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.*;
import com.google.gson.*;
import com.kadowork.app.entity.*;
import com.theokanning.openai.completion.*;
import com.theokanning.openai.service.*;
import org.springframework.http.*;
import org.springframework.web.client.*;

import java.io.*;
import java.util.*;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class ChatGptSupportLine implements RequestHandler<Map<String, Object>, Object> {
    private final RestTemplate restTemplate = new RestTemplate();

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
            outMessage.setText(chatOpenAI(body.getEvents()[0].getMessage().getText()));
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
        // 参照: https://qiita.com/blue_islands/items/dd078af9266960a777c4 様
        final var service = new OpenAiService(OPENAI_GPT3_TOKEN);

        System.out.println("\nCreating completion...");

        final var prompt = "The following is a conversation with an AI assistant. The assistant is helpful, creative, clever.\nHuman: " + message
                           + "\nAI: ";

        final var completionRequest = CompletionRequest.builder()
                                                       .model("text-davinci-003")
                                                       .prompt(prompt)
                                                       .maxTokens(256)
                                                       .build();
        final var completionResult = service.createCompletion(completionRequest);
        final var choiceList = completionResult.getChoices();

        for (final CompletionChoice choice : choiceList) {
            System.out.println(choice);
        }
        return choiceList.get(0).getText();
    }

    private ResponseEntity<String> postLineNotify(String bodyJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + LINE_ACCESS_TOKEN);
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(bodyJson, headers);
        return restTemplate.exchange(LINE_REPLY_POST_URL, POST, httpEntity, String.class);
    }

//    private PopularMovie fetchPopularMovies() {
//        URI uri = UriComponentsBuilder
//                .fromUriString(MOVIE_API_POPULAR_MOVIES_URL)
//                .queryParam("api_key", MOVIE_API_KEY)
//                .queryParam("language", "ja")
//                .queryParam("page", "1")
//                .build().encode().toUri();
//        return restTemplate.exchange(uri, HttpMethod.GET, null, PopularMovie.class).getBody();
//    }
}