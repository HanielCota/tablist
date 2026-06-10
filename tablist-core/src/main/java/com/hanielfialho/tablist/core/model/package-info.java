/**
 * Immutable value objects that make up the tab-list domain.
 *
 * <p>Everything here is a pure value: identities ({@link
 * com.hanielfialho.tablist.core.model.ViewerId ViewerId}), renderable text templates and the
 * composed display structures (entries, header and footer). None of these types references a server
 * platform or a live {@code Player}; adapters translate native types into these values.
 */
package com.hanielfialho.tablist.core.model;
