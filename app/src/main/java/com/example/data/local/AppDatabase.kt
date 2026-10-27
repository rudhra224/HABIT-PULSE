package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [HabitCategoryEntity::class, HabitEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitCategoryDao(): HabitCategoryDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val DEFAULT_CATEGORIES = listOf(
            HabitCategoryEntity(id = "cat_health", name = "Health", colorHex = "#10B981", iconName = "favorite", orderIndex = 0, isDefault = true),
            HabitCategoryEntity(id = "cat_work", name = "Work", colorHex = "#38BDF8", iconName = "work", orderIndex = 1, isDefault = true),
            HabitCategoryEntity(id = "cat_study", name = "Study", colorHex = "#6366F1", iconName = "school", orderIndex = 2, isDefault = true),
            HabitCategoryEntity(id = "cat_fitness", name = "Fitness", colorHex = "#F43F5E", iconName = "fitness_center", orderIndex = 3, isDefault = true),
            HabitCategoryEntity(id = "cat_mindfulness", name = "Mindfulness", colorHex = "#8B5CF6", iconName = "self_improvement", orderIndex = 4, isDefault = true),
            HabitCategoryEntity(id = "cat_creative", name = "Creative", colorHex = "#F59E0B", iconName = "brush", orderIndex = 5, isDefault = true)
        )

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_pulse_local.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.habitCategoryDao()?.insertCategories(DEFAULT_CATEGORIES)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
