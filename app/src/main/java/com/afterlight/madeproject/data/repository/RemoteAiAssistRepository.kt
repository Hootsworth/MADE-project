package com.afterlight.madeproject.data.repository

import com.afterlight.madeproject.domain.model.AiDraftEnhancement
import com.afterlight.madeproject.domain.model.AiProvider
import com.afterlight.madeproject.domain.model.AiSettings
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.model.EventDraft
import com.afterlight.madeproject.domain.model.VibeTag
import com.afterlight.madeproject.domain.repository.AiAssistRepository
import com.afterlight.madeproject.domain.repository.SettingsRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class RemoteAiAssistRepository @Inject constructor(
    private val settingsRepository: SettingsRepository
) : AiAssistRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun polishEventDraft(draft: EventDraft): Result<AiDraftEnhancement> = runCatching {
        val settings = settingsRepository.getAiSettings()
        val prompt = """
            You are a product-side assistant for an events app. Improve quality without changing intent.
            Return strict JSON:
            {
              "title": "...",
              "description": "...",
              "category": "...",
              "tags": ["..."],
              "vibes": ["CHILL|HYPE|INTELLECTUAL|CHAOTIC|COZY|PROFESSIONAL"],
              "reasoning": "one short sentence"
            }
            Rules:
            - Keep title under 55 chars.
            - Keep description under 240 chars.
            - tags max 5.
            - vibes max 3 and only from allowed values.
            Draft:
            title=${draft.title}
            description=${draft.description}
            category=${draft.category}
            venue=${draft.venue}
            tags=${draft.tags}
            vibes=${draft.vibes.joinToString(",") { it.name }}
        """.trimIndent()

        val content = requestText(settings, prompt)
        val obj = parseJsonObjectFromText(content)
        val vibes = parseStringList(obj["vibes"]).mapNotNull { value ->
            runCatching { VibeTag.valueOf(value.uppercase()) }.getOrNull()
        }.map { it.name }

        AiDraftEnhancement(
            title = obj["title"]?.jsonPrimitive?.contentOrNull.orEmpty().ifBlank { draft.title },
            description = obj["description"]?.jsonPrimitive?.contentOrNull.orEmpty().ifBlank { draft.description },
            category = obj["category"]?.jsonPrimitive?.contentOrNull.orEmpty().ifBlank { draft.category },
            tags = parseStringList(obj["tags"]).ifEmpty {
                draft.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
            },
            vibes = vibes.ifEmpty { draft.vibes.map { it.name } },
            reasoning = obj["reasoning"]?.jsonPrimitive?.contentOrNull.orEmpty().ifBlank { "Refined for clarity." }
        )
    }

    override suspend fun polishRecapCaption(caption: String): Result<String> = runCatching {
        val settings = settingsRepository.getAiSettings()
        val prompt = """
            Rewrite this recap caption to be clear, concise, and social-ready.
            Keep tone authentic and keep under 140 chars.
            Return strict JSON: {"caption":"..."}
            Input: $caption
        """.trimIndent()
        val content = requestText(settings, prompt)
        val obj = parseJsonObjectFromText(content)
        obj["caption"]?.jsonPrimitive?.contentOrNull.orEmpty().ifBlank { caption }
    }

    override suspend fun summarizeFeed(events: List<Event>): Result<String> = runCatching {
        if (events.isEmpty()) return@runCatching "No live events right now."
        val settings = settingsRepository.getAiSettings()
        val eventLines = events.take(8).joinToString("\n") {
            "- ${it.title} | ${it.category} | ${it.venue} | spots:${it.spotsLeft}"
        }
        val prompt = """
            Create one useful line for a student browsing events.
            Output should be practical and calm.
            Return strict JSON: {"brief":"..."}
            Max 110 chars.
            Events:
            $eventLines
        """.trimIndent()
        val content = requestText(settings, prompt)
        val obj = parseJsonObjectFromText(content)
        obj["brief"]?.jsonPrimitive?.contentOrNull.orEmpty().ifBlank { "Top pick: ${events.first().title}" }
    }

    private suspend fun requestText(settings: AiSettings, prompt: String): String = withContext(Dispatchers.IO) {
        when (settings.provider) {
            AiProvider.OPENAI -> requestOpenAi(settings, prompt)
            AiProvider.GEMINI -> requestGemini(settings, prompt)
        }
    }

    private fun requestOpenAi(settings: AiSettings, prompt: String): String {
        require(settings.openAiApiKey.isNotBlank()) { "OpenAI API key is missing. Add it in Settings." }
        val model = settings.model.ifBlank { "gpt-4.1-mini" }
        val body = buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                add(
                    buildJsonObject {
                        put("role", "user")
                        put("content", prompt)
                    }
                )
            }
            put("temperature", JsonPrimitive(0.3))
        }.toString()

        val response = postJson(
            url = "https://api.openai.com/v1/chat/completions",
            headers = mapOf("Authorization" to "Bearer ${settings.openAiApiKey}"),
            body = body
        )
        val obj = json.parseToJsonElement(response).jsonObject
        return obj["choices"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")?.jsonObject?.get("content")
            ?.jsonPrimitive?.contentOrNull
            .orEmpty()
    }

    private fun requestGemini(settings: AiSettings, prompt: String): String {
        require(settings.geminiApiKey.isNotBlank()) { "Gemini API key is missing. Add it in Settings." }
        val model = settings.model.ifBlank { "gemini-1.5-flash" }
        val body = buildJsonObject {
            putJsonArray("contents") {
                add(
                    buildJsonObject {
                        putJsonArray("parts") {
                            add(buildJsonObject { put("text", prompt) })
                        }
                    }
                )
            }
            putJsonObject("generationConfig") {
                put("temperature", JsonPrimitive(0.3))
            }
        }.toString()

        val response = postJson(
            url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${settings.geminiApiKey}",
            headers = emptyMap(),
            body = body
        )
        val obj = json.parseToJsonElement(response).jsonObject
        return obj["candidates"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("content")?.jsonObject?.get("parts")
            ?.jsonArray?.firstOrNull()?.jsonObject?.get("text")
            ?.jsonPrimitive?.contentOrNull
            .orEmpty()
    }

    private fun postJson(url: String, headers: Map<String, String>, body: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 15_000
        connection.readTimeout = 25_000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

        OutputStreamWriter(connection.outputStream).use { it.write(body) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val payload = BufferedReader(InputStreamReader(stream)).use { reader ->
            reader.lineSequence().joinToString("\n")
        }
        if (code !in 200..299) {
            error("AI request failed ($code): $payload")
        }
        return payload
    }

    private fun parseStringList(element: kotlinx.serialization.json.JsonElement?): List<String> {
        val arr = element as? JsonArray ?: return emptyList()
        return arr.mapNotNull { it.jsonPrimitive.contentOrNull }.map { it.trim() }.filter { it.isNotBlank() }
    }

    private fun parseJsonObjectFromText(content: String): JsonObject {
        val raw = content.trim()
        val cleaned = if (raw.startsWith("```")) {
            raw.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        } else {
            raw
        }
        return json.parseToJsonElement(cleaned).jsonObject
    }
}
