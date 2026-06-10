# Contributing to Tablist

Thanks for your interest in improving Tablist! This is a small, focused project; the bar is
simply that contributions keep it fast, modular and well-tested.

## Getting started

```bash
git clone https://github.com/hanielfialho/tablist
cd tablist
./gradlew build      # compiles both modules, runs the tests, checks formatting
```

You need a **Java 25** JDK available; the Gradle toolchain selects it. Use the bundled
wrapper (`./gradlew`) — do not install Gradle yourself.

## Project layout

- `tablist-core` — pure domain logic and contracts. **Must never import Bukkit/Paper.**
- `tablist-paper` — the Paper adapter and plugin bootstrap; the only module allowed to touch
  the server API.

Put logic in the core and keep the adapter thin: if a class can be written without a `Player`,
it belongs in `tablist-core`.

## Coding standards

- **Formatting** is enforced by [Spotless](https://github.com/diffplug/spotless) with Google
  Java Format. Run `./gradlew spotlessApply` before committing; CI rejects unformatted code.
- **Object Calisthenics** is the house style. In particular: classes ≤ 150 lines, methods
  ≤ 15 lines, one level of indentation per method, no `else` where an early return will do,
  no static singletons, and wrap primitives/collections in first-class types.
- **Core stays pure.** No `org.bukkit` / `io.papermc` import may appear in `tablist-core`, and
  no live `Player` reference may be stored anywhere — hold a `ViewerId` and resolve on demand.
- **Javadoc in English** on every public type and member.
- **Tests** (JUnit 5 + Mockito) for every behavioural change. The non-spam performance
  contract (`StaticConfigNoSpamTest`) must keep passing — it is the project's reason to exist.

## Pull requests

1. Branch from `main`.
2. Keep the change focused; one concern per PR.
3. Ensure `./gradlew build` is green and `./gradlew spotlessApply` leaves no diff.
4. Describe what changed and why, and add or update tests.

By contributing you agree that your work is licensed under the project's [MIT License](LICENSE).
