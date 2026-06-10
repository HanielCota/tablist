package com.hanielfialho.tablist.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.ViewerDirectory;
import com.hanielfialho.tablist.core.state.DirtyTracker;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigReloaderTest {

  @TempDir private Path dir;

  private final List<ViewerId> viewers =
      List.of(new ViewerId(UUID.randomUUID()), new ViewerId(UUID.randomUUID()));
  private final AtomicInteger invalidations = new AtomicInteger();

  private Path file;
  private DirtyTracker dirty;
  private ActiveConfig active;
  private ConfigReloader reloader;

  @BeforeEach
  void setUp() {
    file = dir.resolve("config.yml");
    dirty = new DirtyTracker();
    active = new ActiveConfig(TabConfig.defaults(), () -> invalidations.incrementAndGet());
    ViewerDirectory directory = () -> viewers;
    reloader =
        new ConfigReloader(new ConfigLoader(file), active, new DirtyAllViewers(dirty, directory));
  }

  @Test
  void keepsPreviousConfigWhenYamlIsInvalid() throws IOException {
    write("header:\n  frames: [unterminated");
    TabConfig before = active.current();

    ReloadResult result = reloader.reload();

    assertTrue(result.failed(), "an invalid reload must fail");
    assertSame(before, active.current(), "the previous configuration must stay active");
    assertEquals(0, invalidations.get(), "caches must not be dropped on failure");
  }

  @Test
  void marksAllViewersDirtyOnSuccessfulReload() throws IOException {
    write("refresh:\n  placeholder-refresh-seconds: 5\n");

    ReloadResult result = reloader.reload();

    assertFalse(result.failed(), "a valid reload must succeed");
    assertEquals(5, active.current().refresh().placeholderRefreshSeconds(), "new value must apply");
    assertEquals(Set.copyOf(viewers), Set.copyOf(dirty.drainDirty()), "every viewer must be dirty");
  }

  private void write(String yaml) throws IOException {
    Files.writeString(file, yaml);
  }
}
