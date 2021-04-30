package com.example.fitnessapp.storage.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_point_table", primaryKeys = ["run_id", "point_id"])
data class RoutePoint(
    @ColumnInfo(name="run_id") val runId : Int,
    @ColumnInfo(name="point_id") val pointId : Int,
    @ColumnInfo(name="latitude") val latitude : Double,
    @ColumnInfo(name="longitude") val longitude : Double
)
