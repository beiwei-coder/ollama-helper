package com.sonyan.ollamahelper.filter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 语义文档切分器（Semantic Document Splitter）
 *
 * 该类实现基于语义相似度的动态文本切分，而非传统的固定长度或段落切分。
 * 核心思想：逐句处理文本，计算相邻句子的 embedding 相似度；
 * 若相似度低于阈值，且当前块已达到最小长度，则在此处切分。
 *
 * 适用于 RAG 场景中提升检索相关性的预处理步骤。
 */
public class SemanticDocumentSplitter implements DocumentSplitter {

    private final EmbeddingModel embeddingModel;
    private final double similarityThreshold;
    private final int minChunkSizeInChars;
    private final int maxChunkSizeInChars;
    private final Pattern sentencePattern;

    /**
     * 默认构造函数（推荐用于 Spring 注入）
     */
    public SemanticDocumentSplitter(EmbeddingModel embeddingModel) {
        this(embeddingModel, 0.75, 100, 300);
    }

    /**
     * 全参构造函数
     *
     * @param embeddingModel       嵌入模型（不可为 null）
     * @param similarityThreshold  语义相似度阈值 [0.0, 1.0]，低于则切分
     * @param minChunkSizeInChars  最小块字符数（防止碎片）
     * @param maxChunkSizeInChars  最大块字符数（防止单块过大）
     */
    public SemanticDocumentSplitter(
            EmbeddingModel embeddingModel,
            double similarityThreshold,
            int minChunkSizeInChars,
            int maxChunkSizeInChars) {

        if (embeddingModel == null) {
            throw new IllegalArgumentException("embeddingModel must not be null");
        }
        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("similarityThreshold must be between 0.0 and 1.0");
        }
        if (minChunkSizeInChars < 0) {
            throw new IllegalArgumentException("minChunkSizeInChars must be >= 0");
        }
        if (maxChunkSizeInChars <= minChunkSizeInChars) {
            throw new IllegalArgumentException("maxChunkSizeInChars must be > minChunkSizeInChars");
        }

        this.embeddingModel = embeddingModel;
        this.similarityThreshold = similarityThreshold;
        this.minChunkSizeInChars = minChunkSizeInChars;
        this.maxChunkSizeInChars = maxChunkSizeInChars;
        this.sentencePattern = Pattern.compile("(?<=[。！？.!?])\\s*");
    }

    @Override
    public List<TextSegment> split(Document document) {
        String text = document.text();
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // === 1. 按句子切分 ===
        String[] rawSentences = sentencePattern.split(text);
        List<String> sentences = new ArrayList<>(rawSentences.length);
        for (String s : rawSentences) {
            s = s.trim();
            if (!s.isEmpty()) {
                sentences.add(s);
            }
        }

        if (sentences.isEmpty()) {
            return Collections.singletonList(TextSegment.from(text, document.metadata()));
        }

        // === 2. 将句子转为 TextSegment 列表（用于批量 embedding）===
        List<TextSegment> sentenceSegments = new ArrayList<>(sentences.size());
        for (String sentence : sentences) {
            // 元数据可为空，因为只取 .text()
            sentenceSegments.add(TextSegment.from(sentence));
        }

        // ✅ 正确调用：传入 List<TextSegment>
        List<Embedding> embeddings = embeddingModel.embedAll(sentenceSegments).content();
        System.out.println("Embedding count: " + embeddings.size());
        System.out.println("First vector length: " + embeddings.get(0).vector().length);

        // === 3. 动态合并句子成块 ===
        List<TextSegment> segments = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int currentChunkCharCount = 0;

        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            int sentenceLength = sentence.length();

            boolean shouldSplit = false;

            // 语义边界判断（从第二句开始）
            if (i > 0) {
                double similarity = cosineSimilarity(
                        embeddings.get(i - 1).vector(),
                        embeddings.get(i).vector()
                );
                if (similarity < similarityThreshold && currentChunkCharCount >= minChunkSizeInChars) {
                    shouldSplit = true;
                }
            }

            // 长度超限判断
            int addedLength = sentenceLength + (currentChunkCharCount > 0 ? 1 : 0); // +1 为前面空格
            boolean wouldExceedMax = (currentChunkCharCount + addedLength) > maxChunkSizeInChars;
            if (wouldExceedMax && currentChunkCharCount > 0) {
                shouldSplit = true;
            }

            if (shouldSplit) {
                segments.add(TextSegment.from(currentChunk.toString().trim(), document.metadata()));
                currentChunk = new StringBuilder();
                currentChunkCharCount = 0;
            }

            // 追加句子
            if (currentChunkCharCount > 0) {
                currentChunk.append(" ");
                currentChunkCharCount++;
            }
            currentChunk.append(sentence);
            currentChunkCharCount += sentenceLength;
        }

        if (currentChunkCharCount > 0) {
            segments.add(TextSegment.from(currentChunk.toString().trim(), document.metadata()));
        }

        return segments;
    }
    /**
     * 计算两个 float 向量的余弦相似度
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}