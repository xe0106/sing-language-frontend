package com.example.myapplication.network.call.webrtc

import kotlinx.coroutines.suspendCancellableCoroutine
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun PeerConnection.createOfferAwait(
    constraints: MediaConstraints
): SessionDescription =
    createDescriptionAwait(
        operationName = "OFFER 생성"
    ) { observer ->
        createOffer(
            observer,
            constraints
        )
    }

internal suspend fun PeerConnection.createAnswerAwait(
    constraints: MediaConstraints
): SessionDescription =
    createDescriptionAwait(
        operationName = "ANSWER 생성"
    ) { observer ->
        createAnswer(
            observer,
            constraints
        )
    }

internal suspend fun PeerConnection.setLocalDescriptionAwait(
    description: SessionDescription
) {
    setDescriptionAwait(
        operationName = "로컬 SDP 적용"
    ) { observer ->
        setLocalDescription(
            observer,
            description
        )
    }
}

internal suspend fun PeerConnection.setRemoteDescriptionAwait(
    description: SessionDescription
) {
    setDescriptionAwait(
        operationName = "원격 SDP 적용"
    ) { observer ->
        setRemoteDescription(
            observer,
            description
        )
    }
}

private suspend fun createDescriptionAwait(
    operationName: String,
    operation: (SdpObserver) -> Unit
): SessionDescription =
    suspendCancellableCoroutine { continuation ->
        val observer = object : EmptySdpObserver() {

            override fun onCreateSuccess(
                description: SessionDescription?
            ) {
                if (!continuation.isActive) {
                    return
                }

                if (description == null) {
                    continuation.resumeWithException(
                        IllegalStateException(
                            "$operationName 결과가 없습니다."
                        )
                    )
                } else {
                    continuation.resume(description)
                }
            }

            override fun onCreateFailure(
                error: String?
            ) {
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        IllegalStateException(
                            "$operationName 실패: " +
                                    error.orEmpty()
                        )
                    )
                }
            }
        }

        runCatching {
            operation(observer)
        }.onFailure { throwable ->
            if (continuation.isActive) {
                continuation.resumeWithException(
                    throwable
                )
            }
        }
    }

private suspend fun setDescriptionAwait(
    operationName: String,
    operation: (SdpObserver) -> Unit
) {
    suspendCancellableCoroutine { continuation ->
        val observer = object : EmptySdpObserver() {

            override fun onSetSuccess() {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }

            override fun onSetFailure(
                error: String?
            ) {
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        IllegalStateException(
                            "$operationName 실패: " +
                                    error.orEmpty()
                        )
                    )
                }
            }
        }

        runCatching {
            operation(observer)
        }.onFailure { throwable ->
            if (continuation.isActive) {
                continuation.resumeWithException(
                    throwable
                )
            }
        }
    }
}

private open class EmptySdpObserver : SdpObserver {

    override fun onCreateSuccess(
        description: SessionDescription?
    ) = Unit

    override fun onSetSuccess() = Unit

    override fun onCreateFailure(
        error: String?
    ) = Unit

    override fun onSetFailure(
        error: String?
    ) = Unit
}