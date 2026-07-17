package com.example.data

import com.example.BuildConfig
import com.example.network.Content
import com.example.network.GeminiApiService
import com.example.network.GeminiRequest
import com.example.network.GenerationConfig
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import retrofit2.HttpException

class GeminiRepositoryImpl(
    private val apiService: GeminiApiService = RetrofitClient.geminiService
) : GeminiRepository {

    override suspend fun enhanceDevotionalPrompt(inputPrompt: String, style: String): GeminiResult<String> = withContext(Dispatchers.IO) {
        val cacheKey = "enhance_${style}_${inputPrompt.trim()}"
        GeminiConfig.Cache.get(cacheKey)?.let { cachedResponse ->
            android.util.Log.d("GeminiRepo", "Cache hit for enhanced prompt key: $cacheKey")
            return@withContext GeminiResult.Success(cachedResponse)
        }

        val systemInstruction = GeminiConfig.SYSTEM_INSTRUCTION_ENHANCER
        val prompt = "Enhance this prompt: '$inputPrompt' using style: '$style'."
        
        when (val response = generateDevotionalText(prompt, systemInstruction)) {
            is GeminiResult.Success -> {
                GeminiConfig.Cache.put(cacheKey, response.data)
                GeminiResult.Success(response.data)
            }
            is GeminiResult.Error -> response
        }
    }

    override suspend fun generateDevotionalText(prompt: String, systemInstruction: String?): GeminiResult<String> = withContext(Dispatchers.IO) {
        val cacheKey = "text_${systemInstruction.hashCode()}_${prompt.trim()}"
        GeminiConfig.Cache.get(cacheKey)?.let { cachedResponse ->
            android.util.Log.d("GeminiRepo", "Cache hit for text key: $cacheKey")
            return@withContext GeminiResult.Success(cachedResponse)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiResult.Error("API Key is missing or default. Please configure it in the Secrets panel.")
        }

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = systemInstruction?.let {
                Content(parts = listOf(Part(text = it)))
            }
        )

        try {
            val responseText = runWithExponentialBackoff(times = 3, initialDelayMillis = 1000) {
                val response = apiService.generateContent(GeminiConfig.activeModel, apiKey, request)
                val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (resultText.isNullOrBlank()) {
                    throw IllegalStateException("Empty response. No divine text was received.")
                }
                resultText
            }
            GeminiConfig.Cache.put(cacheKey, responseText)
            GeminiResult.Success(responseText)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun generateDevotionalImage(prompt: String, aspectRatio: String): GeminiResult<String> {
        return GeminiResult.Error("Image generation is reserved for Phase 2.")
    }

    /**
     * Executes the given network request block with exponential backoff on recoverable exceptions.
     * Retries are cancelled if the parent CoroutineScope is cancelled.
     */
    private suspend fun <T> runWithExponentialBackoff(
        times: Int = 3,
        initialDelayMillis: Long = 1000,
        maxDelayMillis: Long = 6000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMillis
        repeat(times - 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                // Critical: Coroutine cancellation must bypass retry and propagate immediately.
                if (e is kotlinx.coroutines.CancellationException) throw e
                
                if (!isRecoverable(e)) {
                    throw e
                }
                android.util.Log.w("GeminiRepo", "Attempt ${attempt + 1} failed with error: ${e.message}. Retrying in ${currentDelay}ms...")
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
        }
        return block() // Final attempt
    }

    private fun isRecoverable(e: Throwable): Boolean {
        return when (e) {
            is IOException -> true // Connection errors, timeouts, etc.
            is HttpException -> {
                val code = e.code()
                code == 429 || code >= 500 // Rate limit error (429) or Server error (5xx)
            }
            else -> false
        }
    }

    private fun handleException(e: Throwable): GeminiResult.Error {
        e.printStackTrace()
        return when (e) {
            is java.net.UnknownHostException, is java.net.ConnectException -> {
                GeminiResult.Error("No internet connection. Please check your network and try again.", e)
            }
            is java.net.SocketTimeoutException, is java.io.InterruptedIOException -> {
                GeminiResult.Error("Request timed out. Please retry.", e)
            }
            is HttpException -> {
                val code = e.code()
                val msg = when (code) {
                    401, 403 -> "Authentication failed. Invalid API Key. Please verify your credentials in the Secrets panel."
                    429 -> "Rate limit exceeded. Too many requests. Please try again after some time."
                    500, 503 -> "Server error on Gemini. Please try again later."
                    else -> "HTTP error $code: ${e.message()}"
                }
                GeminiResult.Error(msg, e)
            }
            is com.squareup.moshi.JsonDataException -> {
                GeminiResult.Error("Invalid response format received from the server.", e)
            }
            else -> {
                GeminiResult.Error("An unexpected error occurred: ${e.localizedMessage ?: "Unknown error"}", e)
            }
        }
    }
}
