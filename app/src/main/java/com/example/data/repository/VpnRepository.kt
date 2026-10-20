package com.example.data.repository

import com.example.data.local.VpnConfigDao
import com.example.data.local.VpnConfigEntity
import com.example.data.network.PingTester
import com.example.data.network.SubscriptionParser
import com.example.data.network.SubscriptionValidator
import com.example.data.network.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.Request

sealed class FetchSubscriptionResult {
    data class Success(val countAdded: Int, val totalConfigs: Int) : FetchSubscriptionResult()
    data class Error(val message: String) : FetchSubscriptionResult()
}

class VpnRepository(private val dao: VpnConfigDao) {

    val allConfigs: Flow<List<VpnConfigEntity>> = dao.getAllConfigsFlow()
    val selectedConfig: Flow<VpnConfigEntity?> = dao.getSelectedConfigFlow()

    suspend fun getSelectedConfig(): VpnConfigEntity? = dao.getSelectedConfig()

    suspend fun selectConfig(configId: Long) {
        dao.deselectAll()
        dao.selectById(configId)
    }

    suspend fun deleteConfig(config: VpnConfigEntity) {
        val wasSelected = config.isSelected
        dao.delete(config)
        if (wasSelected) {
            // Select another config if available
            val remaining = dao.getSelectedConfig()
            if (remaining == null) {
                // If there are other configs, mark the first one
                // Will be handled dynamically or by user
            }
        }
    }

    suspend fun clearAllConfigs() {
        dao.clearAll()
    }

    suspend fun testPing(config: VpnConfigEntity): Long? {
        val latency = PingTester.measureTcpLatency(config.serverAddress, config.serverPort)
        dao.updatePing(config.id, latency)
        return latency
    }

    suspend fun fetchAndSaveSubscription(url: String): FetchSubscriptionResult {
        return withContext(Dispatchers.IO) {
            val validation = SubscriptionValidator.validateUrl(url)
            if (validation !is ValidationResult.Valid) {
                return@withContext FetchSubscriptionResult.Error(
                    (validation as ValidationResult.Invalid).message
                )
            }

            val validUrl = validation.normalizedUrl
            val client = SubscriptionValidator.createSafeHttpClient()
            val request = Request.Builder()
                .url(validUrl)
                .header("User-Agent", "ParsPing/1.0 (Android; Mobile)")
                .get()
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext FetchSubscriptionResult.Error("خطای سرور: کد پاسخ ${response.code}")
                    }
                    val body = response.body?.string()
                    if (body.isNullOrBlank()) {
                        return@withContext FetchSubscriptionResult.Error("اطلاعات اشتراک از سرور دریافت نشد")
                    }

                    val parsedList = SubscriptionParser.parseSubscription(body)
                    if (parsedList.isEmpty()) {
                        return@withContext FetchSubscriptionResult.Error("هیچ کانفیگ معتبری در لینک اشتراک یافت نشد")
                    }

                    var addedCount = 0
                    val currentSelected = dao.getSelectedConfig()

                    for ((index, item) in parsedList.withIndex()) {
                        val existing = dao.findByRawUri(item.rawUri)
                        if (existing == null) {
                            val shouldSelect = (currentSelected == null && index == 0)
                            dao.insert(item.copy(isSelected = shouldSelect))
                            addedCount++
                        }
                    }

                    FetchSubscriptionResult.Success(
                        countAdded = addedCount,
                        totalConfigs = parsedList.size
                    )
                }
            } catch (e: Exception) {
                val errorMsg = if (e.message == SubscriptionValidator.RESTRICTED_APP_ERROR) {
                    SubscriptionValidator.RESTRICTED_APP_ERROR
                } else {
                    "خطا در برقراری ارتباط با سرور: ${e.localizedMessage ?: "عدم پاسخگویی"}"
                }
                FetchSubscriptionResult.Error(errorMsg)
            }
        }
    }
}
