plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("plugin.parcelize")
  kotlin("kapt")
  id("dagger.hilt.android.plugin")
  kotlin("plugin.serialization") version "1.6.10"
  id("com.squareup.sqldelight")
}

android {
  compileSdk = 32

  defaultConfig {
    applicationId = "io.github.alessandrojean.toshokan"
    minSdk = 24
    targetSdk = 32
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    named("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
//    useIR = true
    freeCompilerArgs += listOf(
      "-opt-in=kotlin.RequiresOptIn",
      "-opt-in=kotlin.Experimental",
      "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
      "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
      "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
      "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
      "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
      "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
      "-opt-in=com.google.accompanist.pager.ExperimentalPagerApi"
    )
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String?
  }

  packagingOptions {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
 }
  namespace = "io.github.alessandrojean.toshokan"
}

dependencies {
  // Kotlin
  val coroutinesVersion = rootProject.extra["coroutines_version"]
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

  // Core
  implementation("androidx.core:core-ktx:1.7.0")
  implementation("androidx.appcompat:appcompat:1.4.1")

  // Lifecycle
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")

  // Test
  testImplementation("junit:junit:4.+")
  androidTestImplementation("androidx.test.ext:junit:1.1.3")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

  // Compose
  val composeVersion = rootProject.extra["compose_version"]
  implementation("androidx.activity:activity-compose:1.6.0-alpha03")
  implementation("androidx.compose.ui:ui:$composeVersion")
  implementation("androidx.compose.material3:material3:1.0.0-alpha12")
  implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
  implementation("androidx.compose.material:material-icons-extended:$composeVersion")
  androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
  debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")

  // Navigation (Voyager)
  val voyagerVersion = "1.0.0-rc02"
  implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
  implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
  implementation("cafe.adriel.voyager:voyager-androidx:$voyagerVersion")
  implementation("cafe.adriel.voyager:voyager-hilt:$voyagerVersion")

  // Material Design
  implementation("com.google.android.material:material:1.7.0-alpha02")
  implementation("com.google.android.material:compose-theme-adapter-3:1.0.10")

  // Accompanist
  val accompanistVersion = "0.24.9-beta"
  implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
  implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")
  implementation("com.google.accompanist:accompanist-pager:$accompanistVersion")
  implementation("com.google.accompanist:accompanist-pager-indicators:$accompanistVersion")
  implementation("com.google.accompanist:accompanist-swiperefresh:$accompanistVersion")

  // Androidx CameraX
  val cameraxVersion = "1.1.0-rc01"
  implementation("androidx.camera:camera-core:$cameraxVersion")
  implementation("androidx.camera:camera-camera2:$cameraxVersion")
  implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
  implementation("androidx.camera:camera-view:$cameraxVersion")

  // Google ML Kit
  implementation("com.google.mlkit:barcode-scanning:17.0.2")

  // Ktor
  val ktorVersion = "2.0.1"
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-android:$ktorVersion")
  implementation("io.ktor:ktor-client-logging:$ktorVersion")
  implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

  // Kotlinx.serialization
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

  // Jsoup
  implementation("org.jsoup:jsoup:1.15.1")

  // Lifecycle
  implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.4.1")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.0-rc01")

  // Hilt
  val hiltVersion = rootProject.extra["hilt_version"]
  implementation("com.google.dagger:hilt-android:$hiltVersion")
  kapt("com.google.dagger:hilt-compiler:$hiltVersion")

  // Coil
  implementation("io.coil-kt:coil-compose:2.1.0")

  // SQLDelight
  val sqlDelightVersion = rootProject.extra["sqldelight_version"]
  implementation("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
  implementation("com.squareup.sqldelight:coroutines-extensions-jvm:$sqlDelightVersion")

  // Compose Reorderable
  implementation("org.burnoutcrew.composereorderable:reorderable:0.8.1")

  // Logcat
  implementation("com.squareup.logcat:logcat:0.1")
}

kapt {
  correctErrorTypes = true
}

sqldelight {
  database("ToshokanDatabase") {
    packageName = "io.github.alessandrojean.toshokan.database"
  }
}
