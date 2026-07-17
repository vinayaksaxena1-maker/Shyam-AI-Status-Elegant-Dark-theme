package com.example.data

import java.util.Collections
import java.util.LinkedHashMap

object GeminiConfig {
    /**
     * Recommended production-grade model selection.
     * Can be configured dynamically at runtime.
     */
    @Volatile
    var activeModel: String = "gemini-1.5-flash"

    /**
     * Centralized system instructions to maintain high-quality, professional,
     * and thematic devotional responses.
     */
    const val SYSTEM_INSTRUCTION_ENHANCER = 
        "You are an elite, premium Hindu devotional image prompt designer. " +
        "Take a user's simple prompt and enhance it into a highly detailed, cinematic visual description " +
        "for a graphic canvas. Keep it visually stunning, evoking deep spiritual peace, divine light, " +
        "saffron/golden glow, detailed traditional ornaments, and sacred atmosphere. Return only the enhanced description."

    const val SYSTEM_INSTRUCTION_CREATOR = 
        "You are a compassionate, deeply spiritual AI bhakti assistant. " +
        "Generate beautiful and inspiring Hindu devotional messages, quotes, couplets, or status lyrics based on the user's focus."

    /**
     * Thread-safe, size-limited in-memory cache for prompt enhancement and generated text.
     * Prevents duplicate remote API calls, reduces latency, and optimizes quota consumption.
     */
    object Cache {
        private const val MAX_CACHE_SIZE = 50
        
        private val textCache: MutableMap<String, String> = Collections.synchronizedMap(
            object : LinkedHashMap<String, String>(MAX_CACHE_SIZE, 0.75f, true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
                    return size > MAX_CACHE_SIZE
                }
            }
        )

        /**
         * Retrieves a cached response for the given key.
         */
        fun get(key: String): String? {
            return textCache[key]
        }

        /**
         * Caches a response.
         */
        fun put(key: String, value: String) {
            textCache[key] = value
        }

        /**
         * Clears all cached prompt contents.
         */
        fun clear() {
            textCache.clear()
        }
    }
}
