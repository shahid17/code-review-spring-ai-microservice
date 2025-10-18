package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class CodeReviewService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newFixedThreadPool(5); // limit concurrency

    public CodeReviewService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public List<CodeReviewResult> reviewRepositoryFiles(List<Path> javaFiles) {
        List<Future<CodeReviewResult>> futures = new ArrayList<>();

        for (Path file : javaFiles) {
            futures.add(executor.submit(() -> reviewSingleFile(file)));
        }

        return futures.stream()
                .map(future -> {
                    try {
                        return future.get(2, TimeUnit.MINUTES); // 2 min per file
                    } catch (TimeoutException e) {
                        System.err.println("Timeout reviewing file: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error reviewing file: " + e.getMessage());
                    }
                    return null;
                })
                .filter(result -> result != null)
                .collect(Collectors.toList());
    }

    private CodeReviewResult reviewSingleFile(Path file) {
        try {
            String code = Files.readString(file);

            CodeReviewRequest req = new CodeReviewRequest("Java", file.getFileName().toString(), code);
            CodeReviewResponse review = reviewCode(req);

            CodeReviewResult result = new CodeReviewResult();
            result.setFilename(file.getFileName().toString());
            result.setReview(review);
            return result;

        } catch (IOException e) {
            CodeReviewResult errorResult = new CodeReviewResult();
            errorResult.setFilename(file.getFileName().toString());
            CodeReviewResponse error = new CodeReviewResponse();
            List<LineIssue> lineIssueList = new ArrayList<>();
            LineIssue lineIssue = new LineIssue();
            lineIssue.setIssue("Error reading file: " + e.getMessage());
            lineIssueList.add(lineIssue);
            error.setIssues(lineIssueList);
            errorResult.setReview(error);
            return errorResult;
        }
    }

    private CodeReviewResponse reviewCode(CodeReviewRequest req) {
        String promptText = buildPrompt(req);

        UserMessage userMessage = new UserMessage(promptText); // wrap prompt

        String aiResponse;
        try {
            aiResponse = chatModel.call(userMessage);
        } catch (Exception e) {
            CodeReviewResponse error = new CodeReviewResponse();
            List<LineIssue> lineIssueList = new ArrayList<>();
            LineIssue lineIssue = new LineIssue();
            lineIssue.setIssue("AI request failed: " + e.getMessage());
            lineIssueList.add(lineIssue);
            error.setIssues(lineIssueList);
            return error;
        }

        try {
            // Attempt to parse structured JSON from AI
            return objectMapper.readValue(aiResponse, CodeReviewResponse.class);
        } catch (Exception e) {
            // Fallback if AI output is plain text
            CodeReviewResponse fallback = new CodeReviewResponse();
            List<LineIssue> lineIssueList = new ArrayList<>();
            LineIssue lineIssue = new LineIssue();
            lineIssue.setIssue(aiResponse);
            lineIssueList.add(lineIssue);
            fallback.setIssues(lineIssueList);
            return fallback;
        }
    }

    private String buildPrompt(CodeReviewRequest req) {

        String[] lines = req.getCode().split("\n");
        StringBuilder numberedCode = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            numberedCode.append(i + 1).append(": ").append(lines[i]).append("\n");
        }

        return """
            You are a senior Java code reviewer.
            Review the following Java code carefully.

            1. Analyze the code line by line.
            2. Identify readability issues, security vulnerabilities, performance problems, or best practice violations.
            3. Return your response in strict JSON format ONLY.
            4. For each issue, include:
               - "line": line number in the file
               - "issue": description of the problem
               - "suggestion": how to fix it in detail
               - "solution": fixed code

            5. JSON structure example:
            {
              "issues": [
                {
                  "line": 12,
                  "issue": "Variable name is not meaningful",
                  "suggestion": "Rename variable 'x' to 'userCount'"
                  "solution": "int userCount = 2",
                },
                {
                  "line": 25,
                  "issue": "Missing access modifier for class variables",
                  "suggestion": "Add access modifiers (e.g., 'private') to the 'chatModel', 'chatClient', and 'aiConfig' variables to encapsulate the class's state and improve maintainability.",
                  "solution": "private OpenAiChatModel chatModel;",
                }
              ]
            }
            
            6. Do NOT wrap your response inside another JSON or Markdown code block.
                ❌ Incorrect:
                {
                  "issues": [
                    {
                      "line": 0,
                      "issue": "```json { \\"issues\\": [ ... ] } ```"
                    }
                  ]
                }
        
                ✅ Correct:
                {
                  "issues": [
                    {
                      "line": 12,
                      "issue": "Variable name not meaningful",
                      "suggestion": "Rename variable 'x' to 'userCount'",
                      "solution": "int userCount = users.size();"
                    }
                  ]
                }


            Filename: %s
            Code: %s
            """.formatted(req.getFilename(), numberedCode.toString());
    }

}
