package com.sonyan.ollamahelper.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author sonyan
 * @version TODO
 * @description
 * @date 28/12/2025 00:23
 */
//@AiService
public interface AiCoderHelperService {

    @SystemMessage(fromResource = "SYSTEM_MESSAGE.txt")
    String chat(String userMessage);

    @SystemMessage(fromResource = "SYSTEM_MESSAGE.txt")
    Report chatForReport(String userMessage);

    // 学习报告
    record Report(String name, List<String> suggestionList) {
    }

    @SystemMessage(fromResource = "SYSTEM_MESSAGE.txt")
    Result<String> chatWithRag(String userMessage);

    @SystemMessage(fromResource = "SYSTEM_MESSAGE.txt")
        // 流式对话
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}