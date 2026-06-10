package com.hanielfialho.tablist.paper.bootstrap;

import com.hanielfialho.commandframework.paper.DefaultBootstrapConfiguration;
import com.hanielfialho.commandframework.paper.PaperBootstrap;
import com.hanielfialho.commandframework.paper.instance.CommandInstanceResolver;
import com.hanielfialho.tablist.paper.TablistPlugin;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Registers the {@code /tablist} command through the CommandFramework during Paper's {@code
 * COMMANDS} lifecycle. Named as {@code bootstrapper} in {@code paper-plugin.yml}.
 *
 * <p>It composes a {@link PaperBootstrap} with a resolver that injects the plugin's shared services
 * into the command. The services are looked up lazily from the enabled plugin (the same pattern the
 * framework itself uses for its scheduler), so no static singleton is needed and the command and
 * the flush loop share one graph.
 */
public final class TablistBootstrap implements PluginBootstrap {

  private static final String COMMAND_PACKAGE = "com.hanielfialho.tablist.paper.command";
  private static final String PLUGIN_NAME = "Tablist";

  @Override
  public void bootstrap(BootstrapContext context) {
    Supplier<TablistServices> services = new PluginServicesSupplier();
    var configuration =
        new DefaultBootstrapConfiguration() {
          @Override
          public CommandInstanceResolver instanceResolver() {
            return new TablistCommandInstanceResolver(services);
          }
        };
    new PaperBootstrap(COMMAND_PACKAGE, configuration).bootstrap(context);
  }

  /** Resolves and caches the enabled plugin's shared services on first use. */
  private static final class PluginServicesSupplier implements Supplier<TablistServices> {

    private TablistServices cached;

    private static TablistServices lookup() {
      Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
      if (plugin instanceof TablistPlugin tablist) {
        return tablist.services();
      }
      throw new IllegalStateException("Tablist is not enabled yet; cannot resolve its services.");
    }

    @Override
    public TablistServices get() {
      if (cached == null) {
        cached = lookup();
      }
      return cached;
    }
  }
}
