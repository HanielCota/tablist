package com.hanielfialho.tablist.core.config;

import java.nio.file.Path;
import java.util.Objects;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * Reads, merges and validates the YAML configuration into a {@link TabConfig}.
 *
 * <p>The parsed file is merged over the built-in {@linkplain TabConfig#defaults() defaults}, so a
 * partial file still yields a complete configuration and a missing file yields the defaults. Any
 * syntax or validation failure is surfaced as a {@link ConfigException} with a readable message.
 */
public final class ConfigLoader {

  private final YamlConfigurationLoader loader;
  private final ConfigurationNode defaults;

  /**
   * Creates a loader bound to a configuration file.
   *
   * @param file the YAML file to read; need not exist yet
   */
  public ConfigLoader(Path file) {
    this.loader = YamlConfigurationLoader.builder().path(file).nodeStyle(NodeStyle.BLOCK).build();
    this.defaults = serializedDefaults(loader);
  }

  /**
   * Reads the file, applies defaults and validates the result.
   *
   * @return the loaded, complete and valid configuration
   * @throws ConfigException if the YAML is malformed or a value fails validation
   */
  public TabConfig load() throws ConfigException {
    try {
      return deserialize(loader.load().mergeFrom(defaults));
    } catch (ConfigurateException error) {
      throw new ConfigException("Invalid Tablist configuration: " + error.getMessage(), error);
    }
  }

  private static TabConfig deserialize(ConfigurationNode node) throws SerializationException {
    return Objects.requireNonNull(node.get(TabConfig.class), "config");
  }

  private static ConfigurationNode serializedDefaults(YamlConfigurationLoader loader) {
    try {
      return loader.createNode().set(TabConfig.class, TabConfig.defaults());
    } catch (SerializationException error) {
      throw new IllegalStateException("default configuration must be serializable", error);
    }
  }
}
