const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: "127.0.0.1",
      port: 8085,
      rules: fs.readFileSync("firestore.rules", "utf8"),
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("unauthenticated user cannot read user profile", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("users").doc("alice").get());
});

test("authenticated user can read and write own profile", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  const userDoc = aliceDb.collection("users").doc("alice");
  await assertSucceeds(
    userDoc.set({
      userId: "alice",
      level: 1,
      xp: 0,
      streakFreezes: 1,
      badges: [],
      displayName: "Alice",
      email: "alice@example.com",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(userDoc.get());
});

test("user cannot write to another user profile", async () => {
  const bobDb = testEnv.authenticatedContext("bob").firestore();
  await assertFails(
    bobDb.collection("users").doc("alice").set({
      userId: "bob",
      level: 1,
      xp: 0,
      streakFreezes: 0,
      badges: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("user can create and read own habit", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  const habitDoc = aliceDb.collection("users").doc("alice").collection("habits").doc("habit1");
  await assertSucceeds(
    habitDoc.set({
      id: "habit1",
      userId: "alice",
      title: "Morning Run",
      category: "Fitness",
      colorHex: "#10B981",
      iconName: "fitness",
      frequency: "daily",
      targetDays: [1, 2, 3, 4, 5, 6, 7],
      targetCount: 1,
      unit: "times",
      currentStreak: 0,
      longestStreak: 0,
      totalCompletions: 0,
      reminderTime: "07:00",
      isArchived: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(habitDoc.get());
});

test("user cannot read another user habit", async () => {
  // Setup habit as admin
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc("alice").collection("habits").doc("habit1").set({
      id: "habit1",
      userId: "alice",
      title: "Secret Run",
      category: "Fitness",
      colorHex: "#10B981",
      iconName: "fitness",
      frequency: "daily",
      targetDays: [1],
      targetCount: 1,
      unit: "times",
      currentStreak: 0,
      longestStreak: 0,
      totalCompletions: 0,
      isArchived: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext("bob").firestore();
  await assertFails(bobDb.collection("users").doc("alice").collection("habits").doc("habit1").get());
});
