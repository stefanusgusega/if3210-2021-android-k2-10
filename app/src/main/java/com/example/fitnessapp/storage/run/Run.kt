package com.example.fitnessapp.storage.run

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_table")
data class Run(
    @PrimaryKey @ColumnInfo(name = "run_id") val runId: Int,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "value") val value: Float
)
