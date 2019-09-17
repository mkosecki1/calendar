package com.sharedcalendar.adapters

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharedcalendar.R
import com.sharedcalendar.database.CalendarDate
import com.sharedcalendar.utility.convertTimestamp
import com.sharedcalendar.utility.convertToday
import kotlinx.android.synthetic.main.event_details.view.*
import java.util.*

class RecyclerViewAdapter : RecyclerView.Adapter<ViewHolder>() {

    private var items: MutableList<CalendarDate> = mutableListOf()
    private lateinit var myContext: Context
    var selectedItem: ((CalendarDate) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val goldRow = layoutInflater.inflate(R.layout.event_details, viewGroup, false)
        myContext = viewGroup.context
        return ViewHolder(goldRow)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.run {
            event_details_data_displayed_id.text = items[position].date.toString()
            if (items[position].time.isNullOrEmpty()) {
                event_details_time_displayed_id.text = "cały"

            } else {
                event_details_time_displayed_id.text = items[position].time.toString()
            }
            event_details_event_displayed_id.text = items[position].event.toString()
            event_details_week_day_displayed_id.text =
                DateFormat.format("EEEE", items[position].date!!.convertTimestamp()).toString()
        }

        holder.itemView.event_details_delete_button_id.setOnClickListener {
            selectedItem?.invoke(items[position])
        }
    }

    fun updateItemList(list: List<CalendarDate>) {
        items.clear()
        items.addAll(list)
        items.sortWith(compareBy({ it.date }, { it.time }))
        notifyDataSetChanged()
    }
}

class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view)