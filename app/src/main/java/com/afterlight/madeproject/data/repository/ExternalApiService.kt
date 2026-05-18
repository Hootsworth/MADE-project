package com.afterlight.madeproject.data.repository

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Centralised HTTP client for all external (non-AI, non-Firebase) API calls.
 *
 * API endpoints used:
 *  1. DiceBear Avatars  – deterministic SVG/PNG avatars from a seed
 *  2. Unsplash Source   – random event-related cover images
 *  3. QR Server API     – real, scannable QR code PNGs
 *  4. Quotable API      – random inspirational quotes for the home-screen
 */
@Singleton
class ExternalApiService @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true }

    // ── 1. DiceBear Avatar ──────────────────────────────────────────────
    /**
     * Returns a URL pointing to a DiceBear "bottts" avatar PNG seeded by [seed].
     * This is a deterministic URL — no HTTP call needed, but the image is loaded via Coil.
     */
    fun diceBearAvatarUrl(seed: String): String {
        val safeSeed = seed.ifBlank { "default" }
        return "https://api.dicebear.com/9.x/bottts/png?seed=$safeSeed&size=128"
    }

    // ── 2. Cover Photo (LoremFlickr) ────────────────────────────────────
    /**
     * Generates a LoremFlickr URL for an event cover photo based on [keywords].
     * Width × Height can be customised. Uses the free keyword endpoint.
     */
    fun unsplashCoverUrl(keywords: String = "event,campus", width: Int = 800, height: Int = 600): String {
        val safeKeywords = keywords.ifBlank { "event,campus" }.replace(",", ",")
        return "https://loremflickr.com/${width}/${height}/${safeKeywords}"
    }

    // ── 3. QR Server API ────────────────────────────────────────────────
    /**
     * Returns a URL that resolves to a scannable QR code PNG encoding [data].
     * Uses the free api.qrserver.com endpoint.
     */
    fun qrCodeUrl(data: String, size: Int = 300): String {
        val encoded = java.net.URLEncoder.encode(data, "UTF-8")
        return "https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=$encoded"
    }

    // ── 4. Quotable API ─────────────────────────────────────────────────
    /**
     * Fetches a random inspirational quote from the Quotable API.
     * Returns a Pair of (content, author).
     */
    suspend fun fetchRandomQuote(): Result<Pair<String, String>> = runCatching {
        withContext(Dispatchers.IO) {
            val connection = URL("https://api.quotable.io/quotes/random").openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/json")

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val payload = BufferedReader(InputStreamReader(stream)).use { reader ->
                reader.lineSequence().joinToString("\n")
            }
            if (code !in 200..299) error("Quotable API error ($code): $payload")

            val arr = json.parseToJsonElement(payload).jsonArray
            val obj = arr.first().jsonObject
            val content = obj["content"]?.jsonPrimitive?.content.orEmpty()
            val author = obj["author"]?.jsonPrimitive?.content.orEmpty()
            content to author
        }
    }
}
