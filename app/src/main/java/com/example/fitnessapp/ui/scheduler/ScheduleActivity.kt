package com.example.fitnessapp.ui.scheduler

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ScheduleActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        val activityScope = CoroutineScope(SupervisorJob())
        val database by lazy { ScheduleRoomDatabase.getDatabase(this, activityScope) }
        val repository by lazy { ScheduleRepository(database.scheduleDao()) }
    }
}