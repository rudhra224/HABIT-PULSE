package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HabitCategoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var categoryDao: HabitCategoryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryDao = database.habitCategoryDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveCategories(): Unit = runBlocking {
        val cat1 = HabitCategoryEntity(id = "c1", name = "Health", colorHex = "#10B981")
        val cat2 = HabitCategoryEntity(id = "c2", name = "Work", colorHex = "#38BDF8")
        val cat3 = HabitCategoryEntity(id = "c3", name = "Study", colorHex = "#6366F1")

        categoryDao.insertCategories(listOf(cat1, cat2, cat3))

        val list = categoryDao.getAllCategories().first()
        assertEquals(3, list.size)
        assertTrue(list.any { it.name == "Health" })
        assertTrue(list.any { it.name == "Work" })
        assertTrue(list.any { it.name == "Study" })
    }

    @Test
    fun testDeleteCategoryById(): Unit = runBlocking {
        val cat = HabitCategoryEntity(id = "c_delete", name = "Temporary", isDefault = false)
        categoryDao.insertCategory(cat)

        var list = categoryDao.getAllCategories().first()
        assertEquals(1, list.size)

        categoryDao.deleteCategoryById("c_delete")
        list = categoryDao.getAllCategories().first()
        assertEquals(0, list.size)
    }
}
