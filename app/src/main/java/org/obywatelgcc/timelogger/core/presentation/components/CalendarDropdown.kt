package org.obywatelgcc.timelogger.core.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CalendarDropdown(
    modifier: Modifier = Modifier,
    calendars: List<Calendar>,
    selected: Calendar?,
    onSelect: (Calendar) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldState by remember { mutableStateOf(selected?.description() ?: "") }

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = textFieldState,
            onValueChange = { },
            singleLine = true,
            label = { Text("Calendar") },
            textStyle = LocalTextStyle.current.copy(fontSize = TimeLoggerTheme.values.textFieldFontSize),
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. A read-only text field has
            // the anchor type `PrimaryNotEditable`.
            modifier = Modifier
                .menuAnchor(MenuAnchorType.Companion.PrimaryNotEditable)
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
                    Text(text = c.description(), fontSize = TimeLoggerTheme.values.textFieldFontSize)
                })
            }
        }
    }
}