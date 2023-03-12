package com.kadowork.app;

import com.amazonaws.services.lambda.runtime.*;
import com.google.gson.*;
import org.springframework.http.*;
import org.springframework.web.client.*;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class ChatGptSupportLine implements RequestHandler<Input, Object> {
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String LINE_ACCESS_TOKEN = System.getenv("LINE_ACCESS_TOKEN");
    private static final String LINE_REPLY_POST_URL = "https://api.line.me/v2/bot/message/reply";

    @Override
    public Object handleRequest(Input input, Context context) {
        System.out.println(input.getEvents()[0].getReplyToken());
        System.out.println(input.getEvents()[0].getMessage().getText());

        Output output = new Output();
        output.setReplyToken(input.getEvents()[0].getReplyToken());
        Messages outMessage = new Messages();
        outMessage.setType("text");
        outMessage.setText(input.getEvents()[0].getMessage().getText() + " ...を受け取りました！");
        output.getMessages().add(outMessage);

        Gson gson = new Gson();
        context.getLogger().log(gson.toJson(output));
        ResponseEntity<String> response = postLineNotify(gson.toJson(output));
        System.out.println(response.getBody());

        return null;
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