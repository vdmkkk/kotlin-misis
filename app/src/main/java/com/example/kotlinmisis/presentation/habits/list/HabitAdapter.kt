package com.example.kotlinmisis.presentation.habits.list

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmisis.R
import com.google.android.material.button.MaterialButton

class HabitAdapter(
    private val onHabitClicked: (String) -> Unit,
    private val onHabitActionClicked: (String) -> Unit
) : ListAdapter<HabitListItemUiModel, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view, onHabitClicked, onHabitActionClicked)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HabitViewHolder(
        itemView: View,
        private val onHabitClicked: (String) -> Unit,
        private val onHabitActionClicked: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val colorIndicator: View = itemView.findViewById(R.id.colorIndicator)
        private val titleText: TextView = itemView.findViewById(R.id.habitTitleText)
        private val descriptionText: TextView = itemView.findViewById(R.id.habitDescriptionText)
        private val frequencyText: TextView = itemView.findViewById(R.id.habitFrequencyText)
        private val statusText: TextView = itemView.findViewById(R.id.habitStatusText)
        private val streakText: TextView = itemView.findViewById(R.id.habitStreakText)
        private val actionButton: MaterialButton = itemView.findViewById(R.id.completeHabitButton)

        fun bind(item: HabitListItemUiModel) {
            colorIndicator.setBackgroundColor(Color.parseColor(item.colorHex))
            titleText.text = item.title
            descriptionText.text = item.description
            descriptionText.visibility = if (item.description.isBlank()) View.GONE else View.VISIBLE
            frequencyText.text = item.frequencyLabel
            statusText.text = item.statusLabel
            streakText.text = item.streakLabel
            actionButton.text = item.actionLabel
            actionButton.setOnClickListener { onHabitActionClicked(item.id) }
            itemView.setOnClickListener { onHabitClicked(item.id) }
        }
    }

    private class HabitDiffCallback : DiffUtil.ItemCallback<HabitListItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: HabitListItemUiModel,
            newItem: HabitListItemUiModel
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: HabitListItemUiModel,
            newItem: HabitListItemUiModel
        ): Boolean = oldItem == newItem
    }
}
