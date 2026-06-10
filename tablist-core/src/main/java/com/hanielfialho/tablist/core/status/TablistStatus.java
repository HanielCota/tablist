package com.hanielfialho.tablist.core.status;

/**
 * An immutable snapshot of Tablist's runtime health.
 *
 * @param activeViewers the number of viewers currently tracked
 * @param cacheHitRate the resolved-text cache hit-rate, between {@code 0.0} and {@code 1.0}
 * @param updatesLastMinute the number of tab-list updates sent in the last minute
 * @param currentFrame the index of the header animation frame currently shown
 */
public record TablistStatus(
    int activeViewers, double cacheHitRate, int updatesLastMinute, int currentFrame) {}
