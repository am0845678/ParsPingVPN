package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.VpnConfigEntity
import com.example.data.network.SubscriptionValidator
import com.example.data.network.ValidationResult
import com.example.data.repository.FetchSubscriptionResult
import com.example.data.repository.VpnRepository
import com.example.vpn.VpnConnectionInfo
import com.example.vpn.VpnManager
import com.example.vpn.VpnStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SubscriptionUiNotice {
    data class Success(val message: String) : SubscriptionUiNotice()
    data class Error(val message: String, val isRestricted: Boolean = false) : SubscriptionUiNotice()
}

data class VpnScreenUiState(
    val connectionInfo: VpnConnectionInfo = VpnConnectionInfo(),
    val configs: List<VpnConfigEntity> = emptyList(),
    val selectedConfig: VpnConfigEntity? = null,
    val isAddSubscriptionOpen: Boolean = false,
    val isServerSelectorOpen: Boolean = false,
    val subscriptionUrlInput: String = "",
    val isAddingSubscription: Boolean = false,
    val subscriptionNotice: SubscriptionUiNotice? = null,
    val isTestingPingForId: Long? = null,
    val toastMessage: String? = null
)

class VpnViewModel(private val repository: VpnRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(VpnScreenUiState())
    val uiState: StateFlow<VpnScreenUiState> = _uiState.asStateFlow()

    init {
        // Collect DB configs
        viewModelScope.launch {
            repository.allConfigs.collect { list ->
                _uiState.value = _uiState.value.copy(configs = list)
            }
        }
        // Collect selected config
        viewModelScope.launch {
            repository.selectedConfig.collect { selected ->
                _uiState.value = _uiState.value.copy(selectedConfig = selected)
            }
        }
        // Collect VPN Connection state
        viewModelScope.launch {
            VpnManager.connectionInfo.collect { info ->
                _uiState.value = _uiState.value.copy(connectionInfo = info)
            }
        }
    }

    fun onUrlInputChange(input: String) {
        _uiState.value = _uiState.value.copy(
            subscriptionUrlInput = input,
            subscriptionNotice = null
        )
    }

    fun openAddSubscription(prefilled: String? = null) {
        _uiState.value = _uiState.value.copy(
            isAddSubscriptionOpen = true,
            subscriptionUrlInput = prefilled ?: _uiState.value.subscriptionUrlInput,
            subscriptionNotice = null
        )
    }

    fun closeAddSubscription() {
        _uiState.value = _uiState.value.copy(
            isAddSubscriptionOpen = false,
            subscriptionNotice = null
        )
    }

    fun openServerSelector() {
        _uiState.value = _uiState.value.copy(isServerSelectorOpen = true)
    }

    fun closeServerSelector() {
        _uiState.value = _uiState.value.copy(isServerSelectorOpen = false)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun handleClipboardUrl(pastedText: String?) {
        if (pastedText.isNullOrBlank()) {
            showToast("کلیپبورد خالی است")
            return
        }

        val validation = SubscriptionValidator.validateUrl(pastedText)
        when (validation) {
            is ValidationResult.Valid -> {
                _uiState.value = _uiState.value.copy(
                    isAddSubscriptionOpen = true,
                    subscriptionUrlInput = validation.normalizedUrl,
                    subscriptionNotice = null
                )
                showToast("لینک اشتراک از کلیپبورد جایگذاری شد")
            }
            is ValidationResult.Invalid -> {
                _uiState.value = _uiState.value.copy(
                    isAddSubscriptionOpen = true,
                    subscriptionUrlInput = pastedText,
                    subscriptionNotice = SubscriptionUiNotice.Error(
                        message = validation.message,
                        isRestricted = (validation.message == SubscriptionValidator.RESTRICTED_APP_ERROR)
                    )
                )
            }
        }
    }

    fun submitSubscription() {
        val input = _uiState.value.subscriptionUrlInput.trim()
        if (input.isBlank()) {
            _uiState.value = _uiState.value.copy(
                subscriptionNotice = SubscriptionUiNotice.Error("لطفاً لینک اشتراک را وارد کنید")
            )
            return
        }

        // Validate domain structure first
        val validation = SubscriptionValidator.validateUrl(input)
        if (validation is ValidationResult.Invalid) {
            _uiState.value = _uiState.value.copy(
                subscriptionNotice = SubscriptionUiNotice.Error(
                    message = validation.message,
                    isRestricted = (validation.message == SubscriptionValidator.RESTRICTED_APP_ERROR)
                )
            )
            return
        }

        val validUrl = (validation as ValidationResult.Valid).normalizedUrl
        _uiState.value = _uiState.value.copy(isAddingSubscription = true, subscriptionNotice = null)

        viewModelScope.launch {
            val result = repository.fetchAndSaveSubscription(validUrl)
            when (result) {
                is FetchSubscriptionResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isAddingSubscription = false,
                        subscriptionUrlInput = "",
                        subscriptionNotice = SubscriptionUiNotice.Success(
                            "${result.countAdded} کانفیگ جدید اضافه شد (مجموع: ${result.totalConfigs})"
                        )
                    )
                }
                is FetchSubscriptionResult.Error -> {
                    val isRestricted = (result.message == SubscriptionValidator.RESTRICTED_APP_ERROR)
                    _uiState.value = _uiState.value.copy(
                        isAddingSubscription = false,
                        subscriptionNotice = SubscriptionUiNotice.Error(
                            message = result.message,
                            isRestricted = isRestricted
                        )
                    )
                }
            }
        }
    }

    fun selectConfig(configId: Long) {
        viewModelScope.launch {
            repository.selectConfig(configId)
            _uiState.value = _uiState.value.copy(isServerSelectorOpen = false)
            showToast("سرور انتخاب شد")
        }
    }

    fun deleteConfig(config: VpnConfigEntity) {
        viewModelScope.launch {
            repository.deleteConfig(config)
            showToast("کانفیگ حذف شد")
        }
    }

    fun testPingForConfig(config: VpnConfigEntity) {
        _uiState.value = _uiState.value.copy(isTestingPingForId = config.id)
        viewModelScope.launch {
            val ping = repository.testPing(config)
            _uiState.value = _uiState.value.copy(isTestingPingForId = null)
            if (ping != null) {
                showToast("پینگ ${config.name}: $ping ms")
            } else {
                showToast("سرور در دسترس نیست")
            }
        }
    }

    fun toggleVpn(context: Context, onPrepareNeeded: (Intent) -> Unit) {
        val currentStatus = _uiState.value.connectionInfo.status

        if (currentStatus == VpnStatus.CONNECTED || currentStatus == VpnStatus.CONNECTING) {
            VpnManager.stopVpn(context)
            return
        }

        val selected = _uiState.value.selectedConfig
        if (selected == null) {
            showToast("ابتدا یک سرور انتخاب کنید یا اشتراک جدید اضافه کنید")
            _uiState.value = _uiState.value.copy(isAddSubscriptionOpen = true)
            return
        }

        // Check VpnService.prepare permission
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent != null) {
            onPrepareNeeded(prepareIntent)
        } else {
            VpnManager.startVpn(context, selected)
        }
    }

    fun onVpnPermissionGranted(context: Context) {
        val selected = _uiState.value.selectedConfig
        if (selected != null) {
            VpnManager.startVpn(context, selected)
        }
    }

    private fun showToast(msg: String) {
        _uiState.value = _uiState.value.copy(toastMessage = msg)
    }

    class Factory(private val repository: VpnRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VpnViewModel(repository) as T
        }
    }
}
