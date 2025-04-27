package org.obywatelgcc.timelogger

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.obywatelgcc.timelogger.timer.presentation.TimerViewModel
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TimerViewModel by viewModel()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callbackId = 42;
        checkPermission(
            callbackId, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR
        )

        viewModel.setAppName(getString(R.string.app_name))

        enableEdgeToEdge()
        setContent {
            TimeLoggerTheme {
                MainScreen(viewModel)
            }
        }
    }

    private fun checkPermission(callbackId: Int, vararg permissionsId: String) {
        var permissions = true
        for (p in permissionsId) {
            permissions =
                permissions && ContextCompat.checkSelfPermission(this, p) == PERMISSION_GRANTED
        }

        if (!permissions) ActivityCompat.requestPermissions(this, permissionsId, callbackId)
    }
}
