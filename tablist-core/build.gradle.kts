description = "Platform-agnostic domain, contracts and logic for Tablist (no Bukkit/Paper)."

// Shared build conventions (Java toolchain, Spotless, Javadoc, tests) are
// applied from the root build script's `subprojects` block. Because the
// `java-library` plugin is applied there rather than in this script's own
// `plugins {}` block, dependency configurations are referenced by name.
dependencies {
    "api"(libs.caffeine)
    "api"(libs.configurate.yaml)

    // Adventure is a platform-agnostic text library (net.kyori), not Bukkit/Paper:
    // the resolution pipeline parses MiniMessage into Components here in the core.
    // `Component` is part of the public API (api); MiniMessage is an internal detail.
    "api"(libs.adventure.api)
    "implementation"(libs.adventure.minimessage)

    "testImplementation"(platform(libs.junit.bom))
    "testImplementation"(libs.junit.jupiter)
    "testImplementation"(libs.mockito.core)
    "testImplementation"(libs.mockito.junit.jupiter)
    "testRuntimeOnly"(libs.junit.platform.launcher)
}
