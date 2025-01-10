package com.example.illscountdowns

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import android.content.Context
import android.app.DatePickerDialog
import android.util.Log
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var countdownAdapter: CountdownAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("CountdownPrefs", Context.MODE_PRIVATE)

        val eventNameEditText: EditText = findViewById(R.id.eventNameEditText)
        val setDateButton: Button = findViewById(R.id.setDateButton)

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.countdownRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        countdownAdapter = CountdownAdapter(loadCountdowns(sharedPreferences).toMutableList(), sharedPreferences) { position ->
            removeItem(position)
        }
        recyclerView.adapter = countdownAdapter

        // Show DatePickerDialog when the "Set Date" button is clicked
        setDateButton.setOnClickListener {
            val eventTitle = eventNameEditText.text.toString().trim()

            val countdownTitle = if (eventTitle.isNotEmpty()) eventTitle else "New Event"

            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val eventDate = "$year-${month + 1}-$dayOfMonth"  // month is 0-indexed

                    // Load the current list of countdowns
                    val countdownItems = loadCountdowns(sharedPreferences).toMutableList()

                    // Add the new countdown with the event title and date
                    countdownItems.add(CountdownItem(countdownTitle, eventDate))

                    // Save the updated list to SharedPreferences
                    saveCountdowns(sharedPreferences, countdownItems)

                    // Update the RecyclerView with the new countdown list
                    countdownAdapter.updateCountdowns(countdownItems)

                    // Clear the input field after saving
                    eventNameEditText.text.clear()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()

            // Get the OK and Cancel buttons from the DatePickerDialog
            val positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            val negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)

            // Set the text color for both buttons based on the system theme (using the selector)
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.dialog_button_text))
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.dialog_button_text))
        }
    }

    private fun loadCountdowns(sharedPreferences: SharedPreferences): List<CountdownItem> {
        val json = sharedPreferences.getString("countdowns", "[]")
        val countdownType = object : TypeToken<List<CountdownItem>>() {}.type
        return Gson().fromJson(json, countdownType) ?: emptyList()
    }

    private fun saveCountdowns(sharedPreferences: SharedPreferences, countdownItems: List<CountdownItem>) {
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(countdownItems)
        editor.putString("countdowns", json)
        editor.apply()
    }

    private fun removeItem(position: Int) {
        val countdownItems = loadCountdowns(sharedPreferences).toMutableList()
        countdownItems.removeAt(position) // Remove the item at the given position
        saveCountdowns(sharedPreferences, countdownItems)
        countdownAdapter.updateCountdowns(countdownItems)
    }
}

