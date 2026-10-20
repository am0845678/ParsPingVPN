package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vpn_configs")
data class VpnConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val protocol: String,
    val serverAddress: String,
    val serverPort: Int,
    val rawUri: String,
    val uuidOrPassword: String = "",
    val security: String = "none",
    val networkType: String = "tcp",
    val pathOrSni: String = "",
    val isSupported: Boolean = true,
    val isSelected: Boolean = false,
    val latencyMs: Long? = null,
    val lastChecked: Long = System.currentTimeMillis()
)
