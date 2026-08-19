package com.example.myapplication.ui.call.video_call.sign

/**
 * 로컬 카메라 한 프레임에서 추출한 AI 입력 특징이다.
 *
 * [timestampMs]는 서버 전송 시 그대로 사용할 실제 촬영 시각(Unix epoch ms)이다.
 */
data class SignFeatureFrame(
    val timestampMs: Long,
    val poseDetected: Boolean,
    val leftHandDetected: Boolean,
    val rightHandDetected: Boolean,
    val features: FloatArray
) {
    val handDetected: Boolean
        get() = leftHandDetected || rightHandDetected
}
