package com.example.demo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class GitReviewController {

    private final GitService gitService;
    private final CodeReviewService codeReviewService;

    public GitReviewController(GitService gitService, CodeReviewService codeReviewService) {
        this.gitService = gitService;
        this.codeReviewService = codeReviewService;
    }

    @PostMapping("/git")
    public List<CodeReviewResult> reviewRepository(@RequestBody GitReviewRequest request) throws Exception {
        System.out.println("🔄 Starting code review for repo: " + request.getRepoUrl());

        // 1️⃣ Clone repo and get all Java files
        List<Path> allJavaFiles = gitService.cloneAndGetJavaFiles(request.getRepoUrl(), request.getBranch());

        // 2️⃣ Perform AI code review on all Java files
        List<CodeReviewResult> results = codeReviewService.reviewRepositoryFiles(allJavaFiles);

        System.out.println("✅ Code review completed for all files!");

        return results;
    }
}

