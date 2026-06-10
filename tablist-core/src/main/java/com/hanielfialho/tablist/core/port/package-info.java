/**
 * Ports: the contracts the core defines and the platform implements.
 *
 * <p>These interfaces are the seam between the platform-agnostic domain and a concrete server. The
 * core depends only on them; adapters such as {@code tablist-paper} provide the implementations
 * (talking to Paper, resolving placeholders, scheduling ticks).
 */
package com.hanielfialho.tablist.core.port;
