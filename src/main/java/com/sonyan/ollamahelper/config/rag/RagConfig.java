package com.sonyan.ollamahelper.config.rag;

import com.sonyan.ollamahelper.filter.SemanticDocumentSplitter;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 加载 RAG
 */
@Configuration
public class RagConfig {

    @Resource
    private EmbeddingModel ollamaEmbeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Bean
    public ContentRetriever contentRetriever() {
        // 1. 加载文档
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");

        // 2. ✅ 使用语义分割器（关键修改）
        SemanticDocumentSplitter semanticSplitter = new SemanticDocumentSplitter(
                ollamaEmbeddingModel,
                0.75,   // 相似度阈值
                100,
                300// 最小块字符数
        );

        // 3. 构建 ingestor
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(semanticSplitter)
                .textSegmentTransformer(textSegment -> TextSegment.from(
                        textSegment.metadata().getString("file_name") + "\n" + textSegment.text(),
                        textSegment.metadata()
                ))
                .embeddingModel(ollamaEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // 4. 执行文档摄入
        ingestor.ingest(documents);

        // 5. 构建检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(ollamaEmbeddingModel)
                .maxResults(5)
                .minScore(0.75)
                .build();

        return contentRetriever;
    }
}
