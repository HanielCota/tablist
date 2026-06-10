# AGENTS.md — CommandFramework

Canonical, copy-paste reference for AI agents generating PaperMC plugins with **CommandFramework**.
Every snippet here compiles against the current API. Prefer cloning a recipe below over inventing
syntax. If you are unsure whether a type or annotation exists, it is not in this file → it does not
exist.

> Stack: Java 25 · Paper 1.21+ · Brigadier · Adventure · Gradle. Commands are declared with
> annotations on methods and compiled to a Brigadier tree at boot.

---

## Mental model (read first)

- A command is a **plain class** annotated `@Command("name")`.
- Each executable action is a **method** annotated `@Subcommand("verb")`.
- Method parameters become Brigadier arguments via `@Argument("name")`. Their **Java type** decides
  how they parse — no manual registration for built-in types.
- `CommandActor` and `CommandScheduler` parameters are **injected**, not parsed — they never become
  Brigadier arguments. Add them anywhere in the parameter list.
- Commands hold **no business logic**: inject a service and delegate. Keep bodies thin.
- A method returns **`void`** (synchronous) or a **`CompletionStage<?>`** (e.g.
  `CompletableFuture<Void>`) for async work. Returning the future ties `@Cooldown` to the
  deferred result: the cooldown commits only when the future completes **successfully**.
- Registration happens at boot through a `PluginBootstrap` + a `BootstrapConfiguration`. You do
  **not** call any register method yourself.

---

## The 4 required files for any plugin

A working plugin needs exactly these pieces. This is the minimal, current, correct wiring.

### 1. `src/main/resources/paper-plugin.yml`

```yaml
name: MyPlugin
version: 1.0.0
main: com.example.myplugin.MyPlugin
bootstrapper: com.example.myplugin.MyBootstrap
api-version: '1.21'
```

### 2. The plugin entry point — can be empty

```java
package com.example.myplugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {
  // Commands register in the bootstrapper during the COMMANDS lifecycle. Nothing needed here.
}
```

### 3. The bootstrapper — **current API**

> ⚠️ You do **not** subclass `PaperBootstrap` and you do **not** call `super("package")`.
> `PaperBootstrap` takes `(String basePackage, BootstrapConfiguration configuration)`. Your class
> implements `PluginBootstrap` and delegates. Use `DefaultBootstrapConfiguration` for sane defaults.

```java
package com.example.myplugin;

import com.hanielfialho.commandframework.paper.DefaultBootstrapConfiguration;
import com.hanielfialho.commandframework.paper.PaperBootstrap;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;

public final class MyBootstrap implements PluginBootstrap {

  // Package scanned for @Command classes:
  private static final String COMMAND_PACKAGE = "com.example.myplugin.command";

  private final PaperBootstrap delegate =
      new PaperBootstrap(COMMAND_PACKAGE, new DefaultBootstrapConfiguration());

  @Override
  public void bootstrap(BootstrapContext context) {
    this.delegate.bootstrap(context);
  }
}
```

If your commands need injected services, override one hook (see [Dependency injection](#dependency-injection)):

```java
var configuration =
    new DefaultBootstrapConfiguration() {
      @Override
      public CommandInstanceResolver instanceResolver() {
        return new MyCommandInstanceResolver();
      }
    };
this.delegate = new PaperBootstrap(COMMAND_PACKAGE, configuration);
```

### 4. At least one `@Command` class — inside `COMMAND_PACKAGE`

```java
package com.example.myplugin.command;

import com.hanielfialho.commandframework.annotation.Argument;
import com.hanielfialho.commandframework.annotation.Command;
import com.hanielfialho.commandframework.annotation.Permission;
import com.hanielfialho.commandframework.annotation.Subcommand;
import com.hanielfialho.commandframework.execution.CommandActor;

@Command(value = "home", aliases = {"casa"})
public final class HomeCommand {

  @Subcommand("set")
  @Permission("myplugin.home.set")
  public void set(CommandActor actor, @Argument("name") String name) {
    actor.sendSuccess("Home '" + name + "' defined.");
  }
}
```

This registers `/home set <name>` (and `/casa set <name>`) with native tab-complete, permission
check, type validation, and ready-made error messages. You never wrote `if (args.length < 2)`.

---

## Annotation cheat sheet

| Annotation | Target | Key fields (defaults) | Effect |
| --- | --- | --- | --- |
| `@Command` | class | `value`, `aliases={}` | Command root. Aliases only on the root. |
| `@Subcommand` | method | `value`, `aliases={}` | Executable node under the root. Supports nested paths: `@Subcommand("admin reload")`. `aliases` add alternative names for the final segment (`@Subcommand(value="create", aliases="new")` → `/x create` and `/x new`). |
| `@DefaultCommand` | method | — | Runs when the root is called with no subcommand (`/home`). |
| `@Argument` | parameter | `value`, `greedy=false` | Names the positional argument. `greedy=true` consumes the rest (String only, must be last). |
| `@OptionalArg` | parameter | — | On a final `Optional<T>`: optional positional. Absent → `Optional.empty()`. |
| `@Flag` | parameter | `value`, `aliases={}` | Named flag after positionals. `boolean` = switch; `Optional<T>` = value flag. |
| `@Range` | parameter | `min`, `max` | Bounds for `int`/`long` (validated client-side by Brigadier). |
| `@Regex` | parameter | `value` | Validates a `String` against a pattern at bind time. |
| `@Length` | parameter | `min`, `max` | Length bounds for a `String`. |
| `@Suggest` | parameter | `SuggestionSource.class` | Tab-completion source (cached, off-thread). |
| `@Permission` | class or method | `value` | Required permission node. On class = applies to all subcommands. |
| `@Cooldown` | method | `seconds`, `bypassPermission=""` | Per-player cooldown. Bypass-node holders skip it. On a method returning `CompletionStage<?>`, commits only when the future succeeds. |
| `@Confirm` | method | `value` (prompt) | Requires `/confirm` before running. |
| `@PlayerOnly` | method | — | Rejects console/command-block/RCON. |
| `@Async` | method | — | Runs the body off the main thread; feedback and the cooldown commit return on the main thread. Body returns `void`/`CommandResult`, not `CompletionStage`. |
| `@Description` | class or method | `value` | Text for the auto-generated `/help` (method overrides class). |
| `@Requires` | class or method | `CommandCondition.class...` | Custom guards run before the body. First denial stops execution. |
| `@Context` | parameter | — | Injects a sender-derived value via a registered `ContextResolver`. |

`/help` and `/confirm` are registered automatically — never declare them yourself.

### Auto-injected parameters (NOT Brigadier arguments)

Add either, both, or neither — in any position:

- `CommandActor actor` — platform-neutral sender. Methods: `sendSuccess/sendError/sendInfo(String)`,
  `hasPermission(String)`, `playerId()` → `Optional<PlayerId>` (empty for console), `name()`,
  `locale()`.
- `CommandScheduler scheduler` — threading boundary. `async(Supplier<T>)` → `CompletableFuture<T>`
  off the main thread; `runFor(PlayerId, Runnable)` back on the player's owning thread; `sync(Runnable)`
  back on the main/global thread. (Or skip it entirely and annotate the body `@Async`.)
- `ServiceContainer services` — typesafe registry of late-bound services: `require(Class<T>)` /
  `find(Class<T>)`. Provide it via `BootstrapConfiguration#serviceContainer()`; see below.

---

## Argument types (no registration needed)

The **parameter's Java type** decides parsing:

| Type | Notes |
| --- | --- |
| `String` | Single token; `@Argument(greedy=true)` consumes the rest of the line. |
| `int` / `Integer`, `long` / `Long` | Numeric; combine with `@Range`. |
| `boolean` / `Boolean` | `true`/`false`. |
| `UUID` | Parsed from canonical UUID string. |
| any `enum` | Matched by name, case-insensitive, with value suggestions. |
| `PlayerId`, `CoinAmount` | Core domain value objects (`com.hanielfialho.commandframework.domain.value`). |
| `Player` | Native online-player selector. Missing/offline target → clean parse error before your method. |
| `OfflinePlayer` | Resolved by profile. |
| `World` | Native world selector. |
| `List<T>` | Final parameter only; consumes the rest of the line. `T` = `String`, number, `boolean`, or `enum`. |
| `Optional<T>` + `@OptionalArg` | Final optional positional. |
| `Optional<T>` + `@Flag` | Value flag. |

Anything else must be registered — see [Custom argument type](#custom-argument-type).

---

## Cookbook (complete, compiling commands)

Each recipe is a full command class. Clone the closest one.

### Simple subcommands

```java
@Command("kit")
@Description("Starter kits")
public final class KitCommand {

  @Subcommand("list")
  public void list(CommandActor actor) {
    actor.sendInfo("Available kits: starter, pvp, builder");
  }

  @Subcommand("give")
  @Permission("myplugin.kit.give")
  public void give(CommandActor actor, @Argument("kit") String kit) {
    actor.sendSuccess("Gave kit '" + kit + "'.");
  }
}
```

### Default root action + nested path

```java
@Command("guild")
@Description("Guild commands")
public final class GuildCommand {

  @DefaultCommand
  public void overview(CommandActor actor) { // /guild
    actor.sendInfo("Use /guild create <name>");
  }

  @Subcommand(value = "create", aliases = "new") // /guild create <name> or /guild new <name>
  public void create(CommandActor actor, @Argument("name") String name) {
    actor.sendSuccess("Guild '" + name + "' created.");
  }

  @Subcommand("admin reload") // nested path → /guild admin reload
  @Permission("myplugin.guild.admin")
  public void reload(CommandActor actor) {
    actor.sendSuccess("Reloaded.");
  }
}
```

### Numeric range, player target, enums

```java
@Command("warp")
public final class WarpCommand {

  enum WarpMode { SAFE, INSTANT }

  @Subcommand("set")
  public void set(
      CommandActor actor,
      @Argument("name") @Length(min = 3, max = 16) @Regex("[A-Za-z0-9_]+") String name,
      @Argument("radius") @Range(min = 1, max = 64) int radius,
      @Argument("mode") WarpMode mode) {
    actor.sendSuccess("Warp '" + name + "' (r=" + radius + ", " + mode + ") set.");
  }

  @Subcommand("send")
  @PlayerOnly
  public void send(CommandActor actor, @Argument("target") Player target) {
    actor.sendSuccess("Sending " + target.getName() + " to a warp.");
  }
}
```

### Greedy string + List

```java
@Command("broadcast")
public final class BroadcastCommand {

  @Subcommand("say")
  @Permission("myplugin.broadcast")
  public void say(CommandActor actor, @Argument(value = "message", greedy = true) String message) {
    actor.sendSuccess("Broadcasting: " + message); // /broadcast say hello there world
  }

  @Subcommand("levels")
  public void levels(CommandActor actor, @Argument("values") java.util.List<Integer> values) {
    actor.sendInfo("Sum: " + values.stream().mapToInt(Integer::intValue).sum()); // /broadcast levels 10 20 30
  }
}
```

### Flags (switch + value flag) and optional positional

```java
@Command("item")
public final class ItemCommand {

  @Subcommand("give")
  public void give(
      CommandActor actor,
      @Argument("name") String name,
      @Flag(value = "amount", aliases = "a") java.util.Optional<Integer> amount, // --amount 5 / -a 5
      @Flag("silent") boolean silent) {                                          // --silent (switch)
    int quantity = amount.orElse(1);
    if (!silent) {
      actor.sendSuccess("Gave " + quantity + "x " + name);
    }
  }

  @Subcommand("info")
  public void info(
      CommandActor actor,
      @Argument("name") String name,
      @Argument("detail") @OptionalArg java.util.Optional<String> detail) { // detail is optional, last
    actor.sendInfo(name + (detail.isPresent() ? " — " + detail.get() : ""));
  }
}
```

### Cooldown + confirmation

```java
@Command("data")
public final class DataCommand {

  @Subcommand("daily")
  @Cooldown(seconds = 86400, bypassPermission = "myplugin.data.nocooldown")
  public void daily(CommandActor actor) {
    actor.sendSuccess("Daily reward claimed!");
  }

  @Subcommand("reset")
  @Confirm("This wipes ALL your data. Run /confirm to proceed.")
  public void reset(CommandActor actor) {
    actor.sendSuccess("Data reset.");
  }
}
```

### Off-thread I/O with the scheduler

Never touch the Bukkit API off the main thread. Do I/O in `async`, then come back with `runFor`.
**Return the `CompletionStage`** when the subcommand has a `@Cooldown`: the cooldown then commits
only after the deferred work succeeds, so failed I/O lets the player retry.

```java
@Command("stats")
public final class StatsCommand {

  private final StatsRepository repository; // injected via instanceResolver()

  public StatsCommand(StatsRepository repository) {
    this.repository = java.util.Objects.requireNonNull(repository);
  }

  @Subcommand("show")
  @PlayerOnly
  @Cooldown(seconds = 5)
  public java.util.concurrent.CompletionStage<Void> show(
      CommandActor actor, CommandScheduler scheduler) {
    PlayerId playerId = actor.playerId().orElseThrow();

    return scheduler
        .async(() -> repository.load(playerId))                // I/O off-thread
        .thenAccept(stats -> scheduler.runFor(                 // back on the player's thread
            playerId, () -> actor.sendInfo("Kills: " + stats.kills())));
  }
}
```

Return `void` instead when there is no cooldown (or the cooldown should commit immediately,
regardless of the async outcome).

### `@Async`: let the framework do the thread hop

When the whole body is blocking I/O, annotate it `@Async` instead of wiring the scheduler by hand.
The framework runs the body on the platform's async pool, then applies the terminal result — error
feedback and the `@Cooldown` commit — back on the **main thread**. The body just blocks and returns
`void` or a `CommandResult`.

```java
@Subcommand("show")
@PlayerOnly
@Cooldown(seconds = 5)
@Async
public void show(CommandActor actor) {
  PlayerId playerId = actor.playerId().orElseThrow();

  var stats = repository.load(playerId); // blocking I/O — already off the main thread
  actor.sendInfo("Kills: " + stats.kills());
}
```

The cooldown commits only if the body finishes without throwing, mirroring the `CompletionStage`
path. A thrown exception is mapped to a clean error and reported on the main thread. `@Async` is for
a synchronous body: a method that already returns a `CompletionStage` is managing its own threading,
so combining the two is **rejected at compile time and at boot**. Like the deferred path, the
`CommandPostExecuteEvent` carries the dispatch-time `Success`, not the off-thread outcome.

---

## Dependency injection

Default: commands and suggestion sources are built via their **no-arg constructor**. If a command
needs services, provide a `CommandInstanceResolver` and inject through the constructor (no static
singletons). Register it on the configuration's `instanceResolver()` hook.

```java
package com.example.myplugin;

import com.hanielfialho.commandframework.paper.instance.CommandInstanceResolver;
import com.hanielfialho.commandframework.paper.instance.ReflectiveCommandInstanceResolver;
import com.example.myplugin.command.StatsCommand;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MyCommandInstanceResolver implements CommandInstanceResolver {

  private final CommandInstanceResolver fallback = new ReflectiveCommandInstanceResolver();
  private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();
  private final StatsRepository repository = StatsRepository.inMemory();

  @Override
  public Object resolve(Class<?> type) {
    return this.instances.computeIfAbsent(type, this::build);
  }

  private Object build(Class<?> type) {
    if (type == StatsCommand.class) {
      return new StatsCommand(this.repository);
    }
    return this.fallback.resolve(type); // anything else: no-arg
  }
}
```

Wire it in the bootstrapper (see [step 3](#3-the-bootstrapper--current-api)).

### `ServiceContainer`: bootstrap-time wiring, enable-time services

Commands register during the `COMMANDS` lifecycle — **before** `onEnable`, so any service that only
exists once the plugin is enabled cannot be wired in directly. The `ServiceContainer` bridges that
gap: a typesafe, thread-safe registry keyed by class. Create one `DefaultServiceContainer`, expose it
from `serviceContainer()`, populate it in `onEnable`, and the framework injects that same instance
into any command (or hands it to your resolver) — no `JavaPlugin.getInstance()` reach-around.

```java
// shared instance, held by both the configuration and the plugin
ServiceContainer services; // a DefaultServiceContainer

// in the configuration:
@Override public ServiceContainer serviceContainer() { return this.services; }

// in onEnable, once the real services exist:
((DefaultServiceContainer) services).register(MenuOpener.class, new PaperMenuOpener(this));
// or lazily, resolved (and memoized) on first use:
((DefaultServiceContainer) services).registerLazy(Economy.class, () -> hookEconomy());

// in a command:
@Subcommand("open")
public void open(CommandActor actor, ServiceContainer services) {
  services.require(MenuOpener.class).open(actor.playerId().orElseThrow());
}
```

`require(Class<T>)` throws `MissingServiceException` if the service was never registered; `find` returns
an `Optional`. The container imposes no ordering, so it can sit anywhere in the parameter list.

---

## Config-sourced aliases & permissions

`@Command(aliases=…)` and `@Permission` are compile-time defaults. To let a server owner override them
from configuration, implement `CommandRouteOverrides` and return it from `routeOverrides()`. The
annotation is the default; an override, when present, wins.

```java
public final class YamlRouteOverrides implements CommandRouteOverrides {
  @Override public Optional<String> permissionFor(CommandPath path) {
    return config.permission(path.toString()); // e.g. "money pay" → "economy.admin"
  }
  @Override public Optional<List<String>> aliasesFor(String rootCommand) {
    return config.aliases(rootCommand);         // e.g. "money" → ["grana", "carteira"]
  }
}

@Override public CommandRouteOverrides routeOverrides() { return this.overrides; }
```

The two routes differ in **when** they are resolved, which fixes their reload semantics:

- **Permission** is resolved on *every* check, so a permission override is **live** — reload your
  config source and the next invocation already sees the new node. Works in both `NOTIFY` (pipeline
  enforces) and `HIDE` (Brigadier `requires`) visibility modes. An empty effective permission leaves
  the command open.
- **Aliases** are resolved **once, at registration**, because they are baked into the Brigadier tree
  at startup. Changing an alias override therefore needs a **server restart**, not a reload — a Paper
  limitation. (An empty override list removes the annotation aliases entirely.)

---

## Custom argument type

Only when a parameter type is not built in. Override `registerArguments` on the configuration. Both
registries arrive pre-populated with the built-ins.

```java
@Override
public void registerArguments(
    ArgumentRegistry argumentRegistry, ArgumentTypeRegistry typeRegistry) {

  var kind = new ArgumentKind("myplugin:material");

  ArgumentExtractor<Material> extractor = raw -> (Material) raw;
  argumentRegistry.register(Material.class, new ArgumentMapping<>(Material.class, kind, extractor));

  typeRegistry.register(kind, new MaterialArgumentBinding()); // implements ArgumentBinding
}
```

`ArgumentBinding` supplies the Brigadier `ArgumentType` and reads the raw value from the context.

---

## Suggestions (tab-complete)

Implement `SuggestionSource`; point with `@Suggest`. `fetch` runs on the Brigadier thread — never
block it. Return a `CompletableFuture`; an in-memory source returns a completed one. `fetch` takes a
`SuggestionContext` carrying both the partial `input()` and the requesting `actor()`, so a source can
tailor completions to who is asking (permissions, the player's own records, …).

```java
public final class KitSuggestionSource implements SuggestionSource {

  @Override
  public java.util.concurrent.CompletableFuture<java.util.List<String>> fetch(SuggestionContext context) {
    var prefix = context.input();
    var viewer = context.actor(); // filter by permission / ownership if needed
    return java.util.concurrent.CompletableFuture.completedFuture(
        java.util.List.of("starter", "pvp", "builder")); // in-memory → no blocking
  }
}

// usage:
@Subcommand("give")
public void give(CommandActor actor, @Argument("kit") @Suggest(KitSuggestionSource.class) String kit) { /* ... */ }
```

Results are cached per `(actor, input)` pair, so an actor-sensitive source never leaks one actor's
suggestions to another, and the live sender is never retained as a cache key. Suggestion sources with
dependencies are built by your `CommandInstanceResolver`, like commands.

---

## Execution events

The framework fires two Bukkit events around every dispatch, so cross-cutting concerns attach as
listeners instead of living in command code. Both are in `com.hanielfialho.commandframework.paper.event`.

- `CommandPreExecuteEvent` — before the body runs (args already parsed). **Cancellable**: cancelling
  stops the command and the framework sends no feedback. Exposes `getSender()`, `getPath()`
  (`List<String>` segments), `getInput()`.
- `CommandPostExecuteEvent` — after the body returns. Exposes `getSender()`, `getPath()`,
  `getResult()` (the terminal `CommandResult`; for a future-returning command this is the
  dispatch-time `Success`, not the deferred outcome).

```java
public final class CommandMetrics implements Listener {

  @EventHandler
  public void onCommand(CommandPostExecuteEvent event) {
    metrics.record(String.join(" ", event.getPath()), event.getResult());
  }
}
```

Register the listener like any other Bukkit listener during plugin enable.

---

## Testing without a server

`CommandTestHarness` (in `command-paper`) builds a real `CommandDispatcher` from your `@Command`
classes. Pass a `CommandSourceStack` (e.g. from MockBukkit) and dispatch strings.

```java
var harness = CommandTestHarness.forCommands(HomeCommand.class);

int result = harness.execute("home set casa", source);        // execute
List<String> tab = harness.completions("home set ", source);  // tab-complete
```

Pure-core domain logic is tested with plain JUnit, no server.

---

## Hard rules (these break the build or the server — follow them)

- **Bootstrap API:** `new PaperBootstrap(basePackage, configuration)`. Do **not** subclass
  `PaperBootstrap` or call `super(...)`. Override hooks on a `DefaultBootstrapConfiguration`, not on
  the bootstrap.
- **`@Command` classes must live inside the scanned `basePackage`** — otherwise they are never
  registered (and you get no error, just a missing command).
- **No business logic in commands.** Inject a service, delegate. Bodies ≤ ~15 lines, 1 indent level.
- **Never store `Player`/`Entity`** in a field — store `UUID`/`PlayerId`, resolve on demand.
- **Threading:** all I/O via `scheduler.async(...)`; never call the Bukkit API off the main thread.
  Use `scheduler.runFor(...)` to return to it.
- **Messages:** use `actor.sendSuccess/Error/Info` for command output. No `§`/`&` legacy color
  codes. Player input is rendered as a literal Component (no MiniMessage injection). The framework's
  own feedback (`CommandMessages`) are **MiniMessage templates** — style them with tags; dynamic
  values use `<name>` placeholders and are inserted as inert literal text.
- **No static singletons / `Plugin.getInstance()`.** Inject everything through the constructor.
- **Optional, not null.** Return `Optional<T>`, never `null`.
- **Compile with `-parameters`** (the build already does) — the scanner reads parameter names.

### Validation that fails the build (fix these if you see them)

With the `command-processor` enabled (see below), these are reported by `./gradlew build` as a
compile error pointing at the exact parameter — fix them there, no need to start a server:

- A positional argument after a `@Flag`; `@Flag` after a greedy/`List` argument.
- A required positional after an optional one (`Optional<T>` / `@OptionalArg` must be last).
- `@Range` on a non-int/long parameter, or `@Regex`/`@Length` on a non-`String` parameter.
- `@Argument(greedy=true)` on a non-`String`.
- A `@Flag` that is not `boolean` or `Optional<T>`, a blank flag name, or a duplicate flag
  name/alias; a valued flag with an unsupported type.
- An invalid `@Regex` pattern; a raw/unsupported `Optional<T>` or `List<T>` element type.
- `@Cooldown(seconds = 0)`; a method marked both `@Subcommand` and `@DefaultCommand`;
  `@OptionalArg` on a non-`Optional`; a `@Suggest` source that is not a `SuggestionSource`.

A few checks still surface only at server startup (the processor cannot prove them from source):
a parameter type with **no registered argument mapping**, and cross-overload **conflicting
arguments** / **duplicate command paths**.

### Enabling compile-time validation

Add the processor so invalid commands fail `./gradlew build` instead of at boot:

```kotlin
dependencies {
    annotationProcessor("com.hanielfialho:command-processor:0.1.0-SNAPSHOT")
}
```

---

## Build

```bash
./gradlew build          # all modules + tests + spotlessCheck
./gradlew spotlessApply  # format with google-java-format (2 spaces, 100 cols, no wildcard imports)
```

Run `spotlessApply` before considering any generated code done; `build` must be green.

---

For the full prose reference, see [`README.md`](README.md). For style and architecture
rules, see [`CLAUDE.md`](CLAUDE.md).
