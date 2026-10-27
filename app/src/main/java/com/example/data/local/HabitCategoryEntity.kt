package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "habit_categories")
data class HabitCategoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorHex: String = "#6366F1",
    val iconName: String = "folder",
    val orderIndex: Int = 0,
    val isDefault: Boolean = false
)
