package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class TextLayer(
    var id: String,
    var text: String,
    var xPercent: Float = 0.5f, // 0.0 to 1.0 (relative to canvas width)
    var yPercent: Float = 0.5f, // 0.0 to 1.0 (relative to canvas height)
    var fontSize: Float = 24f,
    var fontColorHex: String = "#FFFFFF",
    var isGold: Boolean = false,
    var isOutline: Boolean = false,
    var isShadow: Boolean = false,
    var fontFamily: String = "Libre Caslon Text",
    var isBold: Boolean = false,
    var rotation: Float = 0f
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("text", text)
            put("xPercent", xPercent.toDouble())
            put("yPercent", yPercent.toDouble())
            put("fontSize", fontSize.toDouble())
            put("fontColorHex", fontColorHex)
            put("isGold", isGold)
            put("isOutline", isOutline)
            put("isShadow", isShadow)
            put("fontFamily", fontFamily)
            put("isBold", isBold)
            put("rotation", rotation.toDouble())
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): TextLayer {
            return TextLayer(
                id = obj.optString("id", System.currentTimeMillis().toString()),
                text = obj.optString("text", "Divine Words"),
                xPercent = obj.optDouble("xPercent", 0.5).toFloat(),
                yPercent = obj.optDouble("yPercent", 0.5).toFloat(),
                fontSize = obj.optDouble("fontSize", 24.0).toFloat(),
                fontColorHex = obj.optString("fontColorHex", "#FFFFFF"),
                isGold = obj.optBoolean("isGold", false),
                isOutline = obj.optBoolean("isOutline", false),
                isShadow = obj.optBoolean("isShadow", false),
                fontFamily = obj.optString("fontFamily", "Libre Caslon Text"),
                isBold = obj.optBoolean("isBold", false),
                rotation = obj.optDouble("rotation", 0.0).toFloat()
            )
        }
    }
}

data class StickerLayer(
    var id: String,
    var stickerType: String, // "om", "swastik", "lotus", "peacock_feather", "diya", "bell", "trishul", "khatu_nishan"
    var xPercent: Float = 0.5f,
    var yPercent: Float = 0.5f,
    var scale: Float = 1.0f,
    var rotation: Float = 0f
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("stickerType", stickerType)
            put("xPercent", xPercent.toDouble())
            put("yPercent", yPercent.toDouble())
            put("scale", scale.toDouble())
            put("rotation", rotation.toDouble())
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): StickerLayer {
            return StickerLayer(
                id = obj.optString("id", System.currentTimeMillis().toString()),
                stickerType = obj.optString("stickerType", "om"),
                xPercent = obj.optDouble("xPercent", 0.5).toFloat(),
                yPercent = obj.optDouble("yPercent", 0.5).toFloat(),
                scale = obj.optDouble("scale", 1.0).toFloat(),
                rotation = obj.optDouble("rotation", 0.0).toFloat()
            )
        }
    }
}

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val backgroundImage: String, // URL, local drawable identifier, or AI image URL
    val textLayersJson: String,  // JSONArray string of TextLayers
    val stickersJson: String,    // JSONArray string of StickerLayers
    val frameType: String = "None", // "None", "Golden", "Temple", "Floral", "Lotus"
    val effectType: String = "None", // "None", "Golden Glow", "Divine Aura", "Mandala", "Sparkles"
    val blur: Float = 0f,
    val brightness: Float = 1f, // 1.0 = normal
    val contrast: Float = 1f,   // 1.0 = normal
    val saturation: Float = 1f, // 1.0 = normal
    val toneType: String = "Normal", // "Normal", "Warm", "Cool", "Vignette"
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getTextLayers(): List<TextLayer> {
        val list = mutableListOf<TextLayer>()
        if (textLayersJson.isEmpty()) return list
        try {
            val arr = JSONArray(textLayersJson)
            for (i in 0 until arr.length()) {
                list.add(TextLayer.fromJsonObject(arr.getJSONObject(i)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getStickerLayers(): List<StickerLayer> {
        val list = mutableListOf<StickerLayer>()
        if (stickersJson.isEmpty()) return list
        try {
            val arr = JSONArray(stickersJson)
            for (i in 0 until arr.length()) {
                list.add(StickerLayer.fromJsonObject(arr.getJSONObject(i)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    companion object {
        fun serializeTextLayers(layers: List<TextLayer>): String {
            val arr = JSONArray()
            for (layer in layers) {
                arr.put(layer.toJsonObject())
            }
            return arr.toString()
        }

        fun serializeStickerLayers(layers: List<StickerLayer>): String {
            val arr = JSONArray()
            for (layer in layers) {
                arr.put(layer.toJsonObject())
            }
            return arr.toString()
        }
    }
}
