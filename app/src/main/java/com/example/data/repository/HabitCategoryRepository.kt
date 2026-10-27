package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.HabitCategoryDao
import com.example.data.local.HabitCategoryEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class HabitCategoryRepository(private val categoryDao: HabitCategoryDao) {

    val allCategories: Flow<List<HabitCategoryEntity>> = categoryDao.getAllCategories()

    suspend fun ensureDefaultCategories() {
        if (categoryDao.getCategoryCount() == 0) {
            categoryDao.insertCategories(AppDatabase.DEFAULT_CATEGORIES)
        }
    }

    suspend fun addCategory(name: String, colorHex: String = "#6366F1", iconName: String = "folder") {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val entity = HabitCategoryEntity(
            id = UUID.randomUUID().toString(),
            name = trimmed,
            colorHex = colorHex,
            iconName = iconName,
            orderIndex = 10,
            isDefault = false
        )
        categoryDao.insertCategory(entity)
    }

    suspend fun updateCategory(category: HabitCategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(id: String) {
        categoryDao.deleteCategoryById(id)
    }
}
