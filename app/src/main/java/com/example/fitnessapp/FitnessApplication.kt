package com.example.fitnessapp

import android.app.Application
import com.example.fitnessapp.storage.FitnessRepository
import com.example.fitnessapp.storage.FitnessRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class FitnessApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { FitnessRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { FitnessRepository(database.routePointDao(), database.runDao()) }
}