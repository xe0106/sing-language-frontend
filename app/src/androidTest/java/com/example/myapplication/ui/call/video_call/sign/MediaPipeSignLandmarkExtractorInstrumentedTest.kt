package com.example.myapplication.ui.call.video_call.sign

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaPipeSignLandmarkExtractorInstrumentedTest {

    @Test
    fun holisticModelInitializesFromAppAssets() {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        MediaPipeSignLandmarkExtractor(context).use { extractor ->
            checkNotNull(extractor)
        }
    }

    @Test
    fun holisticModelProcessesArgbFrameAs258FiniteFeatures() {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext
        val bitmap = Bitmap.createBitmap(
            640,
            480,
            Bitmap.Config.ARGB_8888
        ).apply {
            eraseColor(Color.BLACK)
        }

        try {
            MediaPipeSignLandmarkExtractor(context).use { extractor ->
                val frame = extractor.extract(
                    bitmap = bitmap,
                    rotationDegrees = 0,
                    mediaPipeTimestampMs = 1L,
                    capturedAtEpochMs = 1_723_100_000_123L
                )

                assertEquals(
                    SignFeatureVectorBuilder.FEATURE_COUNT,
                    frame.features.size
                )
                assertTrue(frame.features.all(Float::isFinite))
            }
        } finally {
            bitmap.recycle()
        }
    }
}
