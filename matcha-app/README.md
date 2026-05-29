# Matcha App

A JavaFX + Maven application for IntelliJ IDEA.

## Requirements

- JDK 11 or higher
- IntelliJ IDEA
- Maven
- MySQL Server

## Setup Instructions

1. Clone or download the repository.
2. Open the project in IntelliJ IDEA.
3. Allow Maven to import dependencies.
4. Configure your database credentials in the `.env` file.
5. Start MySQL server.
6. Run the project using:

```bash
mvn javafx:run
```

Or run the main class:

```text
com.matcha.controller.MainApp
```

## Project Structure

- `src/main/java` → Java source files
- `src/main/resources` → Application resources
- `pom.xml` → Maven dependencies and build configuration

## Notes

- `.env` is excluded from GitHub for security purposes.
- IntelliJ generated files are ignored using `.gitignore`.
