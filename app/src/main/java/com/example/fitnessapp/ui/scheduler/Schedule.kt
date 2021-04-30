package com.example.fitnessapp.ui.scheduler

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_table")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo val type: String,
    @ColumnInfo val day: String,
    @ColumnInfo val startTime: String,
    @ColumnInfo val endTime: String
)