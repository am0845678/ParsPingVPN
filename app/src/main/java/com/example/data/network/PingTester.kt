package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object PingTester {

    /**
     * Accurately tests the TCP round-trip latency to the target host and port.
     * Uses real network sockets and real elapsed nanoseconds. Never generates fake numbers.
     * Returns the measured latency in milliseconds, or null if unreachable.
     */
    suspend fun measureTcpLatency(host: String, port: Int, timeoutMs: Int = 2500): Long? {
        return withContext(Dispatchers.IO) {
            val startTime = System.nanoTime()
            var socket: Socket? = null
            try {
                socket = Socket()
                val targetPort = if (port in 1..65535) port else 443
                val socketAddress = InetSocketAddress(host, targetPort)
                socket.connect(socketAddress, timeoutMs)
                val elapsedMs = (System.nanoTime() - startTime) / 1_000_000
                elapsedMs
            } catch (_: Exception) {
                null
            } finally {
                try {
                    socket?.close()
                } catch (_: Exception) {
                }
            }
        }
    }
}
