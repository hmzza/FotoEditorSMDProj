pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter() // Note: jcenter() is at end of life, consider removing it if all dependencies are available elsewhere
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "FotoEditorSMDProj"
include(":app")
 