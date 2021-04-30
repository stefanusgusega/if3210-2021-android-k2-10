package com.example.fitnessapp.ui.scheduler

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val scheduleDao: ScheduleDao) {
    val allSchedules : Flow<List<Schedule>> = scheduleDao.getAllSchedules()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(schedule: Schedule) {
        scheduleDao.insert(schedule)
    }
}