package com.example.data

sealed class GeminiResult<out T> {
    data class Success<out T>(val data: T) : GeminiResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : GeminiResult<Nothing>()
}

interface GeminiRepository {
    /**
     * Enhances a basic user prompt into a high-fidelity devotional prompt suitable for design.
     */
    suspend fun enhanceDevotionalPrompt(inputPrompt: String, style: String): GeminiResult<String>

    /**
     * Generates creative devotional text, quotes, couplets, or lyrics.
     */
    suspend fun generateDevotionalText(prompt: String, systemInstruction: String? = null): GeminiResult<String>

    /**
     * Future-compatible API for devotional image generation (Phase 2 placeholder).
     */
    suspend fun generateDevotionalImage(prompt: String, aspectRatio: String): GeminiResult<String>
}
