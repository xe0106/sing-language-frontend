package com.example.myapplication.network.call.webrtc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import com.example.myapplication.ui.call.video_call.sign.MediaPipeSignLandmarkExtractor
import com.example.myapplication.ui.call.video_call.sign.SignFeatureFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.webrtc.VideoFrame
import org.webrtc.VideoSink
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt

/**
 * WebRTC 로컬 트랙을 방해하지 않고 최대 15 FPS로 MediaPipe 분석을 수행한다.
 *
 * 분석이 진행 중이면 새 프레임을 즉시 버린다. 따라서 카메라 캡처 스레드에
 * 변환/추론 작업이 쌓이지 않는다.
 */
internal class WebRtcSignFrameSink(
    context: Context,
    private val onFeatures: (SignFeatureFrame) -> Unit,
    private val onFailure: (Throwable) -> Unit
) : VideoSink {

    private val applicationContext = context.applicationContext
    private val executor =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "SignLandmarkThread")
        }
    private val closed = AtomicBoolean(false)
    private val processing = AtomicBoolean(false)
    private val analysisFailed = AtomicBoolean(false)
    private val lastAcceptedAtNs = AtomicLong(Long.MIN_VALUE)

    // 이 필드는 executor의 단일 스레드에서만 읽고 쓴다.
    private var extractor: MediaPipeSignLandmarkExtractor? = null
    private var lastMediaPipeTimestampMs = Long.MIN_VALUE

    override fun onFrame(frame: VideoFrame) {
        if (closed.get() || analysisFailed.get() || !shouldAcceptFrame()) {
            return
        }
        if (!processing.compareAndSet(false, true)) {
            return
        }

        frame.retain()
        val capturedAtEpochMs = System.currentTimeMillis()

        runCatching {
            executor.execute {
                try {
                    analyzeFrame(
                        frame = frame,
                        capturedAtEpochMs = capturedAtEpochMs
                    )
                } catch (throwable: Throwable) {
                    reportFatalFailure(throwable)
                } finally {
                    frame.release()
                    processing.set(false)
                }
            }
        }.onFailure { throwable ->
            frame.release()
            processing.set(false)
            if (!closed.get()) {
                reportFatalFailure(throwable)
            }
        }
    }

    /**
     * 모델 초기화나 추론이 실패하면 같은 오류를 카메라 프레임마다 반복하지 않는다.
     * 다음 통화에서 새 sink가 만들어지면 분석을 다시 시도한다.
     */
    private fun reportFatalFailure(throwable: Throwable) {
        if (analysisFailed.compareAndSet(false, true)) {
            onFailure(throwable)
        }
    }

    private fun shouldAcceptFrame(): Boolean {
        val nowNs = SystemClock.elapsedRealtimeNanos()

        while (true) {
            val previousNs = lastAcceptedAtNs.get()
            if (
                previousNs != Long.MIN_VALUE &&
                nowNs - previousNs < FRAME_INTERVAL_NS
            ) {
                return false
            }
            if (lastAcceptedAtNs.compareAndSet(previousNs, nowNs)) {
                return true
            }
        }
    }

    private fun analyzeFrame(
        frame: VideoFrame,
        capturedAtEpochMs: Long
    ) {
        if (closed.get()) {
            return
        }

        // MediaPipe의 rotation option은 추론 전에 이미지를 회전하지만 결과
        // landmark 좌표는 회전 전 입력 프레임 좌표계로 반환한다. 학습 영상처럼
        // 똑바른 좌표계를 얻기 위해 픽셀 자체를 먼저 회전하고 rotation=0으로
        // 분석한다. 전면 카메라 좌우 반전은 미리보기에만 적용하고 여기서는 하지 않는다.
        val bitmap = frame.toUprightAnalysisBitmap()
        val mediaPipeTimestampMs = nextMediaPipeTimestampMs()

        try {
            val currentExtractor = extractor
                ?: MediaPipeSignLandmarkExtractor(
                    applicationContext
                ).also { extractor = it }

            onFeatures(
                currentExtractor.extract(
                    bitmap = bitmap,
                    rotationDegrees = 0,
                    mediaPipeTimestampMs = mediaPipeTimestampMs,
                    capturedAtEpochMs = capturedAtEpochMs
                )
            )
        } finally {
            bitmap.recycle()
        }
    }

    private fun nextMediaPipeTimestampMs(): Long {
        val elapsedMs = SystemClock.elapsedRealtime()
        val timestampMs = maxOf(
            elapsedMs,
            lastMediaPipeTimestampMs + 1L
        )
        lastMediaPipeTimestampMs = timestampMs
        return timestampMs
    }

    suspend fun close() {
        if (!closed.compareAndSet(false, true)) {
            return
        }

        executor.execute {
            runCatching {
                extractor?.close()
            }
            extractor = null
        }
        executor.shutdown()

        withContext(Dispatchers.IO) {
            executor.awaitTermination(
                CLOSE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
        }
    }

    private fun VideoFrame.toAnalysisBitmap(): Bitmap {
        val sourceWidth = buffer.width
        val sourceHeight = buffer.height
        val dimensions = analysisDimensions(
            width = sourceWidth,
            height = sourceHeight
        )
        val analysisBuffer = buffer.cropAndScale(
            0,
            0,
            sourceWidth,
            sourceHeight,
            dimensions.width,
            dimensions.height
        )

        try {
            val i420Buffer = checkNotNull(
                analysisBuffer.toI420()
            ) {
                "WebRTC 프레임을 I420으로 변환하지 못했습니다."
            }

            return try {
                i420Buffer.toArgbBitmap()
            } finally {
                i420Buffer.release()
            }
        } finally {
            analysisBuffer.release()
        }
    }

    private fun VideoFrame.toUprightAnalysisBitmap(): Bitmap {
        val source = toAnalysisBitmap()
        val clockwiseRotation = rotation
            .mod(FULL_ROTATION_DEGREES)

        if (clockwiseRotation == 0) {
            return source
        }

        return try {
            Bitmap.createBitmap(
                source,
                0,
                0,
                source.width,
                source.height,
                Matrix().apply {
                    postRotate(clockwiseRotation.toFloat())
                },
                false
            )
        } finally {
            source.recycle()
        }
    }

    private fun VideoFrame.I420Buffer.toArgbBitmap(): Bitmap {
        val pixels = IntArray(width * height)
        val yPlane = dataY.duplicate()
        val uPlane = dataU.duplicate()
        val vPlane = dataV.duplicate()

        var pixelIndex = 0
        for (row in 0 until height) {
            val yRowOffset = row * strideY
            val uvRowOffset = (row / 2) * strideU
            val vRowOffset = (row / 2) * strideV

            for (column in 0 until width) {
                val y = yPlane.unsignedByteAt(
                    yRowOffset + column
                )
                val u = uPlane.unsignedByteAt(
                    uvRowOffset + column / 2
                )
                val v = vPlane.unsignedByteAt(
                    vRowOffset + column / 2
                )

                pixels[pixelIndex++] = yuvToArgb(y, u, v)
            }
        }

        return Bitmap.createBitmap(
            pixels,
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
    }

    private fun ByteBuffer.unsignedByteAt(index: Int): Int =
        get(index).toInt() and 0xff

    private fun yuvToArgb(y: Int, u: Int, v: Int): Int {
        val adjustedY = (y - 16).coerceAtLeast(0)
        val adjustedU = u - 128
        val adjustedV = v - 128

        val red = (
            298 * adjustedY + 409 * adjustedV + 128
        ).shr(8).coerceIn(0, 255)
        val green = (
            298 * adjustedY - 100 * adjustedU -
                208 * adjustedV + 128
        ).shr(8).coerceIn(0, 255)
        val blue = (
            298 * adjustedY + 516 * adjustedU + 128
        ).shr(8).coerceIn(0, 255)

        return (0xff shl 24) or
            (red shl 16) or
            (green shl 8) or
            blue
    }

    private fun analysisDimensions(
        width: Int,
        height: Int
    ): AnalysisDimensions {
        val longestEdge = maxOf(width, height)
        if (longestEdge <= MAX_ANALYSIS_EDGE) {
            return AnalysisDimensions(
                width = width.toEven(),
                height = height.toEven()
            )
        }

        val scale = MAX_ANALYSIS_EDGE.toFloat() / longestEdge
        return AnalysisDimensions(
            width = (width * scale).roundToInt().toEven(),
            height = (height * scale).roundToInt().toEven()
        )
    }

    private fun Int.toEven(): Int =
        (this - this % 2).coerceAtLeast(2)

    private data class AnalysisDimensions(
        val width: Int,
        val height: Int
    )

    private companion object {
        const val TARGET_FPS = 15L
        const val FRAME_INTERVAL_NS =
            1_000_000_000L / TARGET_FPS
        // AI는 발화별 30프레임이 쌓여야 추론한다. 실기기에서 960px은 약
        // 6 FPS까지 떨어져 5초 안에도 30프레임에 도달하지 못했으므로,
        // 서버 권장 속도인 10~15 FPS를 확보할 수 있는 크기로 유지한다.
        const val MAX_ANALYSIS_EDGE = 640
        const val FULL_ROTATION_DEGREES = 360
        const val CLOSE_TIMEOUT_SECONDS = 5L
    }
}
