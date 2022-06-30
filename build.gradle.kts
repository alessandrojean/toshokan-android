
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
  extra.apply {
    set("compose_version", "1.2.0-rc02")
    set("kotlin_version", "1.7.0")
    set("coroutines_version", "1.6.3")
    set("hilt_version", "2.42")
    set("sqldelight_version", "1.5.3")
    set("aboutlib_version", "10.3.1")
  }

  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }

  dependencies {
    classpath("com.android.tools.build:gradle:7.2.1")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${rootProject.extra["kotlin_version"]}")
    classpath("com.google.dagger:hilt-android-gradle-plugin:${rootProject.extra["hilt_version"]}")
    classpath("com.squareup.sqldelight:gradle-plugin:${rootProject.extra["sqldelight_version"]}")
    classpath("com.mikepenz.aboutlibraries.plugin:aboutlibraries-plugin:${rootProject.extra["aboutlib_version"]}")
  }
}

tasks.register<Delete>("clean") {
  delete(rootProject.buildDir)
}
