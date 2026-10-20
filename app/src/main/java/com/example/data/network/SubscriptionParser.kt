package com.example.data.network

import android.util.Base64
import com.example.data.local.VpnConfigEntity
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object SubscriptionParser {

    /**
     * Parses raw subscription response body (which can be base64-encoded or raw multi-line)
     * and produces a de-duplicated list of VpnConfigEntity.
     */
    fun parseSubscription(content: String): List<VpnConfigEntity> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return emptyList()

        val decodedLines = tryDecodeBase64Lines(trimmed)
        val configs = mutableListOf<VpnConfigEntity>()
        val seenKeys = mutableSetOf<String>()

        for (line in decodedLines) {
            val cleanLine = line.trim()
            if (cleanLine.isBlank() || cleanLine.startsWith("#")) continue

            val config = parseLine(cleanLine)
            if (config != null) {
                val dedupeKey = "${config.protocol}:${config.serverAddress}:${config.serverPort}:${config.uuidOrPassword}"
                if (seenKeys.add(dedupeKey)) {
                    configs.add(config)
                }
            }
        }
        return configs
    }

    private fun tryDecodeBase64Lines(input: String): List<String> {
        // First try decoding entire input as Base64 (standard or URL-safe)
        val decoded = tryDecodeBase64String(input)
        return if (decoded != null && decoded.contains("://")) {
            decoded.split("\r\n", "\n")
        } else {
            // Might already be plain lines or mixed
            input.split("\r\n", "\n")
        }
    }

    private fun tryDecodeBase64String(input: String): String? {
        val sanitized = input.replace("\r", "").replace("\n", "").trim()
        val flags = listOf(Base64.DEFAULT, Base64.URL_SAFE, Base64.NO_WRAP)
        for (flag in flags) {
            try {
                val bytes = Base64.decode(sanitized, flag)
                val text = String(bytes, StandardCharsets.UTF_8)
                if (text.isNotBlank() && text.any { it.isLetterOrDigit() }) {
                    return text
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun parseLine(line: String): VpnConfigEntity? {
        return when {
            line.startsWith("vless://", ignoreCase = true) -> parseVless(line)
            line.startsWith("vmess://", ignoreCase = true) -> parseVmess(line)
            line.startsWith("trojan://", ignoreCase = true) -> parseTrojan(line)
            line.startsWith("ss://", ignoreCase = true) -> parseShadowsocks(line)
            line.startsWith("wireguard://", ignoreCase = true) || line.startsWith("wg://", ignoreCase = true) -> parseWireguard(line)
            line.startsWith("openvpn://", ignoreCase = true) || line.contains("BEGIN OpenVPN") -> parseOpenVpn(line)
            else -> null
        }
    }

    private fun parseVless(rawUri: String): VpnConfigEntity? {
        return try {
            val uri = URI(rawUri)
            val uuid = uri.userInfo ?: ""
            val host = uri.host ?: return null
            val port = if (uri.port > 0) uri.port else 443
            val queryParams = parseQueryParams(uri.rawQuery)
            val remark = decodeRemark(uri.rawFragment, "Pars Ping VLESS ($host)")

            val security = queryParams["security"] ?: "none"
            val type = queryParams["type"] ?: "tcp"
            val sni = queryParams["sni"] ?: queryParams["host"] ?: ""

            VpnConfigEntity(
                name = remark,
                protocol = "VLESS",
                serverAddress = host,
                serverPort = port,
                rawUri = rawUri,
                uuidOrPassword = uuid,
                security = security,
                networkType = type,
                pathOrSni = sni,
                isSupported = true
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseVmess(rawUri: String): VpnConfigEntity? {
        return try {
            val base64Part = rawUri.substringAfter("vmess://").trim()
            val decodedJson = tryDecodeBase64String(base64Part) ?: return null
            val json = JSONObject(decodedJson)

            val name = json.optString("ps", "Pars Ping VMess")
            val address = json.optString("add", "")
            val port = json.optInt("port", 443)
            val id = json.optString("id", "")
            val net = json.optString("net", "tcp")
            val tls = json.optString("tls", "none")
            val sni = json.optString("sni", json.optString("host", ""))

            if (address.isBlank()) return null

            VpnConfigEntity(
                name = name,
                protocol = "VMess",
                serverAddress = address,
                serverPort = port,
                rawUri = rawUri,
                uuidOrPassword = id,
                security = tls,
                networkType = net,
                pathOrSni = sni,
                isSupported = true
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseTrojan(rawUri: String): VpnConfigEntity? {
        return try {
            val uri = URI(rawUri)
            val password = uri.userInfo ?: ""
            val host = uri.host ?: return null
            val port = if (uri.port > 0) uri.port else 443
            val queryParams = parseQueryParams(uri.rawQuery)
            val remark = decodeRemark(uri.rawFragment, "Pars Ping Trojan ($host)")

            val security = queryParams["security"] ?: "tls"
            val type = queryParams["type"] ?: "tcp"
            val sni = queryParams["sni"] ?: ""

            VpnConfigEntity(
                name = remark,
                protocol = "Trojan",
                serverAddress = host,
                serverPort = port,
                rawUri = rawUri,
                uuidOrPassword = password,
                security = security,
                networkType = type,
                pathOrSni = sni,
                isSupported = true
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseShadowsocks(rawUri: String): VpnConfigEntity? {
        return try {
            val withoutScheme = rawUri.substringAfter("ss://")
            val fragment = withoutScheme.substringAfter("#", "")
            val remark = if (fragment.isNotEmpty()) decodeRemark(fragment, "Pars Ping SS") else "Pars Ping SS"
            val mainPart = withoutScheme.substringBefore("#")

            if (mainPart.contains("@")) {
                val hostPort = mainPart.substringAfter("@")
                val host = hostPort.substringBefore(":")
                val port = hostPort.substringAfter(":").toIntOrNull() ?: 8388
                val userInfo = mainPart.substringBefore("@")
                val decodedUserInfo = tryDecodeBase64String(userInfo) ?: userInfo

                VpnConfigEntity(
                    name = remark,
                    protocol = "Shadowsocks",
                    serverAddress = host,
                    serverPort = port,
                    rawUri = rawUri,
                    uuidOrPassword = decodedUserInfo,
                    isSupported = true
                )
            } else {
                val decoded = tryDecodeBase64String(mainPart) ?: return null
                val uri = URI("ss://$decoded")
                val host = uri.host ?: return null
                val port = if (uri.port > 0) uri.port else 8388

                VpnConfigEntity(
                    name = remark,
                    protocol = "Shadowsocks",
                    serverAddress = host,
                    serverPort = port,
                    rawUri = rawUri,
                    uuidOrPassword = uri.userInfo ?: "",
                    isSupported = true
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseWireguard(rawUri: String): VpnConfigEntity {
        val remark = try {
            val uri = URI(rawUri)
            decodeRemark(uri.rawFragment, "Pars Ping WireGuard")
        } catch (_: Exception) {
            "Pars Ping WireGuard"
        }
        return VpnConfigEntity(
            name = remark,
            protocol = "WireGuard",
            serverAddress = "wg.parsping.net",
            serverPort = 51820,
            rawUri = rawUri,
            isSupported = true
        )
    }

    private fun parseOpenVpn(rawUri: String): VpnConfigEntity {
        return VpnConfigEntity(
            name = "Pars Ping OpenVPN",
            protocol = "OpenVPN",
            serverAddress = "ovpn.parsping.net",
            serverPort = 1194,
            rawUri = rawUri,
            isSupported = false // Declared clearly as unsupported engine without native binary
        )
    }

    private fun parseQueryParams(query: String?): Map<String, String> {
        if (query.isNullOrBlank()) return emptyMap()
        val result = mutableMapOf<String, String>()
        for (param in query.split("&")) {
            val pair = param.split("=", limit = 2)
            if (pair.size == 2) {
                result[pair[0]] = pair[1]
            }
        }
        return result
    }

    private fun decodeRemark(rawFragment: String?, defaultVal: String): String {
        if (rawFragment.isNullOrBlank()) return defaultVal
        return try {
            URLDecoder.decode(rawFragment, StandardCharsets.UTF_8.name())
        } catch (_: Exception) {
            rawFragment
        }
    }
}
