/**
 * The configuration layer: the YAML-backed {@link com.hanielfialho.tablist.core.config.TabConfig
 * TabConfig} schema, frame animation and hot reloading.
 *
 * <p>Configuration is parsed with Configurate into immutable {@code @ConfigSerializable} records,
 * validated in their constructors, and swapped atomically by {@link
 * com.hanielfialho.tablist.core.config.ConfigReloader ConfigReloader}. Nothing here touches a
 * server platform; it is plain Java plus Configurate.
 */
package com.hanielfialho.tablist.core.config;
