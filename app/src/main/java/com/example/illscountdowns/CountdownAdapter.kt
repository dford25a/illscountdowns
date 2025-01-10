package com.example.illscountdowns

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

// Adapter class to handle multiple countdowns in a RecyclerView
class CountdownAdapter(
    private var countdownItems: MutableList<CountdownItem>,
    private val sharedPreferences: SharedPreferences,
    private val onDeleteItem: (Int) -> Unit // Add a callback to handle item deletion
) : RecyclerView.Adapter<CountdownAdapter.CountdownViewHolder>() {

    inner class CountdownViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.countdownTitleTextView)
        val countdownDateTextView: TextView = itemView.findViewById(R.id.countdownDateTextView)  // Reference the event date TextView
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton) // Reference the delete button

        init {
            // Set OnClickListener on the delete button
            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteItem(position)  // Call onDeleteItem function passed in the constructor
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountdownViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.countdown_item_layout, parent, false)
        return CountdownViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountdownViewHolder, position: Int) {
        val countdownItem = countdownItems[position]
        holder.titleTextView.text = countdownItem.title
        holder.countdownDateTextView.text = "Event Date: ${countdownItem.eventDate}"  // Show the event date

        // Set up the delete button
        holder.deleteButton.setOnClickListener {
            onDeleteItem(position) // Call the callback to delete the item
        }
    }

    override fun getItemCount(): Int = countdownItems.size

    // Update the countdown items
    fun updateCountdowns(newItems: MutableList<CountdownItem>) {
        countdownItems = newItems
        notifyDataSetChanged()
    }

    // Calculate days remaining for the event date
    private fun getDaysRemaining(eventDate: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val eventDateParsed = dateFormat.parse(eventDate)
        val today = Calendar.getInstance().time
        val diffInMillis = eventDateParsed.time - today.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}
