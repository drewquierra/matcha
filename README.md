# Matcha App

A JavaFX and Maven-based application developed for managing and streamlining Matcha App operations. This project is designed to run in IntelliJ IDEA using Java and Maven.

---

## Features

* User-friendly JavaFX interface
* Maven project structure
* Database integration
* Dashboard management
* Employee and admin functionalities
* Login and authentication system

---

## Technologies Used

* Java
* JavaFX
* Maven
* MySQL
* IntelliJ IDEA

---

## Requirements

Before running the project, make sure you have:

* JDK 11 or higher
* IntelliJ IDEA
* Maven
* MySQL Server

---

## Installation and Setup

### 1. Clone or Download the Repository

```bash id="7pmsmu"
git clone https://github.com/drewquierra/matcha.git
```

Or download the ZIP file and extract it.

---

### 2. Open in IntelliJ IDEA

1. Open IntelliJ IDEA
2. Click **Open**
3. Select the project folder
4. Wait for Maven dependencies to load

---

### 3. Configure Database

Create a `.env` file in the root directory and add:

```env id="iggl8q"
DB_URL=jdbc:mysql://localhost:3306/your_database
DB_USER=root
DB_PASSWORD=your_password
```

Make sure your MySQL server is running.

---

## Running the Project

Open the IntelliJ terminal and run:

```bash id="84pq1d"
mvn javafx:run
```

If Maven is not recognized:

```bash id="e0w8fk"
mvnw.cmd javafx:run
```

You may also run the main class directly:

```text id="5fgh9y"
com.matcha.controller.MainApp
```

---

## Project Structure

```text id="r4vh4r"
src/
 ├── main/
 │    ├── java/
 │    └── resources/
pom.xml
README.md
```

---

## Notes

* `.env` files are excluded from GitHub for security purposes.
* IntelliJ-generated files are ignored through `.gitignore`.
* Ensure all dependencies are downloaded before running the application.

---

## Developers

Developed as part of a JavaFX and database-integrated application project.

---

## License

This project is for educational purposes only.
