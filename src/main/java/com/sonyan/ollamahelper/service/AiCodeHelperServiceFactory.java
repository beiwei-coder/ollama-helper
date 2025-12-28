package com.sonyan.ollamahelper.service;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeHelperServiceFactory {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Autowired
    private OllamaStreamingChatModel ollamaStreamingChatModel;

    @Autowired
    private ContentRetriever contentRetriever;


    @Bean
    public AiCoderHelperService aiCoderHelperService() {
        // 会话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // 构造 AI Service
        AiCoderHelperService aiCodeHelperService = AiServices.builder(AiCoderHelperService.class)
                .chatLanguageModel(ollamaChatModel)
                .streamingChatLanguageModel(ollamaStreamingChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.withMaxMessages(10)) // 每个会话独立存储
                .contentRetriever(contentRetriever) // RAG 检索增强生成
                .build();
        return aiCodeHelperService;
    }
}
