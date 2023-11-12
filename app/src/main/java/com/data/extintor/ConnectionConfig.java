package com.data.extintor;

import jakarta.validation.constraints.NotNull;

public class ConnectionConfig {
  @NotNull(message = "host required")
  private String host;

  private int port = 3306;

  @NotNull(message = "numberOfThreads required")
  private String database;

  @NotNull(message = "username required")
  private String username;

  @NotNull(message = "username required")
  private String password;

  @NotNull(message = "driverClass required")
  private String driverClass;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDriverClass() {
    return driverClass;
  }

  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }
}
