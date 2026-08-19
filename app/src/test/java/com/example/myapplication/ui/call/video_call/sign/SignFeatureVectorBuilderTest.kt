package com.example.myapplication.ui.call.video_call.sign

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignFeatureVectorBuilderTest {

    private val gson = Gson()

    @Test
    fun `골든 프레임과 동일한 258개 특징을 생성한다`() {
        val fixture = loadGoldenFixture()

        val actual = SignFeatureVectorBuilder.build(
            pose = fixture.rawLandmarks.pose.map {
                it.toSignLandmark()
            },
            leftHand = fixture.rawLandmarks.leftHand.map {
                it.toSignLandmark()
            },
            rightHand = fixture.rawLandmarks.rightHand.map {
                it.toSignLandmark()
            }
        )

        assertEquals(
            SignFeatureVectorBuilder.FEATURE_COUNT,
            actual.size
        )
        assertEquals(
            SignFeatureVectorBuilder.FEATURE_COUNT,
            fixture.expectedFeatures.size
        )

        actual.forEachIndexed { index, value ->
            assertTrue(
                "feature[$index] must be finite",
                value.isFinite()
            )
            assertEquals(
                "feature[$index]",
                fixture.expectedFeatures[index],
                value,
                FEATURE_TOLERANCE
            )
        }
    }

    @Test
    fun `크기가 맞지 않는 그룹은 전체를 0으로 채운다`() {
        val fixture = loadGoldenFixture()

        val actual = SignFeatureVectorBuilder.build(
            pose = fixture.rawLandmarks.pose.dropLast(1).map {
                it.toSignLandmark()
            },
            leftHand = fixture.rawLandmarks.leftHand.map {
                it.toSignLandmark()
            },
            rightHand = fixture.rawLandmarks.rightHand.dropLast(1).map {
                it.toSignLandmark()
            }
        )

        assertTrue(actual.sliceArray(0 until 132).all { it == 0f })
        assertTrue(actual.sliceArray(195 until 258).all { it == 0f })

        val expectedRawLeftHand = fixture.rawLandmarks.leftHand
            .flatMap { listOf(it.x, it.y, it.z) }

        expectedRawLeftHand.forEachIndexed { index, expected ->
            assertEquals(
                "leftHand[$index]",
                expected,
                actual[132 + index],
                FEATURE_TOLERANCE
            )
        }
    }

    private fun loadGoldenFixture(): GoldenFixture {
        val stream = checkNotNull(
            javaClass.classLoader
                ?.getResourceAsStream(GOLDEN_FIXTURE_PATH)
        ) {
            "골든 fixture를 찾을 수 없습니다: $GOLDEN_FIXTURE_PATH"
        }

        return stream.bufferedReader(Charsets.UTF_8).use { reader ->
            gson.fromJson(reader, GoldenFixture::class.java)
        }
    }

    private fun FixtureLandmark.toSignLandmark(): SignLandmark =
        SignLandmark(
            x = x,
            y = y,
            z = z,
            visibility = visibility
        )

    private data class GoldenFixture(
        val rawLandmarks: RawLandmarks,
        val expectedFeatures: List<Float>
    )

    private data class RawLandmarks(
        val pose: List<FixtureLandmark>,
        val leftHand: List<FixtureLandmark>,
        val rightHand: List<FixtureLandmark>
    )

    private data class FixtureLandmark(
        val x: Float,
        val y: Float,
        val z: Float,
        val visibility: Float? = null
    )

    private companion object {
        const val GOLDEN_FIXTURE_PATH =
            "sign/android-golden-frame-032.json"
        const val FEATURE_TOLERANCE = 0.000001f
    }
}
