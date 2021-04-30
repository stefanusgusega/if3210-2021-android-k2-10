package com.example.fitnessapp.storage.run

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Query("SELECT * FROM run_table WHERE run_id = :runId LIMIT 1")
    fun get(runId : Int) : Flow<Run>

    @Query("SELECT (MAX(run_id) + 1) FROM run_table LIMIT 1")
    fun getNewRunId() : Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(run: Run)

    @Query("DELETE FROM run_table")
    suspend fun deleteAll()
}