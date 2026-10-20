package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.VpnRepository

class ParsPingApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: VpnRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        repository = VpnRepository(database.vpnConfigDao())
    }

    companion object {
        lateinit var instance: ParsPingApplication
            private set
    }
}
