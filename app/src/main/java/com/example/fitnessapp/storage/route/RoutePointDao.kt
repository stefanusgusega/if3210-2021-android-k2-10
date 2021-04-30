package com.example.fitnessapp.storage.route

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePointDao {
    @Query("SELECT * FROM route_point_table WHERE run_id = :runId ORDER BY point_id ASC")
    fun get(runId : Int) : List<RoutePoint>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(routePoint: RoutePoint)

    @Query("DELETE FROM route_point_table")
    suspend fun deleteAll()
}