package com.example.myapplication.network.call

data class StompFrame(
    val command: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String = ""
)

object StompFrameCodec {

    fun encode(
        frame: StompFrame
    ): String {
        return buildString {
            append(frame.command)
            append('\n')

            frame.headers.forEach { (key, value) ->
                append(key)
                append(':')
                append(value)
                append('\n')
            }

            append('\n')
            append(frame.body)
            append('\u0000')
        }
    }

    fun decode(
        message: String
    ): List<StompFrame> {
        return message
            .replace("\r\n","\n")
            .split('\u0000')
            .mapNotNull(::decodeSingleFrame)
    }

    private fun decodeSingleFrame(
        rawFrame: String
    ): StompFrame? {
        val content = rawFrame.trimStart('\n')

        if (content.isBlank()) return null

        val headerEndIndex = content.indexOf("\n\n")

        val headerSection =
            if (headerEndIndex >= 0) {
                content.substring(
                    startIndex = 0,
                    endIndex = headerEndIndex
                )
            } else {
                content
            }

        val body =
            if (headerEndIndex >= 0) {
                content.substring(
                    startIndex = headerEndIndex + 2
                )
            } else {
                ""
            }

        val headerLines = headerSection.lines()
        val command = headerLines.firstOrNull()
            ?.takeIf {it.isNotBlank()}
            ?: return null

        val headers = headerLines
            .drop(1)
            .mapNotNull { line ->
                val separatorIndex = line.indexOf(':')

                if(separatorIndex <= 0) {
                    null
                } else {
                    val key = line.substring(
                        startIndex = 0,
                        endIndex = separatorIndex
                    )
                    val value = line.substring(
                        startIndex = separatorIndex + 1
                    )

                    key to value
                }
            }
            .toMap()

        return StompFrame(
            command = command,
            headers = headers,
            body = body
        )
    }
}