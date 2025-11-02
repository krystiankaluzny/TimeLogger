package org.obywatelgcc.timelogger.timer.presentation

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.R
import org.obywatelgcc.timelogger.timer.model.Calendar
import org.obywatelgcc.timelogger.timer.model.CalendarEventColor
import org.obywatelgcc.timelogger.timer.presentation.TimerAction.UpdateSavingType
import org.obywatelgcc.timelogger.timer.presentation.calendar.CalendarState
import org.obywatelgcc.timelogger.timer.presentation.components.DateTimePickersView
import org.obywatelgcc.timelogger.timer.presentation.settings.SettingsState
import org.obywatelgcc.timelogger.timer.presentation.settings.SettingsState.SavingType
import org.obywatelgcc.timelogger.timer.presentation.timer.TimerState
import org.obywatelgcc.timelogger.timer.presentation.title.TitleState
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerView(appName, drawerState)
        },
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


@Composable
private fun ModalDrawerView(
    appName: String,
    drawerState: DrawerState
) {
    val scope = rememberCoroutineScope()

    ModalDrawerSheet {
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
            style = MaterialTheme.typography.titleLarge
        )
        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text(text = "TODO") },
            selected = false,
            onClick = {
                scope.launch {
                    if (drawerState.isClosed) drawerState.open()
                    else drawerState.close()
                }
            }
        )
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
                SavingTypeMenuItem(currentSavingType, SavingType.SAVE_ONLY, "Save only", onAction)
                SavingTypeMenuItem(currentSavingType, SavingType.SAVE_AND_START, "Save and start new event", onAction)
                SavingTypeMenuItem(
                    currentSavingType,
                    SavingType.SAVE_START_AND_CHANGE_COLOR,
                    "Save, start and change change event color",
                    onAction
                )
            }
        }
    )
}

@Composable
private fun SavingTypeMenuItem(
    currentSavingType: SavingType,
    savingType: SavingType,
    text: String,
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
                Text(text = text, modifier = Modifier.padding(start = 10.dp))
            }
        },
        onClick = { }
    )
}

@Composable
private fun LoadingScreen(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun TimeLoggerScreen(
    calendarState: CalendarState,
    timerState: TimerState,
    titleState: TitleState,
    settingsState: SettingsState,
    innerPadding: PaddingValues,
    onAction: OnAction
) {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> TimeLoggerPortraitScreen(
            calendarState,
            timerState,
            titleState,
            settingsState,
            innerPadding,
            onAction
        )

        else -> TimeLoggerLandscapeScreen(calendarState, timerState, titleState, settingsState, innerPadding, onAction)
    }
}

@Composable
fun TimeLoggerPortraitScreen(
    calendarState: CalendarState,
    timerState: TimerState,
    titleState: TitleState,
    settingsState: SettingsState,
    innerPadding: PaddingValues,
    onAction: OnAction
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(innerPadding),
    ) {
        val calendars = calendarState.availableCalendars
        val selectedCalendar = calendarState.selectedCalendar

        CalendarDropdown(calendars, selectedCalendar, { onAction(TimerAction.SelectCalendar(it)) })
        TitleAndColorView(
            titleState,
            calendarState.availableColors,
            calendarState.selectedColor,
            onAction
        )

        HorizontalDivider()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(weight = 1f, fill = false)
        ) {
            DateTimePickersView(
                timerState.startDateTime,
                timerState.endDateTime,
                timerState.runningState != TimerState.RunningState.STARTED,
                onAction
            )
            HorizontalDivider()
            DurationView(timerState.durationStr)
        }

        HorizontalDivider()
        TimeLoggerButtonsView(timerState.runningState, onAction)

        Spacer(Modifier.height(20.dp))

        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(70.dp),
            onClick = { onAction(TimerAction.TrySave) }
        ) {
            Text(
                text = when (settingsState.savingType) {
                    SavingType.SAVE_ONLY -> "Save"
                    SavingType.SAVE_AND_START -> "Save and start"
                    SavingType.SAVE_START_AND_CHANGE_COLOR -> "Save, start and change color"
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TimeLoggerLandscapeScreen(
    calendarState: CalendarState,
    timerState: TimerState,
    titleState: TitleState,
    settingsState: SettingsState,
    innerPadding: PaddingValues,
    onAction: OnAction
) {

    Column(
        modifier = Modifier.padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val calendars = calendarState.availableCalendars
            val selectedCalendar = calendarState.selectedCalendar

            Box(modifier = Modifier.weight(1.0f)) {
                CalendarDropdown(calendars, selectedCalendar, { onAction(TimerAction.SelectCalendar(it)) })
            }
            Box(modifier = Modifier.weight(1.0f)) {
                TitleAndColorView(
                    titleState,
                    calendarState.availableColors,
                    calendarState.selectedColor,
                    onAction
                )
            }
        }
        HorizontalDivider()

        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(weight = 1f, fill = false),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DateTimePickersView(
                    timerState.startDateTime,
                    timerState.endDateTime,
                    timerState.runningState != TimerState.RunningState.STARTED,
                    onAction
                )
                HorizontalDivider()
                DurationView(timerState.durationStr)
            }

            Column(
                modifier = Modifier.weight(1.0f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(10.dp))
                TimeLoggerButtonsView(timerState.runningState, onAction)

                Spacer(Modifier.height(10.dp))

                FilledTonalButton(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(70.dp),
                    onClick = { onAction(TimerAction.TrySave) }
                ) {
                    Text(
                        text = when (settingsState.savingType) {
                            SavingType.SAVE_ONLY -> "Save"
                            SavingType.SAVE_AND_START -> "Save and start"
                            SavingType.SAVE_START_AND_CHANGE_COLOR -> "Save, start and change color"
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CalendarDropdown(
    calendars: List<Calendar>,
    selected: Calendar?,
    onSelect: (Calendar) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldState by remember { mutableStateOf(selected?.description() ?: "") }

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = it },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        TextField(
            value = textFieldState,
            onValueChange = { },
            singleLine = true,
            label = { Text("Calendar") },
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. A read-only text field has
            // the anchor type `PrimaryNotEditable`.
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            calendars.forEach { c ->
                DropdownMenuItem(onClick = {
                    textFieldState = c.description()
                    onSelect(c)
                    expanded = false
                }, text = {
                    Text(text = c.description())
                })
            }
        }
    }
}

@Composable
private fun TitleAndColorView(
    titleState: TitleState,
    colors: List<CalendarEventColor>,
    selectedColor: CalendarEventColor,
    onAction: OnAction
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 10.dp, bottom = 10.dp, end = 10.dp)
            .fillMaxWidth()
    ) {
        EventTitleTextField(titleState, onAction)
        ColorDropdown(colors, selectedColor, { onAction(TimerAction.SelectColor(it)) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.EventTitleTextField(
    titleState: TitleState,
    onAction: OnAction
) {
    val eventTitle = titleState.eventTitle
    val titleSuggestions = titleState.suggestions

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = {
            expanded = it
        },
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .weight(1.0f)
    ) {
        OutlinedTextField(
            value = eventTitle,
            onValueChange = {
                onAction(TimerAction.UpdateTitle(it))
                expanded = true
            },
            label = { Text(text = "Event title") },
            singleLine = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
        )

        if (titleSuggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded, onDismissRequest = { }
            ) {
                titleSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        onClick = {
                            onAction(TimerAction.SelectSuggestion(suggestion))
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1.0f)
                                ) {
                                    Text(text = suggestion.prefix)
                                    Text(text = suggestion.match, fontWeight = FontWeight.ExtraBold)
                                    Text(text = suggestion.suffix)
                                }

                                ColorHolder(
                                    color = suggestion.color,
                                    size = 30.dp,
                                    onClick = {},
                                    showBorder = false
                                )
                            }
                        }
                    )
                }
            }
        }
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RowScope.ColorDropdown(
    colors: List<CalendarEventColor>,
    selectedColor: CalendarEventColor?,
    onSelect: (CalendarEventColor) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val mainColorButtonSize = 40.dp

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = it },
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .width(mainColorButtonSize)
    ) {

        ColorHolder(
            color = selectedColor,
            size = mainColorButtonSize,
            onClick = { expanded = true },
            showBorder = false
        )

        val itemWidth = 55.dp
        val itemHeight = 40.dp
        val columnCount = 6
        val colorButtonSize = itemWidth - 25.dp

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.width(itemWidth * columnCount)
        ) {
            for (rowId in 0..<Math.ceilDiv(colors.size, columnCount)) {
                Row(
                    modifier = Modifier
                ) {
                    for (columnId in 0..<columnCount) {

                        val index = rowId * columnCount + columnId
                        if (index < colors.size) {
                            val c = colors[index]
                            DropdownMenuItem(
                                onClick = {
                                    //handle in Surface.onClick
                                },
                                text = {

                                    ColorHolder(
                                        color = c,
                                        size = colorButtonSize,
                                        onClick = {
                                            onSelect(c)
                                            expanded = false
                                        },
                                        showBorder = c == selectedColor
                                    )
                                },
                                modifier = Modifier
                                    .width(itemWidth)
                                    .height(itemHeight)
                            )
                        }

                    }
                } // end row
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.ColorHolder(
    color: CalendarEventColor?,
    size: Dp,
    onClick: () -> Unit,
    showBorder: Boolean = true
) {
    val border = if (showBorder) BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
    ) else null

    ElevatedButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        shape = CircleShape,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = color?.colorAsLong()?.let { Color(it) } ?: Color.White
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
        border = border,
        content = {}
    )
}

@Composable
private fun DurationView(durationStr: String) {
    Text(
        text = "Duration: $durationStr",
        modifier = Modifier.padding(16.dp),
        fontSize = 20.sp
    )
}

@Composable
private fun TimeLoggerButtonsView(timerRunningState: TimerState.RunningState, onAction: OnAction) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        val buttonModifier = Modifier
            .weight(0.5f)
            .height(50.dp)

        val buttonElevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp)

        ElevatedButton(
            modifier = buttonModifier,
            onClick = { onAction(TimerAction.RestartTimer) },
            elevation = buttonElevation,
            enabled = (timerRunningState == TimerState.RunningState.READY_TO_START || timerRunningState == TimerState.RunningState.STOPPED)
        ) {
            Text(text = "Restart")
        }

        Spacer(Modifier.width(16.dp))

        when (timerRunningState) {
            TimerState.RunningState.READY_TO_START -> ElevatedButton(
                modifier = buttonModifier,
                onClick = { onAction(TimerAction.StartTimer) },
                elevation = buttonElevation,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TimeLoggerTheme.colors.timerToStartButton,
                    contentColor = TimeLoggerTheme.colors.timerButtonContent
                )
            ) {
                Text(text = "Start")
            }

            TimerState.RunningState.STARTED -> ElevatedButton(
                modifier = buttonModifier,
                onClick = { onAction(TimerAction.StopTimer) },
                elevation = buttonElevation,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TimeLoggerTheme.colors.timerToStopButton,
                    contentColor = TimeLoggerTheme.colors.timerButtonContent
                )
            ) {
                Text(text = "Stop")
            }

            TimerState.RunningState.STOPPED -> ElevatedButton(
                modifier = buttonModifier,
                onClick = { onAction(TimerAction.ResumeTimer) },
                elevation = buttonElevation,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TimeLoggerTheme.colors.timerToResumeButton,
                    contentColor = TimeLoggerTheme.colors.timerButtonContent
                )
            ) {
                Text(text = "Resume")
            }
        }
    }
}