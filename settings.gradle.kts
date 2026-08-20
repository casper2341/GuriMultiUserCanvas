pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GuriMultiUserCanvas"
include(":app")
include(":feature:auth")
include(":feature:canvas")
include(":feature:room")
include(":core:ui")
include(":core:data")
include(":core:model")
include(":core:common")
include(":core:network")
