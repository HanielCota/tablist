package com.hanielfialho.tablist.paper.bootstrap;

import com.hanielfialho.commandframework.paper.instance.CommandInstanceResolver;
import com.hanielfialho.commandframework.paper.instance.ReflectiveCommandInstanceResolver;
import com.hanielfialho.tablist.paper.command.Feedback;
import com.hanielfialho.tablist.paper.command.PlaceholdersController;
import com.hanielfialho.tablist.paper.command.PreviewController;
import com.hanielfialho.tablist.paper.command.ReloadController;
import com.hanielfialho.tablist.paper.command.StatusController;
import com.hanielfialho.tablist.paper.command.TablistCommand;
import com.hanielfialho.tablist.paper.command.ToggleController;
import com.hanielfialho.tablist.paper.placeholder.PaperPlaceholderResolvers;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * {@link CommandInstanceResolver} that injects the shared services into {@link TablistCommand}.
 *
 * <p>The services are fetched lazily through a supplier the first time the command is resolved,
 * which only happens at dispatch — after the plugin is enabled and the graph exists. This is the
 * composition root for the command side; it owns no static state.
 */
public final class TablistCommandInstanceResolver implements CommandInstanceResolver {

  private final CommandInstanceResolver fallback = new ReflectiveCommandInstanceResolver();
  private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();
  private final Supplier<TablistServices> services;

  /**
   * Creates the resolver over a lazy source of the shared services.
   *
   * @param services supplies the shared services once the plugin is enabled; never {@code null}
   */
  public TablistCommandInstanceResolver(Supplier<TablistServices> services) {
    this.services = Objects.requireNonNull(services, "services");
  }

  @Override
  public Object resolve(Class<?> commandClass) {
    return instances.computeIfAbsent(commandClass, this::build);
  }

  private Object build(Class<?> commandClass) {
    if (commandClass == TablistCommand.class) {
      return buildCommand();
    }
    return fallback.resolve(commandClass);
  }

  private TablistCommand buildCommand() {
    TablistServices wired = services.get();
    Feedback feedback = new Feedback(wired.config());
    return new TablistCommand(
        new ReloadController(wired.reloader(), feedback),
        new StatusController(wired.statusReporter(), feedback),
        new ToggleController(wired.toggle(), wired.dirty(), feedback),
        new PreviewController(wired.config(), wired.pipeline(), feedback),
        new PlaceholdersController(feedback, PaperPlaceholderResolvers.placeholderApiPresent()));
  }
}
