package com.example.localguide_planner.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.localguide_planner.data.local.db.place.PlaceDao
import com.example.localguide_planner.data.local.db.place.PlaceEntity

@Database(entities = [PlaceEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun placeDao(): PlaceDao

    companion object {
        const val DATABASE_NAME = "localguide_db"
    }
}
