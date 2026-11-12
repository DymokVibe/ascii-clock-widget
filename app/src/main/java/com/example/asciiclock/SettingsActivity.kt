package com.example.asciiclock

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView

class SettingsActivity : Activity() {

    private lateinit var previewTime: TextView
    private lateinit var previewDate: TextView
    private lateinit var syncStatusText: TextView
    private lateinit var exactAlarmBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        previewTime = findViewById(R.id.preview_time)
        previewDate = findViewById(R.id.preview_date)
        syncStatusText = findViewById(R.id.sync_status_text)
        exactAlarmBtn = findViewById(R.id.btn_exact_alarm)

        findViewById<Button>(R.id.btn_style_shaded).setOnClickListener {
            AsciiRenderer.setSavedStyle(this, AsciiRenderer.STYLE_SHADED)
            refreshUi()
            AsciiClockWidget.updateAllWidgets(this)
        }

        findViewById<Button>(R.id.btn_style_minimal).setOnClickListener {
            AsciiRenderer.setSavedStyle(this, AsciiRenderer.STYLE_MINIMAL)
            refreshUi()
            AsciiClockWidget.updateAllWidgets(this)
        }

        exactAlarmBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
        AsciiClockWidget.updateAllWidgets(this)
        AsciiClockWidget.scheduleNextTick(this)
    }

    private fun refreshUi() {
        previewTime.text = AsciiRenderer.renderTime(this)
        previewDate.text = AsciiRenderer.renderDate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                syncStatusText.text = "[OK] Точная поминутная синхронизация активна"
                exactAlarmBtn.visibility = View.GONE
            } else {
                syncStatusText.text = "[!] Разреши «Будильники и напоминания», чтобы OxygenOS не задерживала смену минут"
                exactAlarmBtn.visibility = View.VISIBLE
            }
        } else {
            syncStatusText.text = "[OK] Поминутная синхронизация активна"
            exactAlarmBtn.visibility = View.GONE
        }
    }
}
