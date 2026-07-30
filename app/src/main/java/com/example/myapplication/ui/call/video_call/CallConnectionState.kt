package com.example.myapplication.ui.call.video_call

enum class CallConnectionState {
    CALLING,       // 상대방에게 발신 중
    CONNECTING,    // 연결 준비 중
    CONNECTED,     // 연결 완료
    RECONNECTING,  // 연결 복구 중
    ENDED,         // 통화 종료
    FAILED         // 연결 실패
}