package com.example

import android.app.Application
import com.example.data.database.TurismoDatabase
import com.example.data.repository.TurismoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TurismoSanLuisApp : Application() {
    val database: TurismoDatabase by lazy { TurismoDatabase.getInstance(this) }
    val repository: TurismoRepository by lazy { TurismoRepository(database) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            repository.initializeSeed()
        }
    }
}
