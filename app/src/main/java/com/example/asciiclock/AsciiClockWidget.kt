package com.example.asciiclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.widget.RemoteViews
import java.util.Date

class AsciiClockWidget : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateAllWidgets(context)
        scheduleNextTick(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelTick(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleNextTick(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                updateAllWidgets(context)
                scheduleNextTick(context)
            }
        }
    }

    companion object {
        const val ACTION_TICK = "com.example.asciiclock.ACTION_TICK"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, AsciiClockWidget::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return
            for (id in ids) {
                updateSingleWidget(context, manager, id)
            }
        }

        private fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val now = Date()
            val views = RemoteViews(context.packageName, R.layout.widget_ascii_clock)
            views.setTextViewText(R.id.ascii_time, AsciiRenderer.renderTime(context, now = now))
            views.setTextViewText(R.id.ascii_date, AsciiRenderer.renderDate(now = now))

            val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val alarmPendingIntent = PendingIntent.getActivity(
                context,
                1,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.ascii_time, alarmPendingIntent)

            val settingsIntent = Intent(context, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val settingsPendingIntent = PendingIntent.getActivity(
                context,
                2,
                settingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.ascii_date, settingsPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun scheduleNextTick(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, AsciiClockWidget::class.java))
            if (ids.isEmpty()) return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = getTickPendingIntent(context)
            val now = System.currentTimeMillis()
            val nextMinute = ((now / 60_000L) + 1L) * 60_000L

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC, nextMinute, pendingIntent)
                    } else {
                        alarmManager.setWindow(AlarmManager.RTC, nextMinute, 10_000L, pendingIntent)
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC, nextMinute, pendingIntent)
                }
            } catch (_: SecurityException) {
                alarmManager.set(AlarmManager.RTC, nextMinute, pendingIntent)
            }
        }

        fun cancelTick(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(getTickPendingIntent(context))
        }

        private fun getTickPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, AsciiClockWidget::class.java).apply {
                action = ACTION_TICK
            }
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
