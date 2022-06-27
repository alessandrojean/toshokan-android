plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("plugin.parcelize")
  kotlin("kapt")
  id("dagger.hilt.android.plugin")
  kotlin("plugin.serialization") version "1.6.10"
  id("com.squareup.sqldelight")
  id("com.mikepenz.aboutlibraries.plugin")
}

android {
  compileSdk = AndroidConfig.compileSdk

  defaultConfig {
    applicationId = "io.github.alessandrojean.toshokan"
    minSdk = AndroidConfig.minSdk
    targetSdk = AndroidConfig.targetSdk
    versionCode = 1
    versionName = "1.0.0"

    buildConfigField("String", "COMMIT_COUNT", "\"${getCommitCount()}\"")
    buildConfigField("String", "COMMIT_SHA", "\"${getGitSha()}\"")
    buildConfigField("String", "BUILD_TIME", "\"${getBuildTime()}\"")
    buildConfigField("boolean", "INCLUDE_UPDATER", "false")
    buildConfigField("boolean", "PREVIEW", "false")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    named("debug") {
      versionNameSuffix = "-${getCommitCount()}"
      applicationIdSuffix = ".debug"
    }

    named("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }

    create("preview") {
      initWith(getByName("release"))
      buildConfigField("boolean", "PREVIEW", "true")

      val debugType = getByName("debug")
      signingConfig = debugType.signingConfig
      versionNameSuffix = debugType.versionNameSuffix
      applicationIdSuffix = debugType.applicationIdSuffix
    }
  }

  sourceSets {
    getByName("preview").res.srcDirs("src/debug/res")
  }

  flavorDimensions.add("default")

  productFlavors {
    create("standard") {
      buildConfigField("boolean", "INCLUDE_UPDATER", "true")
      dimension = "default"
    }
    create("dev") {
      // Uncomment if you want the dev flavor to only include the English strings.
      // resourceConfigurations.addAll(listOf("en", "xxhdpi"))
      dimension = "default"
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    isCoreLibraryDesugaringEnabled = true
  }

  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
//    useIR = true
    freeCompilerArgs = freeCompilerArgs + listOf(
      "-opt-in=kotlin.RequiresOptIn",
      "-opt-in=kotlin.Experimental",
      "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
      "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
      "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
      "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
      "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
      "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
      "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
      "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
      "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
      "-opt-in=com.google.accompanist.pager.ExperimentalPagerApi",
      "-opt-in=coil.annotation.ExperimentalCoilApi"
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
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:$coroutinesVersion")

  // Core
  implementation("androidx.core:core-ktx:1.8.0")
  implementation("androidx.core:core-splashscreen:1.0.0-rc01")
  implementation("androidx.appcompat:appcompat:1.4.2")
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

  // Lifecycle
  val lifecycleVersion = "2.5.0-rc02"
  implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

  // Test
  testImplementation("junit:junit:4.+")
  androidTestImplementation("androidx.test.ext:junit:1.1.3")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

  // Compose
  val composeVersion = rootProject.extra["compose_version"]
  implementation("androidx.activity:activity-compose:1.6.0-alpha05")
  implementation("androidx.compose.ui:ui:$composeVersion")
  implementation("androidx.compose.material3:material3:1.0.0-alpha13")
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
  implementation("com.google.android.material:compose-theme-adapter-3:1.0.13")

  // Accompanist
  val accompanistVersion = "0.24.12-rc"
  implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
  implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")
  implementation("com.google.accompanist:accompanist-pager:$accompanistVersion")
  implementation("com.google.accompanist:accompanist-pager-indicators:$accompanistVersion")
  implementation("com.google.accompanist:accompanist-swiperefresh:$accompanistVersion")
  implementation("com.google.accompanist:accompanist-flowlayout:$accompanistVersion")
  implementation("com.google.accompanist:accompanist-placeholder-material:$accompanistVersion")

  // Androidx CameraX
  val cameraxVersion = "1.1.0-rc01"
  implementation("androidx.camera:camera-core:$cameraxVersion")
  implementation("androidx.camera:camera-camera2:$cameraxVersion")
  implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
  implementation("androidx.camera:camera-view:$cameraxVersion")

  // Preferences
  implementation("androidx.preference:preference-ktx:1.2.0")
  implementation("com.fredporciuncula:flow-preferences:1.7.0")

  // Palette
  implementation("androidx.palette:palette-ktx:1.0.0")

  // Paging3
  implementation("androidx.paging:paging-runtime-ktx:3.1.1")
  implementation("androidx.paging:paging-compose:1.0.0-alpha15")

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
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.3")

  // Jsoup
  implementation("org.jsoup:jsoup:1.15.1")

  // Hilt
  val hiltVersion = rootProject.extra["hilt_version"]
  implementation("com.google.dagger:hilt-android:$hiltVersion")
  kapt("com.google.dagger:hilt-compiler:$hiltVersion")

  // Coil
  implementation("io.coil-kt:coil-compose:2.1.0")

  // OkHttp (used for Coil stuff)
  val okHttpVersion = "4.10.0"
  implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
  implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

  // SQLDelight
  val sqlDelightVersion = rootProject.extra["sqldelight_version"]
  implementation("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
  implementation("com.squareup.sqldelight:coroutines-extensions-jvm:$sqlDelightVersion")
  implementation("com.squareup.sqldelight:android-paging3-extensions:$sqlDelightVersion")

  // Compose Reorderable
  implementation("org.burnoutcrew.composereorderable:reorderable:0.8.1")

  // Logcat
  implementation("com.squareup.logcat:logcat:0.1")

  // Subsampling Scale Image View
  implementation("com.github.tachiyomiorg:subsampling-scale-image-view:846abe0")

  // Okio
  implementation("com.squareup.okio:okio:3.1.0")

  // About Libraries
  val aboutLibVersion = rootProject.extra["aboutlib_version"]
  implementation("com.mikepenz:aboutlibraries-core:$aboutLibVersion")
  implementation("com.mikepenz:aboutlibraries-compose:$aboutLibVersion")
}

kapt {
  correctErrorTypes = true
}

sqldelight {
  database("ToshokanDatabase") {
    packageName = "io.github.alessandrojean.toshokan.database"
  }
}
