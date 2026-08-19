package com.example.myapplication.ui.call.video_call.sign

import kotlin.math.sqrt

/**
 * MediaPipe의 이미지 좌표 랜드마크를 AI 서버 입력 형식으로 변환한다.
 *
 * 출력 순서는 pose 33 * (x, y, z, visibility), left hand 21 * (x, y, z),
 * right hand 21 * (x, y, z)이며 항상 258개다.
 */
object SignFeatureVectorBuilder {
    const val FEATURE_COUNT = 258
    const val POSE_LANDMARK_COUNT = 33
    const val HAND_LANDMARK_COUNT = 21

    private const val POSE_FEATURE_COUNT = 132
    private const val HAND_FEATURE_COUNT = 63
    private const val LEFT_HAND_OFFSET = POSE_FEATURE_COUNT
    private const val RIGHT_HAND_OFFSET =
        LEFT_HAND_OFFSET + HAND_FEATURE_COUNT

    private const val LEFT_SHOULDER_INDEX = 11
    private const val RIGHT_SHOULDER_INDEX = 12
    private const val MIN_SCALE = 0.000001f

    fun build(
        pose: List<SignLandmark>?,
        leftHand: List<SignLandmark>?,
        rightHand: List<SignLandmark>?
    ): FloatArray {
        val features = FloatArray(FEATURE_COUNT)
        val validPose = pose.takeIfExpectedSize(
            POSE_LANDMARK_COUNT
        )
        val normalization = validPose?.toNormalization()

        if (validPose != null && normalization != null) {
            validPose.forEachIndexed { index, landmark ->
                val offset = index * POSE_COMPONENT_COUNT

                features[offset] = normalizeCoordinate(
                    value = landmark.x,
                    origin = normalization.originX,
                    scale = normalization.scale
                )
                features[offset + 1] = normalizeCoordinate(
                    value = landmark.y,
                    origin = normalization.originY,
                    scale = normalization.scale
                )
                features[offset + 2] = normalizeCoordinate(
                    value = landmark.z,
                    origin = normalization.originZ,
                    scale = normalization.scale
                )
                features[offset + 3] =
                    landmark.visibility.finiteOrZero()
            }
        }

        writeHandFeatures(
            target = features,
            offset = LEFT_HAND_OFFSET,
            landmarks = leftHand.takeIfExpectedSize(
                HAND_LANDMARK_COUNT
            ),
            normalization = normalization
        )
        writeHandFeatures(
            target = features,
            offset = RIGHT_HAND_OFFSET,
            landmarks = rightHand.takeIfExpectedSize(
                HAND_LANDMARK_COUNT
            ),
            normalization = normalization
        )

        return features
    }

    private fun writeHandFeatures(
        target: FloatArray,
        offset: Int,
        landmarks: List<SignLandmark>?,
        normalization: Normalization?
    ) {
        landmarks?.forEachIndexed { index, landmark ->
            val featureOffset = offset + index * HAND_COMPONENT_COUNT

            target[featureOffset] = normalizeIfAvailable(
                value = landmark.x,
                origin = normalization?.originX,
                scale = normalization?.scale
            )
            target[featureOffset + 1] = normalizeIfAvailable(
                value = landmark.y,
                origin = normalization?.originY,
                scale = normalization?.scale
            )
            target[featureOffset + 2] = normalizeIfAvailable(
                value = landmark.z,
                origin = normalization?.originZ,
                scale = normalization?.scale
            )
        }
    }

    private fun List<SignLandmark>?.takeIfExpectedSize(
        expectedSize: Int
    ): List<SignLandmark>? =
        this?.takeIf { it.size == expectedSize }

    private fun List<SignLandmark>.toNormalization(): Normalization {
        val leftShoulder = this[LEFT_SHOULDER_INDEX]
        val rightShoulder = this[RIGHT_SHOULDER_INDEX]

        val leftX = leftShoulder.x.finiteOrZero()
        val leftY = leftShoulder.y.finiteOrZero()
        val leftZ = leftShoulder.z.finiteOrZero()
        val rightX = rightShoulder.x.finiteOrZero()
        val rightY = rightShoulder.y.finiteOrZero()
        val rightZ = rightShoulder.z.finiteOrZero()

        val dx = leftX - rightX
        val dy = leftY - rightY
        val dz = leftZ - rightZ
        val measuredScale = sqrt(dx * dx + dy * dy + dz * dz)
        val scale = measuredScale.takeIf {
            it.isFinite() && it >= MIN_SCALE
        } ?: 1f

        return Normalization(
            originX = (leftX + rightX) / 2f,
            originY = (leftY + rightY) / 2f,
            originZ = (leftZ + rightZ) / 2f,
            scale = scale
        )
    }

    private fun normalizeIfAvailable(
        value: Float,
        origin: Float?,
        scale: Float?
    ): Float = if (origin == null || scale == null) {
        value.finiteOrZero()
    } else {
        normalizeCoordinate(
            value = value,
            origin = origin,
            scale = scale
        )
    }

    private fun normalizeCoordinate(
        value: Float,
        origin: Float,
        scale: Float
    ): Float = ((value - origin) / scale).finiteOrZero()

    private fun Float?.finiteOrZero(): Float =
        if (this != null && isFinite()) this else 0f

    private data class Normalization(
        val originX: Float,
        val originY: Float,
        val originZ: Float,
        val scale: Float
    )

    private const val POSE_COMPONENT_COUNT = 4
    private const val HAND_COMPONENT_COUNT = 3
}

data class SignLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float? = null
)
