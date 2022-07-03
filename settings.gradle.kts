pluginManagement {
  resolutionStrategy {
    eachPlugin {
      val regex = "com.android.(library|application)".toRegex()
      if (regex matches requested.id.id) {
        useModule("com.android.tools.build:gradle:${requested.version}")
      }
    }
  }
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  versionCatalogs {
    create("androidx") {
      from(files("gradle/androidx.versions.toml"))
    }
    create("compose") {
      from(files("gradle/compose.versions.toml"))
    }
    create("kotlinx") {
      from(files("gradle/kotlinx.versions.toml"))
    }
  }
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
  }
}

rootProject.name = "Toshokan"
include(":app")
