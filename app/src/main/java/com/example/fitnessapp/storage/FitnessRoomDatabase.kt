package com.example.fitnessapp.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fitnessapp.storage.route.RoutePoint
import com.example.fitnessapp.storage.route.RoutePointDao
import com.example.fitnessapp.storage.run.Run
import com.example.fitnessapp.storage.run.RunDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [RoutePoint::class, Run::class], version = 1, exportSchema = false)
abstract class FitnessRoomDatabase : RoomDatabase() {

    abstract fun routePointDao() : RoutePointDao
    abstract fun runDao() : RunDao

    private class RouteDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val routePointDao = database.routePointDao()
                    val runDao = database.runDao()

                    routePointDao.deleteAll()
                    runDao.deleteAll()
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FitnessRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): FitnessRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessRoomDatabase::class.java,
                    "route_point_database"
                )
                    .addCallback(RouteDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}