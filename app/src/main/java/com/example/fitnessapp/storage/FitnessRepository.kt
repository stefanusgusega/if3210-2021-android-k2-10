package com.example.fitnessapp.storage

import androidx.annotation.WorkerThread
import com.example.fitnessapp.storage.route.RoutePoint
import com.example.fitnessapp.storage.route.RoutePointDao
import com.example.fitnessapp.storage.run.Run
import com.example.fitnessapp.storage.run.RunDao

class FitnessRepository(val routePointDao: RoutePointDao, val runDao: RunDao) {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(routePoint: RoutePoint) {
        routePointDao.insert(routePoint)
    }

    @WorkerThread
    suspend fun insertRun(run: Run) {
        runDao.insert(run)
    }
}