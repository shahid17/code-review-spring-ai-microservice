package com.example.demo;

import lombok.Data;

@Data
public class CodeReviewRequest {
    private String filename;
    private String code;
    private String language;

    public CodeReviewRequest(String language, String filename, String code) {
        this.language = language;
        this.filename = filename;
        this.code = code;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}