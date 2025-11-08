package org.obywatelgcc.timelogger.timer.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.R
import org.obywatelgcc.timelogger.timer.presentation.TimerAction.UpdateSavingType
import org.obywatelgcc.timelogger.timer.presentation.components.Clock
import org.obywatelgcc.timelogger.timer.presentation.screens.LoadingScreen
import org.obywatelgcc.timelogger.timer.presentation.screens.Screen
import org.obywatelgcc.timelogger.timer.presentation.screens.TimeLoggerScreen
import org.obywatelgcc.timelogger.timer.presentation.settings.SettingsState
import org.obywatelgcc.timelogger.timer.presentation.settings.SettingsState.SavingType

typealias OnAction = (TimerAction) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootTimerScreen(viewModel: TimerViewModel) {

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val appName = stringResource(id = R.string.app_name)
    val calenderState by viewModel.calendarState.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val titleState by viewModel.titleState.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.effectsFlow) {
        when (it) {
            is TimerEffect.ValidationError -> snackbarHostState.showSnackbar(
                it.message,
                "OK",
                duration = SnackbarDuration.Short
            )

            is TimerEffect.SavingMessage -> snackbarHostState.showSnackbar(
                it.message,
                "OK",
                duration = SnackbarDuration.Short
            )
        }
    }

    val navItems = listOf(
        NavigationItem(
            title = "Time Logger",
            icon = Icons.Filled.Clock,
            destiny = Screen.TimeLogger
        ),
        NavigationItem(
            title = "Statistics",
            icon = Icons.Filled.Info,
            destiny = Screen.Statistics
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RootTimerDrawerContent(appName, drawerState, navItems)
        }
    ) {
        Scaffold(
            topBar = { TopBarView(settingsState, appName, drawerState, viewModel::onAction) },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            val initialized by viewModel.initialized.collectAsStateWithLifecycle()

            if (initialized) {
                TimeLoggerScreen(
                    calenderState,
                    timerState,
                    titleState,
                    settingsState,
                    innerPadding,
                    viewModel::onAction
                )
            } else {
                LoadingScreen(innerPadding)
            }
        }
    }
}


@Composable
private fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: suspend (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(onEvent)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarView(settingsState: SettingsState, appName: String, drawerState: DrawerState, onAction: OnAction) {
    var expandedMenu by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val expandedHeight = when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> TopAppBarDefaults.TopAppBarExpandedHeight
        else -> TopAppBarDefaults.TopAppBarExpandedHeight / 2
    }

    val currentSavingType = settingsState.savingType

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
        },
        actions = {
            IconButton(onClick = { expandedMenu = !expandedMenu }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Settings")
            }

            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false }
            ) {
                SavingTypeMenuItem(currentSavingType, SavingType.SaveOnly, onAction)
                SavingTypeMenuItem(currentSavingType, SavingType.SaveAndStartNew, onAction)
            }
        }
    )
}

@Composable
private fun SavingTypeMenuItem(
    currentSavingType: SavingType,
    savingType: SavingType,
    onAction: OnAction
) {
    val selected = (currentSavingType == savingType)

    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .selectable(
                        selected = selected,
                        onClick = { onAction(UpdateSavingType(savingType)) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected, onClick = null)
                Text(text = savingType.description, modifier = Modifier.padding(start = 10.dp))
            }
        },
        onClick = { }
    )
}
