rootProject.name = "tablist"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
        maven {
            // PlaceholderAPI (compile-only, soft dependency).
            name = "extendedclip"
            url = uri("https://repo.extendedclip.com/releases/")
        }
    }
}

include("tablist-core", "tablist-paper")
