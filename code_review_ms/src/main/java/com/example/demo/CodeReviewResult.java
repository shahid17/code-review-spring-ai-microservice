package com.example.demo;

import lombok.Data;

@Data
public class CodeReviewResult {
    private String filename;
    private CodeReviewResponse review;

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public CodeReviewResponse getReview() {
        return review;
    }

    public void setReview(CodeReviewResponse review) {
        this.review = review;
    }
}

