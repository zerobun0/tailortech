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

data class MeasurementExtractionResult(
    val valuesByKey: Map<String, Double>,
    val uncertainKeys: List<String>,
    val missingKeys: List<String>,
    val notes: List<String>
)

object GeminiFitAdvisor {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    @Volatile
    private var runtimeApiKeyOverride: String = ""

    fun setRuntimeApiKeyOverride(apiKey: String) {
        runtimeApiKeyOverride = apiKey.trim()
    }

    suspend fun chatAssistant(
        measurements: UserMeasurements,
        countryCode: String,
        history: List<Pair<String, String>>,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = runtimeApiKeyOverride.ifBlank { BuildConfig.GEMINI_API_KEY }
            require(apiKey.isNotBlank()) {
                "Gemini API key missing. Add it in Settings or GEMINI_API_KEY in Gradle properties."
            }

            val prompt = buildChatPrompt(
                m = measurements,
                countryCode = countryCode,
                history = history,
                userMessage = userMessage
            )

            requestGeminiText(prompt, apiKey)
        }
    }

    suspend fun analyzeOutfit(
        measurements: UserMeasurements,
        clothingPaste: String,
        countryCode: String,
        analysisMode: String,
        imageUrl: String?
    ): Result<OutfitAdviceResult> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = runtimeApiKeyOverride.ifBlank { BuildConfig.GEMINI_API_KEY }
            require(apiKey.isNotBlank()) {
                "Gemini API key missing. Add it in Settings or GEMINI_API_KEY in Gradle properties."
            }

            val prompt = buildPrompt(
                m = measurements,
                clothingPaste = clothingPaste,
                countryCode = countryCode,
                analysisMode = analysisMode,
                imageUrl = imageUrl
            )
            val responseBody = requestGeminiText(prompt, apiKey)
            parseResponse(responseBody)
        }
    }

    suspend fun extractMeasurementsFromText(rawText: String): Result<MeasurementExtractionResult> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = runtimeApiKeyOverride.ifBlank { BuildConfig.GEMINI_API_KEY }
            require(apiKey.isNotBlank()) {
                "Gemini API key missing. Add it in Settings or GEMINI_API_KEY in Gradle properties."
            }

            val prompt = """
Extract body measurements from user text and return strict JSON only.
Rules:
- Convert all values to centimeters.
- Use null when a value is missing or not inferable.
- If confidence is low for a key, put key in uncertain list.

Return JSON shape:
{
  "values": {
    "height": number|null,
    "neck": number|null,
    "headFrontToBack": number|null,
    "chestUpper": number|null,
    "chestLower": number|null,
    "waistNatural": number|null,
    "waistPantsLevel": number|null,
    "hip": number|null,
    "shoulderToShoulder": number|null,
    "neckToShoulder": number|null,
    "neckToWaist": number|null,
    "armscye": number|null,
    "rise": number|null,
    "bicepFlexed": number|null,
    "bicepRelaxed": number|null,
    "shoulderToElbow": number|null,
    "elbowToWrist": number|null,
    "wrist": number|null,
    "wristToMiddleFinger": number|null,
    "thighWidest": number|null,
    "knee": number|null,
    "calfWidest": number|null,
    "ankle": number|null,
    "innerThighToKnee": number|null,
    "kneeToAnkle": number|null,
    "outerThigh": number|null,
    "footLength": number|null,
    "footWidth": number|null
  },
  "uncertain": ["key"],
  "missing": ["key"],
  "notes": ["short note"]
}

User text:
$rawText
""".trimIndent()

            val raw = requestGeminiText(prompt, apiKey)
            val cleaned = raw
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val root = JSONObject(cleaned)
            val valuesObj = root.optJSONObject("values") ?: JSONObject()
            val valueMap = buildMap {
                val keys = valuesObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (!valuesObj.isNull(key)) {
                        put(key, valuesObj.optDouble(key))
                    }
                }
            }

            MeasurementExtractionResult(
                valuesByKey = valueMap,
                uncertainKeys = root.optJSONArray("uncertain").toStringList(),
                missingKeys = root.optJSONArray("missing").toStringList(),
                notes = root.optJSONArray("notes").toStringList()
            )
        }
    }

    private fun requestGeminiText(prompt: String, apiKey: String): String {
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

        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates") ?: JSONArray()
        val first = candidates.optJSONObject(0) ?: throw IllegalStateException("No candidates from Gemini")
        val parts = first.optJSONObject("content")?.optJSONArray("parts") ?: JSONArray()
        return parts.optJSONObject(0)?.optString("text").orEmpty()
    }

    private fun buildChatPrompt(
        m: UserMeasurements,
        countryCode: String,
        history: List<Pair<String, String>>,
        userMessage: String
    ): String {
        val historyText = history.takeLast(8).joinToString("\n") { (role, text) ->
            "$role: $text"
        }

        return """
You are TailorTech AI, a concise menswear fit assistant.
Give practical clothing fit and size advice based on body measurements and country sizing.
Do not output JSON. Use plain helpful text in 3-7 short bullet points when possible.

User profile (cm):
height=${m.heightCm}, neck=${m.neckCm}, chest=${m.chestUpperCm}, waist=${m.waistPantsLevelCm}, hip=${m.hipCm}, thigh=${m.thighWidestCm}, inseam=${m.inseamCm}, shoulder=${m.shoulderToShoulderCm}
countryCode=$countryCode

Recent conversation:
${historyText.ifBlank { "none" }}

User message:
$userMessage
""".trimIndent()
    }

    private fun buildPrompt(
        m: UserMeasurements,
        clothingPaste: String,
        countryCode: String,
        analysisMode: String,
        imageUrl: String?
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
analysisMode=$analysisMode
imageUrl=${imageUrl ?: "none"}

Clothing text pasted by user:
$clothingPaste

If imageUrl is present but cannot be fetched, continue using text-only inference and mention confidence impact in risks.
""".trimIndent()
    }

    private fun parseResponse(text: String): OutfitAdviceResult {
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
