package com.tailortech.app.domain

import com.tailortech.app.BuildConfig
import com.tailortech.app.data.UserMeasurements
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class OutfitAdviceResult(
    val summary: String,
    val recommendation: String,
    val riskScore: Int,
    val risks: List<String>,
    val fitNotes: List<String>
)

object GeminiFitAdvisor {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun analyzeOutfit(
        measurements: UserMeasurements,
        clothingPaste: String,
        countryCode: String
    ): Result<OutfitAdviceResult> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = BuildConfig.GEMINI_API_KEY
            require(apiKey.isNotBlank()) {
                "Gemini API key missing. Add GEMINI_API_KEY to your Gradle properties."
            }

            val prompt = buildPrompt(measurements, clothingPaste, countryCode)
            val payload = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                    }
                ))
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val responseBody = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Gemini request failed: ${response.code}")
                }
                response.body?.string().orEmpty()
            }

            parseResponse(responseBody)
        }
    }

    private fun buildPrompt(
        m: UserMeasurements,
        clothingPaste: String,
        countryCode: String
    ): String {
        return """
Analyze clothing fit risk for this user. Return strict JSON only with keys:
summary, recommendation, riskScore, risks, fitNotes.
- riskScore must be an integer from 0 to 100 (0 low risk, 100 high risk).
- risks and fitNotes must be arrays of short strings.
- recommendation must be one of: BUY, BUY_WITH_CAUTION, SKIP.

User profile (cm):
height=${m.heightCm}, neck=${m.neckCm}, chest=${m.chestUpperCm}, waist=${m.waistPantsLevelCm}, hip=${m.hipCm}, thigh=${m.thighWidestCm}, inseam=${m.inseamCm}, footLength=${m.footLengthCm}, shoulder=${m.shoulderToShoulderCm}
countryCode=$countryCode

Clothing text pasted by user:
$clothingPaste
""".trimIndent()
    }

    private fun parseResponse(raw: String): OutfitAdviceResult {
        val root = JSONObject(raw)
        val candidates = root.optJSONArray("candidates") ?: JSONArray()
        val first = candidates.optJSONObject(0) ?: throw IllegalStateException("No candidates from Gemini")
        val parts = first.optJSONObject("content")?.optJSONArray("parts") ?: JSONArray()
        val text = parts.optJSONObject(0)?.optString("text").orEmpty()

        val cleaned = text
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val obj = JSONObject(cleaned)
        return OutfitAdviceResult(
            summary = obj.optString("summary", "No summary"),
            recommendation = obj.optString("recommendation", "BUY_WITH_CAUTION"),
            riskScore = obj.optInt("riskScore", 50).coerceIn(0, 100),
            risks = obj.optJSONArray("risks").toStringList(),
            fitNotes = obj.optJSONArray("fitNotes").toStringList()
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                val value = optString(i)
                if (value.isNotBlank()) add(value)
            }
        }
    }
}
