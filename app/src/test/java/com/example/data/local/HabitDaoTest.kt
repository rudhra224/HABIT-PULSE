package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HabitDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var habitDao: HabitDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habitDao = database.habitDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveHabits(): Unit = runBlocking {
        val habit1 = HabitEntity(
            id = "h1",
            name = "Morning Workout",
            category = "Health",
            isCompleted = false
        )
        val habit2 = HabitEntity(
            id = "h2",
            name = "Read 30 mins",
            category = "Study",
            isCompleted = true
        )

        habitDao.insertHabit(habit1)
        habitDao.insertHabit(habit2)

        val all = habitDao.getAllHabits().first()
        assertEquals(2, all.size)

        val studyHabits = habitDao.getHabitsByCategory("Study").first()
        assertEquals(1, studyHabits.size)
        assertEquals("Read 30 mins", studyHabits[0].name)
        assertTrue(studyHabits[0].isCompleted)
    }

    @Test
    fun testBulkInsertAndCount(): Unit = runBlocking {
        val habits = listOf(
            HabitEntity(id = "b1", name = "Hydrate", category = "Health", isCompleted = false),
            HabitEntity(id = "b2", name = "Walk 10k", category = "Health", isCompleted = true),
            HabitEntity(id = "b3", name = "Plan Day", category = "Work", isCompleted = false)
        )
        habitDao.insertHabits(habits)

        val count = habitDao.getHabitCount()
        assertEquals(3, count)

        val completed = habitDao.getHabitsByCompletion(true).first()
        assertEquals(1, completed.size)
        assertEquals("Walk 10k", completed[0].name)

        val uncompleted = habitDao.getHabitsByCompletion(false).first()
        assertEquals(2, uncompleted.size)
    }

    @Test
    fun testGetHabitById(): Unit = runBlocking {
        val habit = HabitEntity(id = "h_unique", name = "Deep Work", category = "Work", isCompleted = false)
        habitDao.insertHabit(habit)

        val fetched = habitDao.getHabitById("h_unique")
        assertNotNull(fetched)
        assertEquals("Deep Work", fetched?.name)

        val observed = habitDao.observeHabitById("h_unique").first()
        assertNotNull(observed)
        assertEquals("Deep Work", observed?.name)

        val missing = habitDao.getHabitById("non_existent")
        assertNull(missing)
    }

    @Test
    fun testUpdateHabit(): Unit = runBlocking {
        val habit = HabitEntity(id = "h_up", name = "Old Name", category = "Health", isCompleted = false)
        habitDao.insertHabit(habit)

        val updated = habit.copy(name = "New Name", isCompleted = true)
        habitDao.updateHabit(updated)

        val retrieved = habitDao.getHabitById("h_up")
        assertEquals("New Name", retrieved?.name)
        assertTrue(retrieved?.isCompleted == true)
    }

    @Test
    fun testUpdateCompletion(): Unit = runBlocking {
        val habit = HabitEntity(id = "h3", name = "Code Review", category = "Work", isCompleted = false)
        habitDao.insertHabit(habit)

        var list = habitDao.getAllHabits().first()
        assertFalse(list[0].isCompleted)

        habitDao.updateCompletion("h3", true)
        list = habitDao.getAllHabits().first()
        assertTrue(list[0].isCompleted)
    }

    @Test
    fun testDeleteOperations(): Unit = runBlocking {
        val habitA = HabitEntity(id = "del_a", name = "Habit A", category = "Health")
        val habitB = HabitEntity(id = "del_b", name = "Habit B", category = "Study")
        val habitC = HabitEntity(id = "del_c", name = "Habit C", category = "Work")

        habitDao.insertHabits(listOf(habitA, habitB, habitC))
        assertEquals(3, habitDao.getHabitCount())

        // Delete by ID
        val rowsDeleted = habitDao.deleteHabitById("del_a")
        assertEquals(1, rowsDeleted)
        assertEquals(2, habitDao.getHabitCount())

        // Delete entity
        habitDao.deleteHabit(habitB)
        assertEquals(1, habitDao.getHabitCount())

        // Delete all
        habitDao.deleteAllHabits()
        assertEquals(0, habitDao.getHabitCount())
    }
}

