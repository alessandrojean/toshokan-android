
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
  dependencies {
    classpath(libs.android.tools.r8)
    classpath(libs.hilt.gradle)
    classpath(libs.sqldelight.gradle)
    classpath(libs.aboutLibraries.gradle)
    classpath(kotlinx.serialization.gradle)
  }
}

plugins {
  alias(androidx.plugins.application) apply false
  alias(androidx.plugins.library) apply false
  alias(kotlinx.plugins.android) apply false
}

tasks.register<Delete>("clean") {
  delete(rootProject.buildDir)
}
