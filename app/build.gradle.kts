// Module level file
plugins {
    alias(libs.plugins.android.application)
    id("com.chaquo.python")
}

android {
    namespace = "com.example.gatoapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gatoapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // snake things
    flavorDimensions += "pyVersion"
    productFlavors {
        create("py310") { dimension = "pyVersion" }
        create("py311") { dimension = "pyVersion" }
        create("py312") { dimension = "pyVersion" }

    }
}
chaquopy {
    defaultConfig {
        version = "3.12"
        pip {
      //      install("contourpy==1.3.0")
            install("cycler==0.12.1")
            install("daltonlens==0.1.5")
            install("fonttools==4.54.1")
            install("imageio==2.36.0")
            install("kiwisolver")
            install("lazy_loader==0.4")
            //install("matplotlib==3.9.2")
            install("networkx==3.4.2")
            install("numpy==1.23.3")
            install("packaging==24.1")
            install("pillow==11.0.0")
            install("pyparsing==3.2.0")
            install("python-dateutil==2.9.0.post0")
            install("scikit-image")
            install("scipy")
            install("six==1.16.0")
            install("tifffile==2024.9.20")
        }
    }
    productFlavors {
        productFlavors {
            getByName("py310") { version = "3.10" }
            getByName("py311") { version = "3.11" }
            getByName("py312") { version = "3.12" }
        }
    }
    sourceSets {
    }
}
dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}