package com.example.illscountdowns

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class CountdownWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Retrieve countdown items from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("CountdownPrefs", Context.MODE_PRIVATE)
        val countdownItems = loadCountdowns(sharedPreferences)

        Log.d("CountdownWidget", "Updating widget with ${countdownItems.size} countdowns.")

        // Iterate through all widgets
        for (appWidgetId in appWidgetIds) {
            // Prepare the views for the widget
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Check if there are any countdowns
            if (countdownItems.isNotEmpty()) {
                // Set the first countdown in the widget layout
                val countdownItem = countdownItems[0]
                val countdownText = getCountdownText(countdownItem.eventDate)

                views.setTextViewText(R.id.widgetCountdownText, countdownText)

                // Launch MainActivity when the widget is clicked
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE )
                views.setOnClickPendingIntent(R.id.setCountdownButton, pendingIntent)
            } else {
                views.setTextViewText(R.id.widgetCountdownText, "No Countdown Set")
            }

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    // Load countdowns from SharedPreferences
    private fun loadCountdowns(sharedPreferences: SharedPreferences): List<CountdownItem> {
        val json = sharedPreferences.getString("countdowns", "[]")
        val type = object : TypeToken<List<CountdownItem>>() {}.type
        return Gson().fromJson(json, type)
    }

    // Calculate the remaining days for the event date
    private fun getCountdownText(eventDate: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val eventDateParsed = dateFormat.parse(eventDate)
        val today = Calendar.getInstance().time
        val diffInMillis = eventDateParsed.time - today.time
        val daysRemaining = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        return "Days Remaining: $daysRemaining"
    }

    // Force an initial update when the widget is placed on the home screen
    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, CountdownWidget::class.java))

        if (widgetIds.isNotEmpty()) {
            onUpdate(context, appWidgetManager, widgetIds)
        }
    }
}
