package org.obywatelgcc.timelogger.timer.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoadingScreen(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier.Companion
            .padding(innerPadding)
            .fillMaxSize(),
        contentAlignment = Alignment.Companion.Center
    ) {
        CircularProgressIndicator()
    }
}