package com.data.extintor;

public interface ConfigurationLoader<T> {

  T loadConfigurationFile(String file);
}
