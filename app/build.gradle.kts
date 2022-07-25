plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("plugin.parcelize")
  kotlin("kapt")
  id("dagger.hilt.android.plugin")
  kotlin("plugin.serialization")
  id("com.squareup.sqldelight")
  id("com.mikepenz.aboutlibraries.plugin")
}

android {
  namespace = "io.github.alessandrojean.toshokan"
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
      "-opt-in=coil.annotation.ExperimentalCoilApi",
    )
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = compose.versions.compiler.get()
  }

  packagingOptions {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  sqldelight {
    database("ToshokanDatabase") {
      packageName = "io.github.alessandrojean.toshokan.database"
    }
  }
}

dependencies {
  // Compose
  implementation(compose.activity)
  implementation(compose.material3.core)
  implementation(compose.material3.adapter)
  implementation(compose.material.core)
  implementation(compose.material.icons)
  implementation(compose.accompanist.flowLayout)
  implementation(compose.accompanist.pager)
  implementation(compose.accompanist.pagerIndicators)
  implementation(compose.accompanist.permissions)
  implementation(compose.accompanist.placeholder)
  implementation(compose.accompanist.swipeRefresh)
  implementation(compose.accompanist.systemUiController)
  debugImplementation(compose.ui.tooling)
  debugImplementation(compose.ui.toolingPreview)

  // AndroidX libraries
  implementation(androidx.appCompat)
  implementation(androidx.coreKtx)
  implementation(androidx.dataStore)
  implementation(androidx.paging.runtimeKtx)
  implementation(androidx.paging.compose)
  implementation(androidx.paletteKtx)
  implementation(androidx.splashScreen)
  implementation(androidx.bundles.camera)
  implementation(androidx.bundles.lifecycle)

  // Desugaring
  coreLibraryDesugaring(libs.desugarJdk)

  // KotlinX libraries
  implementation(kotlinx.bundles.coroutines)
  implementation(kotlinx.bundles.serialization)

  // Other libraries
  implementation(libs.aboutLibraries.core)
  implementation(libs.aboutLibraries.compose)
  implementation(libs.coilCompose)
  implementation(libs.composeReorderable)
  implementation(libs.jsoup)
  implementation(libs.logcat)
  implementation(libs.mlkit.barcodeScanning)
  implementation(libs.okio)
  implementation(libs.sqldelight.android.driver)
  implementation(libs.sqldelight.coroutines)
  implementation(libs.sqldelight.android.paging)
  implementation(libs.subsamplingImageView)
  implementation(libs.bundles.ktor)
  implementation(libs.bundles.okhttp)
  implementation(libs.bundles.vico)
  implementation(libs.bundles.voyager)

  implementation(libs.hilt.android)
  kapt(libs.hilt.compiler)

  // Test
  // testImplementation("junit:junit:4.+")
  // androidTestImplementation("androidx.test.ext:junit:1.1.3")
  // androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

kapt {
  correctErrorTypes = true
}

buildscript {
  dependencies {
    classpath(kotlinx.gradle)
  }
}
