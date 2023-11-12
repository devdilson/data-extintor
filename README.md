# Data Extintor Readme

## Overview
Data Extintor is a Java application designed for data management and cleanup tasks in databases. It purges data based on configurations provided in a YAML file and supports a dry-run mode for testing without affecting data.

## Requirements
- Java Development Kit (JDK)
- Gradle (for building the project)

## Installation and Building
1. **Java Installation**: Ensure Java is installed on your system. Download it from the [Java website](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).
2. **Clone the Repository**: Clone the Data Extintor repository to your local machine.
3. **Build the Application**: Navigate to the project root directory and run the following command to build the application using Gradle:
   ```shell
   gradle build
   ```
   This will generate the executable JAR file in the `./app/build/libs/` directory.

## Configuration
Create a YAML configuration file (`application.yaml`) defining the database connection settings and tables to purge.

### Sample Configuration Structure
```yaml
connectionConfig:
  host: [database_host]
  port: [database_port]
  user: [database_user]
  password: [database_password]
purgeThreadsList:
  - tableName: [your_table_name]
    whereFilter: [your_where_filter]
    limit: [your_limit_number]
logSettingsPath: [path_to_log_settings_file]
```
Replace placeholders with your specific values.

## Running the Application

### Standard Mode
Run Data Extintor using:
```shell
java -jar ./app/build/libs/app.jar --file=application.yaml
```

### Dry-Run Mode
For a dry run (no data deletion):
```shell
java -jar ./app/build/libs/app.jar --file=application.yaml --dry-run
```

## Logging
The application logs operations as configured in `logSettingsPath` in the YAML file. Ensure the log settings file is correctly prepared for detailed logging.

## Troubleshooting
Refer to application logs for issues related to configuration or database connections.

## Contribution
To contribute or report issues, please visit our [GitHub repository](#).

---

*Note: Always back up your database before running cleanup operations, especially in production environments.*