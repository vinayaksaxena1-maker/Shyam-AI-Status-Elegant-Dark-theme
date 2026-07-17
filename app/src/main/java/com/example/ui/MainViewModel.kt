package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID

sealed class Screen {
    object Home : Screen()
    object Search : Screen()
    object Generator : Screen()
    object CanvasEditor : Screen()
    object Library : Screen() // Project management list
    object Profile : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(db.projectDao())

    // All saved projects
    val savedProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current screen
    var currentScreen by mutableStateOf<Screen>(Screen.Home)

    // Daily Limit Quota (Module 11)
    var dailyQuotaLeft by mutableStateOf(3)
        private set

    fun consumeQuota(): Boolean {
        return if (dailyQuotaLeft > 0) {
            dailyQuotaLeft--
            true
        } else {
            false
        }
    }

    fun resetQuota() {
        dailyQuotaLeft = 3
    }

    // --- Active Canvas Editor States ---
    var activeProjectId by mutableStateOf<Int?>(null)
    var projectName by mutableStateOf("New Status Design")
    var selectedBackground by mutableStateOf(DEFAULTS_IMAGES[0].url)
    var textLayers by mutableStateOf<List<TextLayer>>(emptyList())
    var stickerLayers by mutableStateOf<List<StickerLayer>>(emptyList())
    
    var selectedFrame by mutableStateOf("None") // "None", "Golden", "Temple", "Floral", "Lotus"
    var selectedEffect by mutableStateOf("None") // "None", "Golden Glow", "Divine Aura", "Mandala", "Sparkles"
    
    // Background filters
    var blurVal by mutableStateOf(0f)
    var brightnessVal by mutableStateOf(1f)
    var contrastVal by mutableStateOf(1f)
    var saturationVal by mutableStateOf(1f)
    var toneTypeVal by mutableStateOf("Normal") // "Normal", "Warm", "Cool", "Vignette"

    // Selection tracking
    var selectedTextLayerId by mutableStateOf<String?>(null)
    var selectedStickerLayerId by mutableStateOf<String?>(null)

    // --- Search states ---
    var searchQuery by mutableStateOf("")
    var searchHistory by mutableStateOf(listOf("Khatu Shyam", "Krishna Neon", "Shiv Aura", "Kedarnath"))
    var searchSuggestions = listOf("Khatu Shyam HD", "Radha Krishna 3D", "Mahadev Saffron", "Hanuman Bajrang", "Temple Dawn", "Vrindavan Light")
    var filteredBackgrounds by mutableStateOf(DEFAULTS_IMAGES)

    // --- Prompt/AI states ---
    var aiPromptInput by mutableStateOf("")
    var selectedAiStyle by mutableStateOf("Bhakti Style") // "Bhakti Style", "Pooja Style", "Neon-Light", "3D Canvas", "Realistic", "Artistic Portrait"
    var isProcessingAi by mutableStateOf(false)
    var generatedResultText by mutableStateOf("")

    init {
        // Prepare some default text layers if creating a new design
        resetCanvas()
    }

    fun resetCanvas() {
        activeProjectId = null
        projectName = "Devotional Status"
        selectedBackground = DEFAULTS_IMAGES[0].url
        textLayers = listOf(
            TextLayer(
                id = UUID.randomUUID().toString(),
                text = "हारे का सहारा, बाबा श्याम हमारा",
                xPercent = 0.5f,
                yPercent = 0.75f,
                fontSize = 24f,
                fontColorHex = "#FF9933",
                fontFamily = "Libre Caslon Text",
                isBold = true,
                isShadow = true
            )
        )
        stickerLayers = emptyList()
        selectedFrame = "None"
        selectedEffect = "None"
        blurVal = 0f
        brightnessVal = 1f
        contrastVal = 1f
        saturationVal = 1f
        toneTypeVal = "Normal"
        selectedTextLayerId = textLayers.firstOrNull()?.id
        selectedStickerLayerId = null
    }

    // --- Text Layer Actions ---
    fun addTextLayer(text: String = "जय श्री श्याम") {
        val newLayer = TextLayer(
            id = UUID.randomUUID().toString(),
            text = text,
            xPercent = 0.5f,
            yPercent = 0.5f,
            fontSize = 22f,
            fontColorHex = "#FFFFFF",
            fontFamily = "Libre Caslon Text",
            isBold = true,
            isShadow = true
        )
        textLayers = textLayers + newLayer
        selectedTextLayerId = newLayer.id
        selectedStickerLayerId = null
    }

    fun updateSelectedText(update: (TextLayer) -> Unit) {
        selectedTextLayerId?.let { id ->
            textLayers = textLayers.map {
                if (it.id == id) {
                    val cloned = it.copy()
                    update(cloned)
                    cloned
                } else it
            }
        }
    }

    fun deleteSelectedText() {
        selectedTextLayerId?.let { id ->
            textLayers = textLayers.filter { it.id != id }
            selectedTextLayerId = textLayers.lastOrNull()?.id
        }
    }

    // --- Sticker Actions ---
    fun addSticker(type: String) {
        val newSticker = StickerLayer(
            id = UUID.randomUUID().toString(),
            stickerType = type,
            xPercent = 0.5f,
            yPercent = 0.35f,
            scale = 1.2f,
            rotation = 0f
        )
        stickerLayers = stickerLayers + newSticker
        selectedStickerLayerId = newSticker.id
        selectedTextLayerId = null
    }

    fun updateSelectedSticker(update: (StickerLayer) -> Unit) {
        selectedStickerLayerId?.let { id ->
            stickerLayers = stickerLayers.map {
                if (it.id == id) {
                    val cloned = it.copy()
                    update(cloned)
                    cloned
                } else it
            }
        }
    }

    fun deleteSelectedSticker() {
        selectedStickerLayerId?.let { id ->
            stickerLayers = stickerLayers.filter { it.id != id }
            selectedStickerLayerId = null
        }
    }

    // --- Smart AI Designer (Module 4) ---
    fun applySmartAutoLayout() {
        // Auto-analyze and align elements beautifully
        // Center-align text in structured margins
        val totalLayers = textLayers.size
        textLayers = textLayers.mapIndexed { index, layer ->
            val spacing = 0.15f
            val baseOffset = 0.55f
            layer.copy(
                xPercent = 0.5f,
                yPercent = baseOffset + (index * spacing),
                fontSize = if (index == 0) 28f else 20f,
                fontColorHex = if (index == 0) "#FF9933" else "#FFFFFF",
                isBold = true,
                isShadow = true,
                isGold = index == 0
            )
        }
        // Auto position sticker on the upper center
        if (stickerLayers.isNotEmpty()) {
            stickerLayers = stickerLayers.mapIndexed { index, sticker ->
                sticker.copy(
                    xPercent = 0.5f,
                    yPercent = 0.25f,
                    scale = 1.3f
                )
            }
        } else {
            // Add a beautiful default Peacock Feather or Om sticker
            addSticker("om")
            stickerLayers = stickerLayers.map { it.copy(xPercent = 0.5f, yPercent = 0.25f) }
        }
        // Choose best filters automatically (higher saturation, warmth filter)
        saturationVal = 1.3f
        brightnessVal = 1.05f
        contrastVal = 1.15f
        toneTypeVal = "Warm"
        selectedEffect = "Golden Glow"
        selectedFrame = "Golden"
    }

    // --- Creative Tools: Shuffle & Remix (Module 10) ---
    fun remixDesign() {
        // Remix colors, font sizes and random layouts
        val randomPalettes = listOf(
            listOf("#FF9933", "#FFFFFF", "#FED65B"),
            listOf("#FED65B", "#FFDAD6", "#FF9933"),
            listOf("#FFDA44", "#FFFFFF", "#E9C349"),
            listOf("#FFFFFF", "#FFCDD2", "#FFE088")
        )
        val palette = randomPalettes.random()
        textLayers = textLayers.mapIndexed { index, layer ->
            layer.copy(
                fontColorHex = palette[index % palette.size],
                fontSize = (20..32).random().toFloat(),
                isGold = (0..1).random() == 1,
                isOutline = (0..1).random() == 1,
                isShadow = true,
                rotation = (-8..8).random().toFloat()
            )
        }
        if (stickerLayers.isNotEmpty()) {
            stickerLayers = stickerLayers.map {
                it.copy(
                    scale = (10..15).random() / 10f,
                    rotation = (-15..15).random().toFloat()
                )
            }
        }
        // Shuffle effect/frame
        selectedEffect = listOf("None", "Golden Glow", "Divine Aura", "Mandala", "Sparkles").random()
        selectedFrame = listOf("None", "Golden", "Temple", "Floral", "Lotus").random()
    }

    // --- Project Database Operations (Module 9) ---
    fun saveProject() {
        viewModelScope.launch {
            val textJson = Project.serializeTextLayers(textLayers)
            val stickerJson = Project.serializeStickerLayers(stickerLayers)
            
            val projectToSave = Project(
                id = activeProjectId ?: 0,
                name = projectName,
                backgroundImage = selectedBackground,
                textLayersJson = textJson,
                stickersJson = stickerJson,
                frameType = selectedFrame,
                effectType = selectedEffect,
                blur = blurVal,
                brightness = brightnessVal,
                contrast = contrastVal,
                saturation = saturationVal,
                toneType = toneTypeVal,
                timestamp = System.currentTimeMillis()
            )
            
            val savedId = repository.insert(projectToSave)
            if (activeProjectId == null) {
                activeProjectId = savedId.toInt()
            }
        }
    }

    fun openProject(project: Project) {
        activeProjectId = project.id
        projectName = project.name
        selectedBackground = project.backgroundImage
        textLayers = project.getTextLayers()
        stickerLayers = project.getStickerLayers()
        selectedFrame = project.frameType
        selectedEffect = project.effectType
        blurVal = project.blur
        brightnessVal = project.brightness
        contrastVal = project.contrast
        saturationVal = project.saturation
        toneTypeVal = project.toneType
        
        selectedTextLayerId = textLayers.firstOrNull()?.id
        selectedStickerLayerId = null
        currentScreen = Screen.CanvasEditor
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteById(project.id)
            if (activeProjectId == project.id) {
                resetCanvas()
            }
        }
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            val duplicated = project.copy(
                id = 0,
                name = "${project.name} (Copy)",
                timestamp = System.currentTimeMillis()
            )
            repository.insert(duplicated)
        }
    }

    fun renameProject(project: Project, newName: String) {
        viewModelScope.launch {
            val updated = project.copy(
                name = newName,
                timestamp = System.currentTimeMillis()
            )
            repository.insert(updated)
        }
    }

    // --- Search Logic ---
    fun performSearch(query: String) {
        searchQuery = query
        if (query.isNotEmpty() && !searchHistory.contains(query)) {
            searchHistory = (listOf(query) + searchHistory).take(6)
        }
        
        filteredBackgrounds = if (query.isEmpty()) {
            DEFAULTS_IMAGES
        } else {
            DEFAULTS_IMAGES.filter {
                it.title.contains(query, ignoreCase = true) || 
                it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
    }

    // --- AI Image & Prompt Generation Logic (Module 2, 14) ---
    fun enhancePromptAndGenerate() {
        if (aiPromptInput.isBlank()) return
        
        isProcessingAi = true
        generatedResultText = ""
        
        viewModelScope.launch {
            // Consume quota check
            val quotaOk = consumeQuota()
            if (!quotaOk) {
                generatedResultText = "Daily limit of 3 AI generations reached! Please try again tomorrow or select a background from our high-fidelity temple gallery."
                isProcessingAi = false
                return@launch
            }

            // Let's call real Gemini API to enhance the prompt for spiritual depth and suggest devotional overlays!
            val systemInstruction = "You are a highly premium Hindu devotional designer assistant. Take a simple text prompt and enhance it into a beautiful, detailed cinematic image generation description that evokes deep peace and divine presence. Additionally, reply in this structured JSON-like format:\n{\n  \"enhancedPrompt\": \"<detailed scenic description>\",\n  \"suggestedQuote\": \"<a short beautiful Sanskrit or Hindi devotional couplet/quote>\",\n  \"colorPalette\": [\"#HEX1\", \"#HEX2\"]\n}"
            
            val prompt = "Enhance this devotional prompt: '$aiPromptInput' in style: '$selectedAiStyle'."
            
            val result = GeminiClient.generateDevotionalText(prompt, systemInstruction)
            
            // Simulating image generation matching the selected style
            // We map style to one of our gorgeous premium design templates to keep the app 100% responsive and offline-resilient
            val selectedStyleImage = when (selectedAiStyle) {
                "Neon-Light" -> DEFAULTS_IMAGES[1].url
                "3D Canvas" -> DEFAULTS_IMAGES[2].url
                "Artistic Portrait" -> DEFAULTS_IMAGES[3].url
                "Pooja Style" -> DEFAULTS_IMAGES[0].url
                "Bhakti Style" -> DEFAULTS_IMAGES[0].url
                else -> DEFAULTS_IMAGES.random().url
            }

            selectedBackground = selectedStyleImage
            
            // Try to extract suggestions or fall back beautifully
            var quote = "जय श्री श्याम"
            if (result.contains("suggestedQuote")) {
                try {
                    // Extract simplistic JSON or substring
                    val quoteStartIndex = result.indexOf("\"suggestedQuote\":")
                    if (quoteStartIndex != -1) {
                        val sub = result.substring(quoteStartIndex + 17)
                        val end = sub.indexOf("\"")
                        val nextQuoteStart = sub.indexOf("\"", end + 1)
                        if (nextQuoteStart != -1) {
                            val nextQuoteEnd = sub.indexOf("\"", nextQuoteStart + 1)
                            quote = sub.substring(nextQuoteStart + 1, nextQuoteEnd)
                        } else {
                            quote = sub.substring(0, sub.indexOf("\n")).replace("\"", "").replace(",", "").trim()
                        }
                    }
                } catch (e: Exception) {
                    quote = "बाबा श्याम सदा सहायते"
                }
            } else {
                quote = when {
                    aiPromptInput.contains("shyam", true) || aiPromptInput.contains("श्याम", true) -> "हारे का सहारा, बाबा श्याम हमारा"
                    aiPromptInput.contains("krishna", true) -> "राधे राधे जपो चले आएंगे बिहारी"
                    aiPromptInput.contains("shiv", true) -> "हर हर महादेव शंभू"
                    else -> "शुभ प्रभात - प्रभु कृपा सदा बनी रहे"
                }
            }

            // Create Canvas with generated background and recommended quote
            projectName = "AI - $aiPromptInput"
            textLayers = listOf(
                TextLayer(
                    id = UUID.randomUUID().toString(),
                    text = quote,
                    xPercent = 0.5f,
                    yPercent = 0.75f,
                    fontSize = 22f,
                    fontColorHex = "#FF9933",
                    fontFamily = "Libre Caslon Text",
                    isBold = true,
                    isShadow = true
                )
            )
            stickerLayers = listOf(
                StickerLayer(
                    id = UUID.randomUUID().toString(),
                    stickerType = "om",
                    xPercent = 0.5f,
                    yPercent = 0.28f,
                    scale = 1.3f
                )
            )
            selectedFrame = "Golden"
            selectedEffect = "Divine Aura"
            
            isProcessingAi = false
            currentScreen = Screen.CanvasEditor
        }
    }

    // Bhakti Mode One-Tap Creator (Module 14)
    fun buildOneTapBhaktiPoster(topic: String) {
        // Choose topic specific assets
        val matches = DEFAULTS_IMAGES.filter { it.title.contains(topic, true) || it.tags.any { tag -> tag.contains(topic, true) } }
        val bg = if (matches.isNotEmpty()) matches.random().url else DEFAULTS_IMAGES.random().url
        
        selectedBackground = bg
        projectName = "One-Tap $topic Poster"
        
        val quote = when {
            topic.contains("shyam", true) -> "हारे का सहारा, बाबा श्याम हमारा"
            topic.contains("krishna", true) || topic.contains("राधा", true) -> "राधे राधे जय श्री कृष्णा"
            topic.contains("shiv", true) || topic.contains("महादेव", true) -> "ॐ नमः शिवाय"
            topic.contains("hanuman", true) || topic.contains("बजरंग", true) -> "जय श्री राम, जय हनुमान"
            else -> "प्रभु की शरण में परम शांति"
        }

        textLayers = listOf(
            TextLayer(
                id = UUID.randomUUID().toString(),
                text = quote,
                xPercent = 0.5f,
                yPercent = 0.72f,
                fontSize = 24f,
                fontColorHex = "#FED65B",
                isBold = true,
                isShadow = true,
                isGold = true
            ),
            TextLayer(
                id = UUID.randomUUID().toString(),
                text = "नमो नमः",
                xPercent = 0.5f,
                yPercent = 0.85f,
                fontSize = 18f,
                fontColorHex = "#FFFFFF",
                isBold = false,
                isShadow = true
            )
        )

        stickerLayers = listOf(
            StickerLayer(
                id = UUID.randomUUID().toString(),
                stickerType = if (topic.contains("shiv", true)) "trishul" else "om",
                xPercent = 0.5f,
                yPercent = 0.25f,
                scale = 1.2f
            )
        )

        selectedFrame = "Golden"
        selectedEffect = "Golden Glow"
        currentScreen = Screen.CanvasEditor
    }
}

// Default images list with beautiful high quality hotlinks from Google Storage
data class DevotionalImage(
    val title: String,
    val url: String,
    val description: String,
    val tags: List<String>
)

val DEFAULTS_IMAGES = listOf(
    DevotionalImage(
        title = "Khatu Shyam Ji (Crown of Devotion)",
        url = "https://lh3.googleusercontent.com/aida-public/AB6AXuCmcbOIWxf8kZCV4kwS4h_W05mxigFup0PsRRUFRIgZM54S0DsvJJGot21feDzqxnvshgrR2J7HkgL6Sqx-98Yfm2eiR8z_-XnXxjOQjiLgQAMoIEKva55FY454VcH4C7Cc5w9ssA69YUDT2xgU1e1WkIWyOwXih4o40okfEFDP6wJECcLYJy0mJJo3Q2kRV_3fw8ShpFVwQzrzf17xfY5E0VIIm2qXLwERNstKL0ZB8lD5snW1164moA",
        description = "A majestic digital portrait of Khatu Shyam Ji with a shimmering golden crown and traditional vibrant garlands.",
        tags = listOf("Khatu Shyam", "Shyam Baba", "Krishna", "Saffron", "Crown")
    ),
    DevotionalImage(
        title = "Neon Light Ganesha",
        url = "https://lh3.googleusercontent.com/aida-public/AB6AXuBrUwhjadyKSIQr8ZuhsjHfpC3q8d7bvx0zn9lHK5p1x9K5_xD7_gLnn4P9pz7-929HEBCXtlR1YoCYfhi3WtUMW2k0BojSPe3rA6rAzgsNYrFA7OuZbFwvdifD3soioSwNFT6Qkn7g4v3oSCC4gF4E_VpqgxtAGz1TFtc-i5cZ1D2EPqLE9UzpDzc2W1eDuYFgP3-2BWLmTYDjBtavVVv97UeVUfR7LbpWhSZM7jyBDGBesDx1kXG0yA",
        description = "A mesmerizing cinematic close-up of a spiritual deity in a modern neon-light style with glowing sacred geometry.",
        tags = listOf("Neon", "Ganesha", "Modern", "Orange", "Aura")
    ),
    DevotionalImage(
        title = "Temple Interior Dawn",
        url = "https://lh3.googleusercontent.com/aida-public/AB6AXuDGCOsrYvAy_qn4rUZY5HzL7R7VWMAiauN4TZ8TPDtVDsrjDV4IlWsQD-Oi8h-i7ZEcwetwpm155KsxUH3U6yV0qHZ6vrWdiTQVdPrqzU8XgyKd-MY4aiSNuiMzhdlJObIKv6naS4-8_wUjywtgMct91ETIRBAsfDtNI8G8Yym_-1JmOQlNaovAClf_v7E7YBL9RldmL2MH613kUwZTiE1T_QiZuyE2FnruQdT0lsQkSNOWPHUr8-D52w",
        description = "A peaceful and serene 3D canvas art piece of a temple interior at dawn with sunbeams filtering through sacred columns.",
        tags = listOf("Temple", "Dawn", "Luminance", "Gold", "Interior")
    ),
    DevotionalImage(
        title = "Artistic Saffron Portrait",
        url = "https://lh3.googleusercontent.com/aida-public/AB6AXuD0D2ncaPQIanK2WPgxQxW5hZ22KFDbeoAr42qPufWgWRKqCou8mML9Zfo6OUDEDHnAbaXk6xOhiByprK49kp-uuI7qWRT53Kykn8QP1jEZppGvd_hwRi_LqLqSQCz5ZXHJwKcPGLxPxzm6yRTWroRCSGJHi3Fw-wDSFJhiYgolxtB8vvGNunEAKN0LyFrqCoInSbRjUo5l-capGh0_333vWH7kKPvVE5JeS2c55CnT1D-VZbExF90EHA",
        description = "An artistic spiritual portrait featuring stylized traditional Indian motifs and floral arrangements in saffron gradients.",
        tags = listOf("Artistic", "Saraswati", "Saffron", "Floral", "Gold")
    )
)
