package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import com.example.model.AppThemeMode
import com.example.model.AuthScreenMode
import com.example.model.MainNavigationTab
import com.example.ui.CivicNexusViewModel
import com.example.ui.SubScreen
import com.example.ui.components.CivicBottomNavigation
import com.example.ui.components.CivicTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CivicNexusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                CivicNexusApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CivicNexusApp(viewModel: CivicNexusViewModel) {
    val authState by viewModel.authUiState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val currentSubScreen by viewModel.currentSubScreen.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifCount = notifications.count { !it.isRead }

    // 1. Unauthenticated Gate: Citizen Login and Registration Flow
    if (!authState.isAuthenticated) {
        AnimatedContent(
            targetState = authState.authScreenMode,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "auth_screen_transition"
        ) { mode ->
            when (mode) {
                AuthScreenMode.LOGIN -> {
                    CitizenLoginScreen(viewModel = viewModel)
                }
                AuthScreenMode.REGISTER -> {
                    CitizenRegisterScreen(viewModel = viewModel)
                }
            }
        }
        return
    }

    // 2. Authenticated Citizen Dashboard
    BackHandler(enabled = currentSubScreen !is SubScreen.None || currentTab != MainNavigationTab.HOME) {
        viewModel.handleBackPress()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (currentSubScreen is SubScreen.None) {
                CivicTopAppBar(
                    unreadNotifCount = unreadNotifCount,
                    onEmergencyClick = { viewModel.openSubScreen(SubScreen.EmergencyCenter) },
                    onNotificationsClick = { viewModel.openSubScreen(SubScreen.Notifications) }
                )
            }
        },
        bottomBar = {
            if (currentSubScreen is SubScreen.None) {
                CivicBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentSubScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { subScreen ->
                when (subScreen) {
                    is SubScreen.EmergencyCenter -> {
                        EmergencyScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.closeSubScreen() }
                        )
                    }
                    is SubScreen.MyComplaints -> {
                        ComplaintsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.closeSubScreen() }
                        )
                    }
                    is SubScreen.ComplaintDetail -> {
                        ComplaintsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.closeSubScreen() }
                        )
                    }
                    is SubScreen.Notifications -> {
                        NotificationsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.closeSubScreen() }
                        )
                    }
                    is SubScreen.None -> {
                        when (currentTab) {
                            MainNavigationTab.HOME -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToReport = { viewModel.selectTab(MainNavigationTab.REPORT) },
                                    onNavigateToMap = { viewModel.selectTab(MainNavigationTab.MAP) },
                                    onNavigateToAssistant = { viewModel.selectTab(MainNavigationTab.ASSISTANT) },
                                    onOpenEmergency = { viewModel.openSubScreen(SubScreen.EmergencyCenter) },
                                    onOpenComplaints = { viewModel.openSubScreen(SubScreen.MyComplaints) }
                                )
                            }
                            MainNavigationTab.MAP -> {
                                MapScreen(viewModel = viewModel)
                            }
                            MainNavigationTab.REPORT -> {
                                ReportScreen(viewModel = viewModel)
                            }
                            MainNavigationTab.ASSISTANT -> {
                                AiAssistantScreen(
                                    viewModel = viewModel,
                                    onNavigateToReport = { viewModel.selectTab(MainNavigationTab.REPORT) }
                                )
                            }
                            MainNavigationTab.PROFILE -> {
                                ProfileScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
