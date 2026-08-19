plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)    //hilt 사용하기 위한 의존성 추가
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation("io.coil-kt.coil3:coil-compose:3.5.0")       //AsyncImage 의존성
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.5.0")
    implementation(libs.hilt.android)       //hilt 사용하기 위한 의존성 추가
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation("androidx.media3:media3-exoplayer:1.10.1")      //수어 강의 의존성
    implementation("androidx.media3:media3-ui:1.10.1")
    implementation(libs.retrofit)                       // retrofit 서버 통신
    implementation(libs.retrofit.converter.gson)        // JSON <-> 객체 변환
    implementation(libs.okhttp.logging.interceptor)     // 통신 로그 확인용

    implementation(libs.okhttp)
    implementation(libs.stream.webrtc.android)
    implementation(libs.mediapipe.tasks.vision) {
        // MediaPipe 0.10.35의 Android AAR은 full protobuf API로 컴파일되어 있지만
        // POM에는 javalite가 선언되어 있어 HolisticLandmarker 생성 시
        // Any.Builder.build() NoSuchMethodError가 발생한다.
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
    }
    implementation("com.google.protobuf:protobuf-java:4.26.1")
}
