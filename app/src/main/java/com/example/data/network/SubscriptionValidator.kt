package com.example.data.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class ValidationResult {
    data class Valid(val normalizedUrl: String) : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

object SubscriptionValidator {

    private val ALLOWED_HOSTS = setOf(
        "zerofee.ok-exor.ir",
        "panelsaz.mamadhp.ir"
    )

    private const val REQUIRED_PATH_PREFIX = "/sub/"
    const val RESTRICTED_APP_ERROR = "این اپلیکیشن اختصاصی است"

    /**
     * Validates if the given URL belongs strictly to authorized subscription servers.
     * Prevents security bypasses by parsing scheme, host, port, and path hierarchy.
     */
    fun validateUrl(rawInput: String?): ValidationResult {
        if (rawInput.isNullOrBlank()) {
            return ValidationResult.Invalid("لطفاً لینک اشتراک را وارد کنید")
        }

        val trimmed = rawInput.trim()

        val httpUrl: HttpUrl = trimmed.toHttpUrlOrNull() ?: run {
            // Attempt with https:// prefix if user pasted domain without scheme
            val withScheme = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else {
                trimmed
            }
            withScheme.toHttpUrlOrNull() ?: return ValidationResult.Invalid(RESTRICTED_APP_ERROR)
        }

        // Scheme verification: allow HTTPS and HTTP
        val scheme = httpUrl.scheme.lowercase(Locale.ROOT)
        if (scheme != "https" && scheme != "http") {
            return ValidationResult.Invalid(RESTRICTED_APP_ERROR)
        }

        val host = httpUrl.host.lowercase(Locale.ROOT)
        if (!ALLOWED_HOSTS.contains(host)) {
            return ValidationResult.Invalid(RESTRICTED_APP_ERROR)
        }

        // Path validation: must start with /sub/
        val encodedPath = httpUrl.encodedPath
        if (!encodedPath.startsWith(REQUIRED_PATH_PREFIX) && encodedPath != "/sub") {
            return ValidationResult.Invalid(RESTRICTED_APP_ERROR)
        }

        // Preserves full URL with token, path, and queries
        return ValidationResult.Valid(httpUrl.toString())
    }

    /**
     * Creates an OkHttpClient with a security interceptor that prevents redirects
     * to untrusted third-party hosts.
     */
    fun createSafeHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(false) // We manually validate redirects
            .followSslRedirects(false)
            .addInterceptor(SafeRedirectInterceptor())
            .build()
    }

    private class SafeRedirectInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            var response = chain.proceed(request)
            var redirectCount = 0

            while (response.isRedirect && redirectCount < 3) {
                redirectCount++
                val locationHeader = response.header("Location") ?: break
                val targetUrl = locationHeader.toHttpUrlOrNull() ?: break

                // Verify redirection target host
                val targetHost = targetUrl.host.lowercase(Locale.ROOT)
                if (!ALLOWED_HOSTS.contains(targetHost)) {
                    response.close()
                    throw IOException(RESTRICTED_APP_ERROR)
                }

                response.close()
                request = request.newBuilder().url(targetUrl).build()
                response = chain.proceed(request)
            }
            return response
        }
    }
}
