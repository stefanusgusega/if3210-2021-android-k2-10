package com.example.fitnessapp.ui.scheduler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R

class ScheduleListAdapter: ListAdapter<Schedule, ScheduleListAdapter.ScheduleViewHolder>(ScheduleComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        return ScheduleViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class ScheduleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val scheduleCardView: CardView = itemView.findViewById(R.id.schedule_card_view)

        fun bind(schedule: Schedule) {
            scheduleCardView.findViewById<TextView>(R.id.fitness_type).text = schedule.type
            scheduleCardView.findViewById<TextView>(R.id.days).text = schedule.day
            scheduleCardView.findViewById<TextView>(R.id.start_time).text = schedule.startTime
            scheduleCardView.findViewById<TextView>(R.id.end_time).text = schedule.endTime
        }

        companion object {
            fun create(parent: ViewGroup): ScheduleViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_scheduler, parent, false)

                return ScheduleViewHolder(view)
            }
        }
    }

    class ScheduleComparator: DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem.id == newItem.id
        }
    }
}