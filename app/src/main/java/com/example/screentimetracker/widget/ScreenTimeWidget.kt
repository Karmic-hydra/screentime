package com.example.screentimetracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.app.usage.UsageStatsManager
import android.view.View
import android.widget.RemoteViews
import com.example.screentimetracker.MainActivity
import com.example.screentimetracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*

class ScreenTimeWidget : AppWidgetProvider() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.screen_time_widget)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetContent, pendingIntent)

        // Hide all containers initially
        views.setViewVisibility(R.id.app1Container, View.GONE)
        views.setViewVisibility(R.id.app2Container, View.GONE)
        views.setViewVisibility(R.id.app3Container, View.GONE)

        coroutineScope.launch {
            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val calendar = Calendar.getInstance()
                val endTime = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val startTime = calendar.timeInMillis

                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )

                val topApps = usageStats
                    .filter { it.totalTimeInForeground > 0 }
                    .sortedByDescending { it.totalTimeInForeground }
                    .take(3)

                topApps.forEachIndexed { index, stats ->
                    try {
                        val appInfo = context.packageManager.getApplicationInfo(stats.packageName, 0)
                        val appName = appInfo.loadLabel(context.packageManager).toString()
                        val usageTime = formatUsageTime(stats.totalTimeInForeground)

                        views.setTextViewText(
                            getAppNameViewId(index),
                            appName
                        )
                        views.setTextViewText(
                            getAppTimeViewId(index),
                            usageTime
                        )
                        views.setViewVisibility(getAppContainerViewId(index), View.VISIBLE)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatUsageTime(timeInMillis: Long): String {
        val hours = timeInMillis / (1000 * 60 * 60)
        val minutes = (timeInMillis % (1000 * 60 * 60)) / (1000 * 60)
        return when {
            hours > 0 -> "$hours hours"
            else -> "$minutes minutes"
        }
    }

    private fun getAppContainerViewId(index: Int): Int {
        return when (index) {
            0 -> R.id.app1Container
            1 -> R.id.app2Container
            else -> R.id.app3Container
        }
    }

    private fun getAppNameViewId(index: Int): Int {
        return when (index) {
            0 -> R.id.app1Name
            1 -> R.id.app2Name
            else -> R.id.app3Name
        }
    }

    private fun getAppTimeViewId(index: Int): Int {
        return when (index) {
            0 -> R.id.app1Time
            1 -> R.id.app2Time
            else -> R.id.app3Time
        }
    }
} 