import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

description = "Paper 1.26 adapter for Tablist: implements the core contracts and bootstraps the plugin."

// Bundle the plugin and its non-provided runtime dependencies into a single jar.
apply(plugin = "com.gradleup.shadow")

// Shared build conventions are applied from the root `subprojects` block, so
// dependency configurations are referenced by name (see tablist-core).
dependencies {
    "implementation"(project(":tablist-core"))

    // CommandFramework (HanielCota) — the only command layer; no Bukkit CommandExecutor.
    "implementation"(libs.commandframework.paper)
    "implementation"(libs.commandframework.core)

    // Provided by the Paper runtime; never bundled into the plugin jar.
    "compileOnly"(libs.paper.api)
    "compileOnly"(libs.adventure.minimessage)

    // Optional integration: compiled against, never bundled. The plugin runs fine
    // without it; the soft dependency lives in paper-plugin.yml.
    "compileOnly"(libs.placeholderapi)

    "testImplementation"(platform(libs.junit.bom))
    "testImplementation"(libs.junit.jupiter)
    "testImplementation"(libs.mockito.core)
    "testImplementation"(libs.mockito.junit.jupiter)
    // Paper provides MiniMessage at runtime (compileOnly above); tests run without Paper,
    // so they need it on the test classpath to render feedback components.
    "testImplementation"(libs.adventure.minimessage)
    "testRuntimeOnly"(libs.junit.platform.launcher)
}

// Keep the plugin descriptor version in sync with the project version.
tasks.withType<ProcessResources>().configureEach {
    val tokens = mapOf("version" to project.version.toString())
    inputs.properties(tokens)
    filesMatching("paper-plugin.yml") {
        expand(tokens)
    }
}

// The deployable plugin jar bundles tablist-core and its non-provided runtime
// dependencies (Caffeine, Configurate + geantyref + net.kyori:option, and the
// CommandFramework). Adventure and paper-api come from the Paper runtime.
//
// Bundled data libraries are RELOCATED under `com.hanielfialho.tablist.libs` so
// that another plugin shipping a different Caffeine/Configurate version cannot
// clash with ours on the shared server classloader. Relocation rewrites every
// reference across the whole shaded jar (including the CommandFramework's own use
// of Caffeine), so it is enough to list each library once here.
//
// The CommandFramework and its ClassGraph scanner are deliberately NOT relocated:
// the framework scans command classes by package name at runtime, and rewriting
// its packages risks breaking that discovery for a conflict that is far less
// likely than two plugins shading Configurate.
val libsPrefix = "com.hanielfialho.tablist.libs"

tasks.named<ShadowJar>("shadowJar") {
    // Distinct "-all" classifier so it does not clash with the thin `jar`.
    archiveClassifier.set("all")

    // Adventure is provided by Paper, so it must stay un-relocated and un-bundled.
    // Only the adventure-* artifacts are excluded; net.kyori:option is a Configurate
    // dependency that Paper does NOT provide, so it is kept (and relocated below).
    dependencies {
        exclude(dependency("net.kyori:adventure-.*:.*"))
        // Compile-time-only annotations; never needed on the runtime classpath.
        exclude(dependency("org.jspecify:jspecify:.*"))
        exclude(dependency("com.google.errorprone:error_prone_annotations:.*"))
    }

    relocate("com.github.benmanes.caffeine", "$libsPrefix.caffeine")
    relocate("org.spongepowered.configurate", "$libsPrefix.configurate")
    relocate("io.leangen.geantyref", "$libsPrefix.geantyref")
    relocate("net.kyori.option", "$libsPrefix.option")

    // Configurate registers TypeSerializers via the ServiceLoader; merge (and let
    // shadow rewrite) the META-INF/services files so the relocated classes resolve.
    mergeServiceFiles()

    // Drop signatures (invalid after merging) and redundant build metadata.
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    exclude("META-INF/maven/**")
    exclude("module-info.class", "META-INF/versions/*/module-info.class")
}

// Make `build` produce the bundled jar.
tasks.named("assemble") {
    dependsOn("shadowJar")
}
