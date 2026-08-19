package com.example.myapplication.ui.call.video_call.sign

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.holisticlandmarker.HolisticLandmarker

/**
 * 백엔드 학습 전처리와 동일한 Holistic VIDEO 설정으로 특징을 추출한다.
 *
 * 월드 좌표와 얼굴 랜드마크는 사용하지 않는다. 호출과 [close]는 반드시 같은
 * 단일 작업 스레드에서 순차적으로 수행해야 한다.
 */
class MediaPipeSignLandmarkExtractor(
    context: Context
) : AutoCloseable {

    private val holisticLandmarker =
        HolisticLandmarker.createFromOptions(
            context,
            HolisticLandmarker.HolisticLandmarkerOptions
                .builder()
                .setBaseOptions(
                    BaseOptions.builder()
                        .setModelAssetPath(MODEL_ASSET_PATH)
                        .build()
                )
                .setRunningMode(RunningMode.VIDEO)
                .setMinFaceDetectionConfidence(MIN_CONFIDENCE)
                .setMinFacePresenceConfidence(MIN_CONFIDENCE)
                .setMinHandLandmarksConfidence(MIN_CONFIDENCE)
                .setMinPoseDetectionConfidence(MIN_CONFIDENCE)
                .setMinPosePresenceConfidence(MIN_CONFIDENCE)
                .setOutputFaceBlendshapes(false)
                .setOutputPoseSegmentationMasks(false)
                .build()
        )

    fun extract(
        bitmap: Bitmap,
        rotationDegrees: Int,
        mediaPipeTimestampMs: Long,
        capturedAtEpochMs: Long
    ): SignFeatureFrame {
        require(bitmap.config == Bitmap.Config.ARGB_8888) {
            "MediaPipe 입력 Bitmap은 ARGB_8888이어야 합니다."
        }

        val image = BitmapImageBuilder(bitmap).build()
        val imageProcessingOptions =
            ImageProcessingOptions.builder()
                .setRotationDegrees(rotationDegrees)
                .build()

        return try {
            val result = holisticLandmarker.detectForVideo(
                image,
                imageProcessingOptions,
                mediaPipeTimestampMs
            )

            val pose = result.poseLandmarks()
                .takeIf { it.size == SignFeatureVectorBuilder.POSE_LANDMARK_COUNT }
                ?.map { it.toSignLandmark() }
            val leftHand = result.leftHandLandmarks()
                .takeIf { it.size == SignFeatureVectorBuilder.HAND_LANDMARK_COUNT }
                ?.map { it.toSignLandmark() }
            val rightHand = result.rightHandLandmarks()
                .takeIf { it.size == SignFeatureVectorBuilder.HAND_LANDMARK_COUNT }
                ?.map { it.toSignLandmark() }

            SignFeatureFrame(
                timestampMs = capturedAtEpochMs,
                poseDetected = pose != null,
                leftHandDetected = leftHand != null,
                rightHandDetected = rightHand != null,
                features = SignFeatureVectorBuilder.build(
                    pose = pose,
                    leftHand = leftHand,
                    rightHand = rightHand
                )
            )
        } finally {
            image.close()
        }
    }

    override fun close() {
        holisticLandmarker.close()
    }

    private fun NormalizedLandmark.toSignLandmark(): SignLandmark =
        SignLandmark(
            x = x(),
            y = y(),
            z = z(),
            visibility = visibility().orElse(0f)
        )

    private companion object {
        const val MODEL_ASSET_PATH = "holistic_landmarker.task"
        const val MIN_CONFIDENCE = 0.5f
    }
}
