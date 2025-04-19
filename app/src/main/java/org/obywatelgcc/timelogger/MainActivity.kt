package org.obywatelgcc.timelogger

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.obywatelgcc.timelogger.ui.compose.App
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callbackId = 42;
        checkPermission(
            callbackId, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR
        )
        enableEdgeToEdge()
        setContent {
            TimeLoggerTheme {

                Scaffold(
                    topBar = { TopAppBar(title = { Text(text = getString(R.string.app_name)) }) }) { innerPadding ->
                    App(innerPadding)
                }
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
