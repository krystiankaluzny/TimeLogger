package org.obywatelgcc.timelogger

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.provider.CalendarContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.obywatelgcc.timelogger.ui.compose.App
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import org.obywatelgcc.timelogger.viewmodel.TimeEntryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TimeEntryViewModel by viewModel()

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
                App(viewModel)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.calendarEntryToSave.collect { calendarEntry ->

                    val intent = Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(
                            CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                            calendarEntry.startMillis()
                        )
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendarEntry.endMillis())
                        .putExtra(
                            CalendarContract.Events.EVENT_TIMEZONE,
                            calendarEntry.zoneIdName()
                        )
                        .putExtra(CalendarContract.Events.TITLE, calendarEntry.title)
                        .putExtra(CalendarContract.Events.DESCRIPTION, "")

                    startActivity(intent)
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
