package com.example.data.ai

/**
 * Typed domain exceptions for CIVIC NEXUS AI City Copilot operations.
 * Allows discriminating connection errors, missing credentials, rate limits,
 * and server errors for clear user guidance without leaking sensitive keys.
 */
sealed class CivicAiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class KeyMissing(message: String = "Gemini API key is not configured. Please add your GEMINI_API_KEY in the AI Studio Secrets panel.") : CivicAiException(message)
    class NetworkError(message: String = "Network connection failed or timed out. Please check your internet connection.", cause: Throwable? = null) : CivicAiException(message, cause)
    class RateLimit(message: String = "Gemini AI rate limit reached. Please wait a moment before trying again.") : CivicAiException(message)
    class ServerError(val statusCode: Int, message: String = "AI service error (HTTP $statusCode).") : CivicAiException(message)
    class InvalidResponse(message: String = "Received an invalid or empty response from AI engine.") : CivicAiException(message)
    class BadRequest(message: String = "Invalid request parameters for AI engine.") : CivicAiException(message)
}
