# Data Extintor

Data Extintor is a Java-based application for efficiently managing and purging data from databases. It operates based on configurations specified in YAML files, allowing for flexible and controlled data cleanup processes.

## Features
- Database connection setup via a YAML configuration file.
- Multi-threaded data purging operations.
- Dry-run mode for testing without modifying actual data.
- Detailed logging using SLF4J and Logback.

## Prerequisites
- Java JDK 17 or higher.
- Gradle for building the application.
- MySQL Database (as implied by the MySQL JDBC driver).

## Installation and Setup
1. **Clone the Repository**: Download the source code to your local environment.
2. **Gradle Build**: Navigate to the project directory and execute `gradle build` to compile the project and generate an executable JAR.

## Configuration
Create a YAML file (`application.yaml`) with the following structure:
- Database connection details (host, port, database name, user credentials).
- Thread configurations for purging (table name, conditional filters, limits).
- Log settings file path.
- Dry-run flag.

Example:
```yaml
connection:
  host: "localhost"
  port: 3306
  database: "your_db"
  username: "user"
  password: "pass"
purgeThreads:
  - tableName: "your_table"
    name: "Thread-Name"
    whereFilter: "your_conditions"
    limit: 1000
log-settings-path: "logback.xml"
dry-run: false
```

## Execution
Run the application using:
```bash
java -jar build/libs/data-extintor.jar --file=application.yaml
```
For dry-run mode, set `dry-run: true` in the configuration file.

## Docker Setup
The code includes a Docker configuration for setting up a MySQL database. Use Docker Compose to start a MySQL instance:
```yaml
version: '3.8'
services:
  db:
    image: mysql:8.0
    ...
```

## Logging
Configure logging details in `logback.xml` as referenced in the YAML configuration file.

## Caution
Always backup your database before performing purge operations, especially in production environments.

## Contributing
Contributions, feedback, and issue reporting are welcomed. Please refer to the project's GitHub repository.

---

