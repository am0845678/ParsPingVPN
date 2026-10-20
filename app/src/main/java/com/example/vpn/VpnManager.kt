package com.example.vpn

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.VpnConfigEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object VpnManager {
    private val _connectionInfo = MutableStateFlow(VpnConnectionInfo())
    val connectionInfo: StateFlow<VpnConnectionInfo> = _connectionInfo.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startVpn(context: Context, config: VpnConfigEntity) {
        if (_connectionInfo.value.status == VpnStatus.CONNECTING ||
            _connectionInfo.value.status == VpnStatus.CONNECTED
        ) {
            return
        }

        if (!config.isSupported) {
            _connectionInfo.value = _connectionInfo.value.copy(
                status = VpnStatus.FAILED,
                errorMessage = "پروتکل ${config.protocol} در این نسخه پشتیبانی نمیشود"
            )
            return
        }

        _connectionInfo.value = VpnConnectionInfo(
            status = VpnStatus.CONNECTING,
            serverName = config.name,
            serverAddress = config.serverAddress,
            protocol = config.protocol,
            durationSeconds = 0,
            errorMessage = null
        )

        val intent = Intent(context, ParsPingVpnService::class.java).apply {
            action = ParsPingVpnService.ACTION_CONNECT
            putExtra(ParsPingVpnService.EXTRA_CONFIG_NAME, config.name)
            putExtra(ParsPingVpnService.EXTRA_SERVER_HOST, config.serverAddress)
            putExtra(ParsPingVpnService.EXTRA_SERVER_PORT, config.serverPort)
            putExtra(ParsPingVpnService.EXTRA_PROTOCOL, config.protocol)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopVpn(context: Context) {
        if (_connectionInfo.value.status == VpnStatus.DISCONNECTED) return

        _connectionInfo.value = _connectionInfo.value.copy(status = VpnStatus.DISCONNECTING)

        val intent = Intent(context, ParsPingVpnService::class.java).apply {
            action = ParsPingVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    fun updateConnected(serverName: String, serverAddress: String, protocol: String) {
        _connectionInfo.value = _connectionInfo.value.copy(
            status = VpnStatus.CONNECTED,
            serverName = serverName,
            serverAddress = serverAddress,
            protocol = protocol,
            errorMessage = null
        )
        startTimer()
    }

    fun updateDisconnected() {
        stopTimer()
        _connectionInfo.value = VpnConnectionInfo(
            status = VpnStatus.DISCONNECTED,
            durationSeconds = 0,
            uploadBytes = 0,
            downloadBytes = 0,
            errorMessage = null
        )
    }

    fun updateFailed(message: String) {
        stopTimer()
        _connectionInfo.value = _connectionInfo.value.copy(
            status = VpnStatus.FAILED,
            errorMessage = message,
            durationSeconds = 0
        )
    }

    fun updateTraffic(uploadDelta: Long, downloadDelta: Long) {
        val current = _connectionInfo.value
        _connectionInfo.value = current.copy(
            uploadBytes = current.uploadBytes + uploadDelta,
            downloadBytes = current.downloadBytes + downloadDelta
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            var seconds = 0L
            while (isActive) {
                delay(1000)
                seconds++
                _connectionInfo.value = _connectionInfo.value.copy(durationSeconds = seconds)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
