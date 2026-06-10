import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding

plugins {
    // Applied (not configured) here so the plugins are on the build classpath
    // and can be applied to the modules that need them.
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.shadow) apply false
}

// Read catalog values here, where the generated `libs` accessor is in scope,
// and capture them so they can be used inside the `subprojects {}` block (where
// the receiver is the subproject and `libs` would not resolve).
val javaVersion = libs.versions.java.get().toInt()
val googleJavaFormatVersion = libs.versions.googleJavaFormat.get()

allprojects {
    group = "com.hanielfialho.tablist"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.diffplug.spotless")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
        withJavadocJar()
        withSourcesJar()
    }

    configure<SpotlessExtension> {
        // Enforce LF everywhere so the repository is consistent across platforms.
        lineEndings = LineEnding.UNIX
        java {
            googleJavaFormat(googleJavaFormatVersion)
            importOrder()
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(javaVersion)
        options.compilerArgs.add("-Xlint:all,-processing")
    }

    tasks.withType<Javadoc>().configureEach {
        (options as StandardJavadocDocletOptions).apply {
            encoding = "UTF-8"
            docEncoding = "UTF-8"
            charSet = "UTF-8"
            // Enforce well-formed Javadoc, but tolerate not-yet-documented members
            // while the skeleton is still being filled in.
            addBooleanOption("Xdoclint:all,-missing", true)
            addStringOption("Xmaxwarns", "0")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
