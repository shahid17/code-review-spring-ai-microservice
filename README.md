# AI Code Review Microservice

This project is an AI-powered microservice using Spring AI that behaves like a code review AI Agent and automatically reviews Java code in any Git repository — just like a senior developer would. It analyses code line by line and provides structured feedback on readability, security, performance, and best practices.

---

## Features

- Clone a public Git repository and extract Java files.
- Analyse each Java file line by line using OpenAI LLM.
- Identify:
  - Readability issues
  - Security vulnerabilities
  - Performance problems
  - Best practice violations
- Return structured JSON response with:
  - Line number
  - Issue description
  - Suggested fix
  - Corrected solution
- Concurrency support: review multiple files in parallel.
- Handles large files and errors gracefully.

---

## Tech Stack

- Java 17+
- Spring Boot 3+
- Spring AI (OpenAI integration)
- JGit (Git repository cloning)
- Jackson (JSON parsing)
- ExecutorService (parallel processing)
- Maven/Gradle for dependency management

---


---

## Getting Started

### Prerequisites

- Java 17+
- Maven or Gradle
- OpenAI API Key

### Setup

1. Clone the repository:
   ```bash
   git clone <your-repo-url>
   cd <project-folder>
   
2. Set your OpenAI API key in application.properties or environment variable:
   spring.ai.openai.api-key=<YOUR_OPENAI_API_KEY>

3. Build and run the application:
   mvn clean install
   mvn spring-boot:run

API Usage

* Review a repository:

POST /api/code-review
Content-Type: application/json

  {
    "repoUrl": "https://github.com/user/sample-repo.git",
    "branch": "main"
  }

* Response example:

  [
   {
    "filename": "MyClass.java",
    "review": {
      "issues": [
        {
          "line": 12,
          "issue": "Variable name is not meaningful",
          "suggestion": "Rename variable 'x' to 'userCount'",
          "solution": "int userCount = 2;"
        }
      ]
    }
  }
]

