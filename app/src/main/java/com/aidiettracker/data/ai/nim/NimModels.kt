package com.aidiettracker.data.ai.nim

import com.google.gson.annotations.SerializedName

data class NimChatRequest(
    val model: String,
    val messages: List<NimMessage>,
    val temperature: Float = 0.4f,
    @SerializedName("max_tokens") val maxTokens: Int = 220
)

data class NimMessage(
    val role: String,
    val content: String
)

data class NimChatResponse(
    val choices: List<NimChoice>?
)

data class NimChoice(
    val message: NimMessage?
)
