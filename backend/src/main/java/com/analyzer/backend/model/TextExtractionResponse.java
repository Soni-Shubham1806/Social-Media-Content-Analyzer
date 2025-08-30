package com.analyzer.backend.model;

public class TextExtractionResponse {
    private String sourceType;  // "PDF" or "IMAGE"
    private String text;
    private Integer pageCount;  // for PDFs; null for images
    private long durationMs;

    public TextExtractionResponse() {}

    public TextExtractionResponse(String sourceType, String text, Integer pageCount, long durationMs) {
        this.sourceType = sourceType;
        this.text = text;
        this.pageCount = pageCount;
        this.durationMs = durationMs;
    }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}
