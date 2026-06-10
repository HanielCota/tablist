/**
 * The stateful core of the tab list: snapshots, diffing and coalesced flushing.
 *
 * <p>{@link com.hanielfialho.tablist.core.state.TabSnapshot TabSnapshot} is the immutable, complete
 * state of one viewer's tab. {@link com.hanielfialho.tablist.core.state.DirtyTracker DirtyTracker}
 * and {@link com.hanielfialho.tablist.core.state.SnapshotStore SnapshotStore} are first-class
 * collections, and {@link com.hanielfialho.tablist.core.state.TabFlusher TabFlusher} ties them
 * together: once per flush it drains the dirty viewers, rebuilds their snapshots, diffs each
 * against the last one sent and renders only the difference.
 */
package com.hanielfialho.tablist.core.state;
