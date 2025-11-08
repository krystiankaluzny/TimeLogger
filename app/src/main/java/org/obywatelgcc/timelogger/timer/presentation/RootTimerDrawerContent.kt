package org.obywatelgcc.timelogger.timer.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.timer.presentation.screens.Screen
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme

@Composable
fun RootTimerDrawerContent(
    navController: NavHostController,
    appName: String,
    drawerState: DrawerState,
    navigationItems: List<NavigationItem>
) {
    val scope = rememberCoroutineScope()

    ModalDrawerSheet(
        modifier = Modifier.width(250.dp)
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
                    if(index != selectedItemIndex) navController.navigate(item.destiny)

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

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val destiny: Screen
)