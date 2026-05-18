package com.afterlight.madeproject.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.compose.navigation
import com.afterlight.madeproject.ui.theme.*

@Composable
fun GatherApp(
    launchViewModel: LaunchViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination by launchViewModel.destination.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val tabs = listOf(
        Routes.Home to Pair("Home", Icons.Default.Home),
        Routes.Discover to Pair("Discover", Icons.Default.Search),
        Routes.MyEvents to Pair("My Events", Icons.Default.DateRange),
        Routes.Notifications to Pair("Alerts", Icons.Default.Notifications),
        Routes.Settings to Pair("Settings", Icons.Default.Settings)
    )

    val showBottomBar = tabs.any { it.first == currentRoute }

    Scaffold(
        containerColor = Snow,
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .neoBrutalism(
                                backgroundColor = Coal,
                                borderColor = Coal,
                                shadowOffset = 6.dp,
                                cornerRadius = 28.dp
                            )
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEach { (route, metadata) ->
                            val selected = route == currentRoute

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (selected) Snow else Color.Transparent)
                                    .clickable {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = metadata.second,
                                            contentDescription = metadata.first,
                                            tint = if (selected) Coal else Snow.copy(alpha = 0.72f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        AnimatedVisibility(visible = selected) {
                                            Text(
                                                text = metadata.first,
                                                style = GatherTypography.labelMedium,
                                                color = Coal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Splash,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                if (targetState.destination.route?.contains("event_detail") == true) {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn()
                } else {
                    fadeIn()
                }
            },
            exitTransition = {
                if (initialState.destination.route?.contains("event_detail") == true) {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) + fadeOut()
                } else {
                    fadeOut()
                }
            }
        ) {
            composable(Routes.Splash) {
                SplashScreen {
                    navController.navigate(startDestination) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            }
            composable(Routes.Onboarding) {
                OnboardingScreen(onFinish = { navController.navigate(Routes.Auth) })
            }
            composable(Routes.Auth) {
                AuthScreen(
                    onProfileSetup = { navController.navigate("profile_flow") }
                )
            }
            navigation(
                startDestination = Routes.ProfileSetup,
                route = "profile_flow"
            ) {
                composable(Routes.ProfileSetup) { backStackEntry ->
                    val parentEntry = navController.getBackStackEntry("profile_flow")
                    val sharedViewModel: ProfileSetupViewModel = hiltViewModel(parentEntry)
                    ProfileSetupScreen(onNext = { navController.navigate(Routes.RoleSelect) }, viewModel = sharedViewModel)
                }
                composable(Routes.RoleSelect) { backStackEntry ->
                    val parentEntry = navController.getBackStackEntry("profile_flow")
                    val sharedViewModel: ProfileSetupViewModel = hiltViewModel(parentEntry)
                    RoleSelectionScreen(
                        onFinish = {
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Auth) { inclusive = true }
                            }
                        },
                        viewModel = sharedViewModel
                    )
                }
            }
            composable(Routes.Home) {
                HomeScreen(
                    onEventClick = { navController.navigate(Routes.eventDetail(it)) },
                    onHostClick = { navController.navigate(Routes.HostEvent) }
                )
            }
            composable(Routes.Discover) {
                DiscoverScreen(onEventClick = { navController.navigate(Routes.eventDetail(it)) })
            }
            composable(Routes.MyEvents) {
                MyEventsScreen(
                    onEventClick = { navController.navigate(Routes.eventDetail(it)) },
                    onHostControlsClick = { navController.navigate(Routes.hostControls(it)) }
                )
            }
            composable(Routes.Notifications) {
                NotificationsScreen()
            }
            composable(Routes.Settings) {
                SettingsScreen(onAccountClick = { navController.navigate(Routes.Accounts) })
            }
            composable(Routes.Accounts) {
                AccountsScreen(
                    onBackClick = { navController.popBackStack() },
                    onMyEventsClick = { navController.navigate(Routes.MyEvents) },
                    onHostClick = { navController.navigate(Routes.HostEvent) },
                    onSettingsClick = { navController.navigate(Routes.Settings) },
                    onSignedOut = {
                        navController.navigate(Routes.Auth) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = Routes.EventDetail,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
                deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "https://paperlike.app/events/{eventId}" })
            ) {
                EventDetailScreen(
                    onRecapClick = { eventId -> navController.navigate(Routes.recapWall(eventId)) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Routes.HostEvent) {
                HostEventScreen(onPublished = { eventId -> navController.navigate(Routes.eventDetail(eventId)) })
            }
            composable(
                route = Routes.HostControls,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) {
                HostControlsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(
                route = Routes.RecapWall,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) {
                RecapWallScreen()
            }
        }
    }
}
