package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.ClassReminderReceiver
import com.example.ui.StudentHubApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.StudentHubViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: StudentHubViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    handleIntent(intent)
    setContent {
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      MyApplicationTheme(themeMode = uiState.themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
          StudentHubApp(viewModel = viewModel)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    if (intent != null && intent.getStringExtra(ClassReminderReceiver.EXTRA_NAVIGATE_TAB) == "TIMETABLE") {
      viewModel.selectTab(AppTab.TIMETABLE)
    }
  }
}


