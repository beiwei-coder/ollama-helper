package com.sonyan.ollamahelper;

import com.sonyan.ollamahelper.service.AiCoderHelperService;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class OllamaHelperApplicationTests {

    @Autowired
    private AiCoderHelperService aiCoderHelperService;

    @Test
    void contextLoads() {
//        String hello = ollamaAssistant.chat("hello");
//        System.out.println(hello);

        String text = "如何使用LangChain4j？";
        Result<String> result = aiCoderHelperService.chatWithRag(text);
        String content = result.content();
        List<Content> sources = result.sources();
        System.out.println(content);
        System.out.println(sources);
    }




}
