package com.sonyan.ollamahelper.controller;

import com.sonyan.ollamahelper.service.AiCoderHelperService;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AiController
 *
 * @author smh
 * @version TODO
 * @description
 * @date 02/10/2025 01:06
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiCoderHelperService aiCoderHelperService;


    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(int memoryId, String message) {
        return aiCoderHelperService.chatStream(memoryId, message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .id(memoryId + "")
                        .event("message")
                        .build());
    }

    @GetMapping("/chat2")
    public String chat(String message) {
        Result<String> result = aiCoderHelperService.chatWithRag(message);
        List<Content> sources = result.sources();
        System.out.println(sources);
        return result.content();
    }
}