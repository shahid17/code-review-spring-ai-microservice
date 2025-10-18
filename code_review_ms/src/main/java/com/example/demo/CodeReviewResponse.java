package com.example.demo;

import lombok.Data;

import java.util.List;

@Data
public class CodeReviewResponse {
    private List<LineIssue> issues; // List of line-level issues

    public List<LineIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<LineIssue> issues) {
        this.issues = issues;
    }
}