package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Habit
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class HabitRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun testAuthenticatedUserCanCreateAndObserveHabit(): Unit = runBlocking {
        val uid = signInTestUser("alice_${UUID.randomUUID()}@example.com")
        val repository = HabitRepository(firestore, auth)

        val newHabit = Habit(
            id = "habit_${UUID.randomUUID()}",
            userId = uid,
            title = "Morning Meditation",
            category = "Mindfulness",
            colorHex = "#6366F1",
            iconName = "star",
            frequency = "daily",
            targetDays = listOf(1, 2, 3, 4, 5, 6, 7),
            targetCount = 1L,
            unit = "times"
        )

        repository.saveHabit(newHabit)

        val habits = repository.observeHabits(uid).first()
        assertTrue("Habits should contain newly saved habit", habits.any { it.title == "Morning Meditation" })
    }

    @Test
    fun testUnauthenticatedUserCannotSaveHabit(): Unit = runBlocking {
        auth.signOut()
        val repository = HabitRepository(firestore, auth)

        val unauthedHabit = Habit(
            id = "habit_unauth",
            userId = "someone",
            title = "Unauthorized Habit"
        )

        try {
            repository.saveHabit(unauthedHabit)
            fail("Expected exception when unauthenticated")
        } catch (e: Exception) {
            assertTrue(
                "Should fail with IllegalStateException or permission denied",
                e is IllegalStateException || e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
    }

    @Test
    fun testCrossUserCannotWriteToAnotherUserSubcollection(): Unit = runBlocking {
        val aliceUid = signInTestUser("alice_${UUID.randomUUID()}@example.com")
        val bobUid = signInTestUser("bob_${UUID.randomUUID()}@example.com")

        // Alice attempts to write directly into Bob's subcollection
        try {
            firestore.collection("users").document(bobUid).collection("habits").document("h1").set(
                mapOf(
                    "id" to "h1",
                    "userId" to aliceUid,
                    "title" to "Malicious Habit"
                )
            ).await()
            fail("Alice should not be allowed to write to Bob's habits")
        } catch (e: Exception) {
            // Expected PERMISSION_DENIED
            assertTrue(
                "Expected permission denied, got ${e.message}",
                e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
    }
}
