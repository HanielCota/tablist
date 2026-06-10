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

// The deployable plugin jar bundles tablist-core, Caffeine and Configurate.
// Adventure and paper-api are provided by the Paper runtime, so they are kept out.
tasks.named<ShadowJar>("shadowJar") {
    // Distinct "-all" classifier so it does not clash with the thin `jar`.
    archiveClassifier.set("all")
    dependencies {
        exclude(dependency("net.kyori:.*:.*"))
    }
}

// Make `build` produce the bundled jar.
tasks.named("assemble") {
    dependsOn("shadowJar")
}
