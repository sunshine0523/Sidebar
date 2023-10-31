package io.sunshine0523.sidebar.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * @author sunshine
 * @date 2021/1/31
 */
@Database(entities = [SidebarAppsEntity::class], version = 5, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    abstract val sidebarAppsDao: SidebarAppsDao

    companion object {
        private var database: MyDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): MyDatabase {
            if (database == null) {
                database = Room.databaseBuilder(context.applicationContext, MyDatabase::class.java, "database.db")
                    .allowMainThreadQueries()
                    .build()
            }
            return database!!
        }
    }
}