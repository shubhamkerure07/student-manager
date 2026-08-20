package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.StudentDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules all 15-minute advance class reminders when device reboots or app is updated.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d("BootReceiver", "Re-scheduling class reminders after device boot / package replace")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = StudentDatabase.getDatabase(context)
                    val timetableList = db.timetableDao().getAllTimetableItemsList()
                    ClassNotificationScheduler.scheduleAll(context, timetableList)
                    Log.d("BootReceiver", "Successfully scheduled ${timetableList.size} class reminders on boot")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling class reminders on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
