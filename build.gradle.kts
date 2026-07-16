// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false    //hilt 사용하기 위한 의존성 추가
    alias(libs.plugins.ksp) apply false
}
