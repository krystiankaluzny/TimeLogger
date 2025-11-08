package org.obywatelgcc.timelogger

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.obywatelgcc.timelogger.core.presentation.SnackbarMessageBus
import org.obywatelgcc.timelogger.core.presentation.components.ObserveAsEvents
import org.obywatelgcc.timelogger.settings.SettingsScreen
import org.obywatelgcc.timelogger.statistics.StatisticsScreen
import org.obywatelgcc.timelogger.timer.presentation.TimerEffect
import org.obywatelgcc.timelogger.timer.presentation.TimerScreen
import org.obywatelgcc.timelogger.timer.presentation.components.Clock
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme

private val navItems = listOf(
    NavigationItem(
        title = "Time Logger",
        icon = Icons.Filled.Clock,
        destiny = Screen.TimeLogger
    ),
    NavigationItem(
        title = "Statistics",
        icon = Icons.Filled.Info,
        destiny = Screen.Statistics
    ),
    NavigationItem(
        title = "Settings",
        icon = Icons.Filled.Settings,
        destiny = Screen.Settings
    )
)

@Composable
fun MainScreen() {

    val snackbarMessageBus = koinInject<SnackbarMessageBus>()

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val appName = stringResource(id = R.string.app_name)

    ObserveAsEvents(snackbarMessageBus.effectsFlow) {
        snackbarHostState.showSnackbar(it.message, it.actionLabel, false, it.duration)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController, appName, drawerState, navItems)
        }
    ) {
        Scaffold(
            topBar = { TopBarView(appName, drawerState) },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            NavHostView(navController, innerPadding)
        }
    }
}

@Composable
fun DrawerContent(
    navController: NavHostController,
    appName: String,
    drawerState: DrawerState,
    navigationItems: List<NavigationItem>
) {
    val scope = rememberCoroutineScope()

    ModalDrawerSheet(
        modifier = Modifier.width(280.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = appName,
            modifier = Modifier
                .padding(16.dp)
                .clickable(onClick = {
                    scope.launch {
                        if (drawerState.isClosed) drawerState.open()
                        else drawerState.close()
                    }
                }),
            style = TimeLoggerTheme.typography.titleLarge
        )
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

        navigationItems.forEachIndexed { index, item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = (index == selectedItemIndex),
                onClick = {
                    if (index != selectedItemIndex) navController.navigate(item.destiny)

                    selectedItemIndex = index
                    scope.launch {
                        if (drawerState.isOpen) drawerState.close()
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarView(appName: String, drawerState: DrawerState) {
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val expandedHeight = when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> TopAppBarDefaults.TopAppBarExpandedHeight
        else -> TopAppBarDefaults.TopAppBarExpandedHeight / 2
    }

    TopAppBar(
        title = { Text(text = appName) },
        expandedHeight = expandedHeight,
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    if (drawerState.isClosed) drawerState.open()
                    else drawerState.close()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Nav menu"
                )
            }
        }
    )
}

@Composable
private fun NavHostView(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(innerPadding),
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.TimeLogger
        ) {
            composable<Screen.TimeLogger> {
                TimerScreen()
            }
            composable<Screen.Statistics> {
                StatisticsScreen()
            }
            composable<Screen.Settings> {
                SettingsScreen()
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val destiny: Screen
)