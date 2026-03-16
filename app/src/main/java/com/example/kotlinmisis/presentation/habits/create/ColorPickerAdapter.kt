package com.example.kotlinmisis.presentation.habits.create

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmisis.R

class ColorPickerAdapter(
    private val onColorSelected: (String) -> Unit
) : RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder>() {

    private var colors: List<String> = emptyList()
    private var selectedColor: String = ""

    fun submitData(colors: List<String>, selectedColor: String) {
        this.colors = colors
        this.selectedColor = selectedColor
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color_circle, parent, false)
        return ColorViewHolder(view, onColorSelected)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position], colors[position] == selectedColor)
    }

    override fun getItemCount(): Int = colors.size

    class ColorViewHolder(
        itemView: View,
        private val onColorSelected: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val circleView: View = itemView.findViewById(R.id.colorCircle)
        private val selectedRing: View = itemView.findViewById(R.id.selectedRing)

        fun bind(colorHex: String, isSelected: Boolean) {
            val drawable = circleView.background.mutate() as GradientDrawable
            drawable.setColor(Color.parseColor(colorHex))
            selectedRing.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            itemView.setOnClickListener { onColorSelected(colorHex) }
        }
    }
}
