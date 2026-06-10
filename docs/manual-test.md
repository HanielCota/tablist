# Tablist — manual test

This walks through verifying the Paper adapter end to end: **per-viewer isolation**
on Paper and **no thread-safety errors** on Folia. The plugin uses only the
official API (no NMS, no packets, no ProtocolLib).

## 1. Build the plugin jar

The deployable jar bundles `tablist-core`, Caffeine and Configurate. Adventure
and `paper-api` are provided by the server, so they are intentionally left out.

```bash
./gradlew :tablist-paper:shadowJar
```

Output: `tablist-paper/build/libs/tablist-paper-0.1.0-SNAPSHOT-all.jar`
(use the **`-all`** jar — the thin `jar` does not contain the bundled deps).

## 2. Paper 1.26 — per-viewer isolation

1. Download a Paper **1.26 line** build (`26.1.x`, e.g. `26.1.2.build.69-stable`)
   into a fresh server folder and accept the EULA.
2. Copy the `-all` jar into `plugins/` and start the server. On first run it
   writes `plugins/Tablist/config.yml`.
3. Confirm the console shows `Tablist enabled.` with **no stack trace**.
4. Join with **two accounts** (a second account, or a second client / alt). Open
   the tab list (hold `Tab`) on each.

Expected:

- Both players appear in the list with the configured name format
  (`%player_name%` resolved, MiniMessage colours applied).
- The **header/footer is sent per viewer** — it is delivered to each player as an
  audience, so the two clients are driven independently. Edit
  `plugins/Tablist/config.yml` (e.g. change the `header` frames), run
  `/reload confirm` *or* rejoin, and confirm each viewer updates on its own.
- The animated header cycles between its two gradient frames (~1s each); a
  static footer with `%online%` shows the live online count at render time.
- Disconnect one account and reconnect it a few times. Memory stays flat and the
  console stays clean — on quit the viewer is removed from the dirty set, its
  cache entries are dropped and its last snapshot is discarded (anti-leak).

### Quick isolation check

With two players online, set distinct values by giving them different groups in a
permissions plugin (or temporarily hard-code two name formats) and confirm each
player's **name/order** is applied to that player only. Note: names and list
order are *global per listed player* in vanilla (the same for every viewer); only
header/footer is per-viewer. That is the official-API behaviour — no packets are
forged to fake per-viewer names.

## 3. Folia — no console errors

1. Download a **Folia** build for the same 1.26 line into a separate server
   folder.
2. Drop in the same `-all` jar and start it.
3. Confirm the console prints `Tablist enabled.` and that Tablist is using the
   Folia scheduler (it is selected automatically when
   `io.papermc.paper.threadedregions.RegionizedServer` is present).
4. Join with two accounts, walk into different regions (far apart), open the tab
   list, and disconnect/reconnect.

Expected:

- **No `IllegalStateException` / "Cannot read/write … asynchronously" / wrong-region
  errors** in the console. Every player interaction
  (`sendPlayerListHeaderAndFooter`, `playerListName`, `setPlayerListOrder`) is
  dispatched to that player's own region thread via their `EntityScheduler`; the
  global flush loop only coalesces and dispatches.
- Tab list behaves exactly as on Paper.

## What is being exercised

| Requirement                | Where                                                        |
| -------------------------- | ----------------------------------------------------------- |
| Resolve player at render   | `PaperTabRenderer.runIfOnline` → `Bukkit.getPlayer(uuid)`   |
| Header/footer              | `Player#sendPlayerListHeaderAndFooter(Component, Component)` |
| Name                       | `Player#playerListName(Component)`                          |
| Sorting (native)           | `Player#setPlayerListOrder(int)` (teams only as fallback)   |
| Paper vs Folia             | `TabSchedulerFactory` (runtime `Class.forName` probe)       |
| Per-player thread (Folia)  | `FoliaViewerScheduler` → `Player#getScheduler()`            |
| Join/quit anti-leak        | `JoinQuitListener` → `ViewerLifecycle.quit` (`forget` all)  |
| Manual object graph        | `TablistPlugin#onEnable` (no DI framework, no singletons)   |

## Notes / known limitations

- **Continuous placeholder refresh** is driven by the resolution cache's
  `expireAfterWrite` (`placeholder-refresh-seconds`, default 3s) plus the
  per-frame diff. Values inside a *static* frame refresh when a render is
  triggered (frame change, join/quit); making every dynamic value tick
  independently would require snapshots to carry resolved text and is a planned
  follow-up.
- Caffeine and Configurate are bundled **without relocation**. If another plugin
  on the server ships conflicting versions, enable shading/relocation in
  `tablist-paper/build.gradle.kts` before production use.
