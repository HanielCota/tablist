package com.hanielfialho.tablist.core.state;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TablistToggleTest {

  private static final ViewerId VIEWER = new ViewerId(UUID.randomUUID());

  @Test
  void viewersAreEnabledByDefault() {
    assertTrue(new TablistToggle().isEnabled(VIEWER));
  }

  @Test
  void togglingFlipsBetweenCustomAndVanilla() {
    TablistToggle toggle = new TablistToggle();

    assertFalse(toggle.toggle(VIEWER), "first toggle turns the custom tab off");
    assertFalse(toggle.isEnabled(VIEWER));
    assertTrue(toggle.toggle(VIEWER), "second toggle turns it back on");
    assertTrue(toggle.isEnabled(VIEWER));
  }

  @Test
  void forgettingResetsToTheEnabledDefault() {
    TablistToggle toggle = new TablistToggle();
    toggle.toggle(VIEWER);

    toggle.forget(VIEWER);

    assertTrue(toggle.isEnabled(VIEWER), "a forgotten viewer reverts to the default");
  }
}
