package com.afterlight.madeproject.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.afterlight.madeproject.ui.theme.*

@Composable
fun GatherApp(
    launchViewModel: LaunchViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination by launchViewModel.destination.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val activity = LocalContext.current as ComponentActivity

    val tabs = listOf(
        Routes.Home to Pair("Home", Icons.Default.Home),
        Routes.Discover to Pair("Discover", Icons.Default.Search),
        Routes.MyEvents to Pair("Events", Icons.Default.AccountBalanceWallet),
        Routes.Notifications to Pair("Alerts", Icons.Default.Notifications),
        Routes.Settings to Pair("Settings", Icons.Default.Settings)
    )

    val showBottomBar = tabs.any { it.first == currentRoute }

    Scaffold(
        containerColor = Snow,
        bottomBar = {
            if (showBottomBar) {
                GatherBottomNav(
                    tabs = tabs,
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
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
                    onAuthenticated = { navController.navigate(Routes.Home) },
                    onProfileSetup = { navController.navigate(Routes.ProfileSetup) }
                )
            }
            composable(Routes.ProfileSetup) {
                val sharedViewModel: ProfileSetupViewModel = hiltViewModel(activity)
                ProfileSetupScreen(
                    onNext = { navController.navigate(Routes.RoleSelect) },
                    viewModel = sharedViewModel
                )
            }
            composable(Routes.RoleSelect) {
                val sharedViewModel: ProfileSetupViewModel = hiltViewModel(activity)
                RoleSelectionScreen(
                    onFinish = {
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.ProfileSetup) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    viewModel = sharedViewModel
                )
            }
            composable(Routes.Home) {
                HomeScreen(
                    onEventClick = { navController.navigate(Routes.eventDetail(it)) },
                    onHostClick = { navController.navigate(Routes.HostEvent) },
                    onScanClick = { navController.navigate(Routes.ScanRsvp) },
                    onPosterScanClick = { navController.navigate(Routes.PosterScan) }
                )
            }
            composable(Routes.ScanRsvp) {
                QrScanRsvpScreen(
                    onBackClick = { navController.popBackStack() },
                    onEventClick = { navController.navigate(Routes.eventDetail(it)) }
                )
            }
            composable(Routes.PosterScan) {
                PosterScanScreen(
                    onBackClick = { navController.popBackStack() },
                    onEventClick = { navController.navigate(Routes.eventDetail(it)) }
                )
            }
            composable(
                route = Routes.PaymentCheckout,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) {
                PaymentCheckoutScreen(onBackClick = { navController.popBackStack() })
            }
            composable(
                route = Routes.CheckIn,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) {
                QrCheckInScreen(onBackClick = { navController.popBackStack() })
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
                SettingsScreen(
                    onAccountClick = { navController.navigate(Routes.Accounts) },
                    onAiSettingsClick = { navController.navigate(Routes.AiSettings) }
                )
            }
            composable(Routes.AiSettings) {
                AiSettingsScreen(onBackClick = { navController.popBackStack() })
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
                    onPayClick = { eventId -> navController.navigate(Routes.paymentCheckout(eventId)) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Routes.HostEvent) {
                HostEventScreen(onPublished = { navController.navigate(Routes.Home) })
            }
            composable(
                route = Routes.HostControls,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) {
                HostControlsScreen(
                    onBackClick = { navController.popBackStack() },
                    onCheckInClick = { eventId -> navController.navigate(Routes.checkIn(eventId)) }
                )
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

/**
 * Floating pill-style bottom navigation bar.
 *
 * Design rationale:
 * - Floats above content on a Coal-colored pill with soft shadow — feels premium and deliberate.
 * - Selected item gets a warm Sand highlight pill with the label; unselected are icon-only ghosts.
 * - Animated label expand/collapse keeps it fluid without being distracting.
 * - Full-width background is transparent so the Snow scaffold color shows through at the edges,
 *   giving the pill a truly "floating" appearance.
 */
@Composable
fun GatherBottomNav(
    tabs: List<Pair<String, Pair<String, androidx.compose.ui.graphics.vector.ImageVector>>>,
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    // Transparent wrapper so the pill appears to float
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // The floating pill container
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    ambientColor = Coal.copy(alpha = 0.15f),
                    spotColor = Coal.copy(alpha = 0.25f)
                ),
            shape = CircleShape,
            color = Coal
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (route, metadata) ->
                    val selected = route == currentRoute
                    val interactionSource = remember { MutableInteractionSource() }
                    val inactiveIconTint = Snow.copy(alpha = 0.78f)
                    val navContentColor = inactiveIconTint

                    // Animate icon tint
                    val iconTint by animateColorAsState(
                        targetValue = navContentColor,
                        animationSpec = tween(durationMillis = 250),
                        label = "iconTint"
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null // handled visually by the pill bg
                            ) { onTabSelected(route) }
                    ) {
                        // Active background pill with animated width
                        androidx.compose.animation.AnimatedVisibility(
                            visible = selected,
                            enter = fadeIn(tween(200)) + expandHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ),
                            exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150))
                        ) {
                            // Warm Sand pill for active tab
                            Surface(
                                shape = CircleShape,
                                color = Sand,
                                modifier = Modifier.matchParentSize()
                            ) {}
                        }

                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = metadata.second,
                                contentDescription = metadata.first,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                            // Label slides in only for the selected tab
                            AnimatedVisibility(
                                visible = selected,
                                enter = fadeIn(tween(200)) + expandHorizontally(tween(220)),
                                exit = fadeOut(tween(120)) + shrinkHorizontally(tween(150))
                            ) {
                                Text(
                                    text = metadata.first,
                                    style = GatherTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = navContentColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}