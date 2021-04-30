package com.example.fitnessapp.ui.scheduler

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Schedule::class], version = 1, exportSchema = false)
abstract class ScheduleRoomDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao

    private class ScheduleDatabaseCallback(
            private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val scheduleDao = database.scheduleDao()

                    scheduleDao.deleteAll()

                    var schedule = Schedule(
                            0,
                            "running",
                            "May 1, 2021",
                            "16:00",
                            "18:00"
                    )
                    scheduleDao.insert(schedule)

                    schedule = Schedule(
                            0,
                            "cycling",
                            "May 2, 2021",
                            "16:00",
                            "18:00"
                    )
                    scheduleDao.insert(schedule)
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ScheduleRoomDatabase? = null

        fun getDatabase(
                context: Context,
                scope: CoroutineScope
        ): ScheduleRoomDatabase {
            return INSTANCE?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ScheduleRoomDatabase::class.java,
                        "schedule_database"
                )
                        .addCallback(ScheduleDatabaseCallback(scope))
                        .build()

                INSTANCE = instance

                instance
            }
        }
    }
}