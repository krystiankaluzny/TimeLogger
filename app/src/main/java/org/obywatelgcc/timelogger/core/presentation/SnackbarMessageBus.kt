package org.obywatelgcc.timelogger.core.presentation

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class SnackbarMessageBus {

    private val _effectsChannel = Channel<SnackbarMessage>()
    val effectsFlow = _effectsChannel.receiveAsFlow()

    suspend fun send(message: SnackbarMessage) {
        _effectsChannel.send(message)
    }
}

data class SnackbarMessage(
    val message: String,
    val actionLabel: String?,
    val duration: SnackbarDuration
)