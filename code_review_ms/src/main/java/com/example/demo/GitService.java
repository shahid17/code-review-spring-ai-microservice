package com.example.demo;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitService {

    private static final String CLONE_DIR = "/tmp/ai-code-review";

    public List<Path> cloneAndGetJavaFiles(String repoUrl, String branch) throws Exception {
        File directory = new File(CLONE_DIR);
        if (directory.exists()) {
            deleteDirectory(directory);
        }

        System.out.println("Cloning repo: " + repoUrl);
        Git.cloneRepository()
                .setURI(repoUrl)
                .setBranch(branch)
                .setDirectory(directory)
                .call();

        return Files.walk(Path.of(CLONE_DIR))
                .filter(p -> p.toString().endsWith(".java"))
                .collect(Collectors.toList());
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}

