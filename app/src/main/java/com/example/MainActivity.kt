package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.NotificationHelper
import com.example.ui.HabitViewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.attemptAutoSignIn
import com.example.ui.screens.signOut
import com.example.ui.theme.HabitPulseTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

import androidx.compose.material.icons.filled.AutoAwesome
import com.example.ai.HabitCoachViewModel
import com.example.ai.configureAppCheck
import com.example.ui.screens.HabitCoachScreen

enum class NavTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TODAY("Today", Icons.Default.CheckCircle),
    ANALYTICS("Analytics", Icons.Default.BarChart),
    REWARDS("Rewards", Icons.Default.EmojiEvents),
    COACH("Coach", Icons.Default.AutoAwesome),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()
    private val coachViewModel: HabitCoachViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        configureAppCheck(this, intent)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            HabitPulseTheme(themeMode = uiState.themeMode) {
                AppRoot(
                    viewModel = viewModel,
                    coachViewModel = coachViewModel
                )
            }
        }
    }
}

@Composable
fun AppRoot(
    viewModel: HabitViewModel,
    coachViewModel: HabitCoachViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            auth.currentUser?.uid?.let { uid ->
                viewModel.initializeUserSession(uid)
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                    currentUser?.uid?.let { viewModel.initializeUserSession(it) }
                },
                onUnauthenticated = { },
                scope = coroutineScope
            )
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
                currentUser?.uid?.let { viewModel.initializeUserSession(it) }
            }
        )
    } else {
        MainAppContent(
            viewModel = viewModel,
            coachViewModel = coachViewModel,
            onSignOut = {
                signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}

@Composable
fun MainAppContent(
    viewModel: HabitViewModel,
    coachViewModel: HabitCoachViewModel,
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(NavTab.TODAY) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavTab.TODAY -> {
                    TodayScreen(
                        uiState = uiState,
                        onToggleHabit = { habit -> viewModel.toggleHabit(habit) },
                        onUpdateHabitCount = { habit, count -> viewModel.updateHabitProgress(habit, count) },
                        onSaveHabit = { habit -> viewModel.saveHabit(habit) },
                        onDeleteHabit = { habitId -> viewModel.deleteHabit(habitId) },
                        onSelectCategory = { cat -> viewModel.setSelectedCategory(cat) },
                        onAddCategory = { name, color -> viewModel.addCategory(name, color) }
                    )
                }
                NavTab.ANALYTICS -> {
                    AnalyticsScreen(
                        uiState = uiState
                    )
                }
                NavTab.REWARDS -> {
                    RewardsScreen(
                        uiState = uiState,
                        onBuyFreeze = { viewModel.purchaseStreakFreeze() }
                    )
                }
                NavTab.COACH -> {
                    HabitCoachScreen(
                        coachViewModel = coachViewModel
                    )
                }
                NavTab.SETTINGS -> {
                    SettingsScreen(
                        currentThemeMode = uiState.themeMode,
                        userProfile = uiState.userProfile,
                        categories = uiState.categories,
                        onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                        onSendTestReminder = { viewModel.sendTestReminder() },
                        onAddCategory = { name, color -> viewModel.addCategory(name, color) },
                        onDeleteCategory = { id -> viewModel.deleteCategory(id) },
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}
