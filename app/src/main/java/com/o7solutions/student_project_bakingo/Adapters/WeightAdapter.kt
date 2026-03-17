package com.o7solutions.student_project_bakingo.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.student_project_bakingo.R

class WeightAdapter(
    private val weights: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<WeightAdapter.WeightVH>() {

    private var selectedPosition = -1

    inner class WeightVH(val view: View) : RecyclerView.ViewHolder(view) {
        val tvWeight: TextView = view.findViewById(R.id.tvWeight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weight, parent, false)
        return WeightVH(view)
    }

    override fun onBindViewHolder(holder: WeightVH, @SuppressLint("RecyclerView") position: Int) {
        val weight = weights[position]
        holder.tvWeight.text = weight

        // 🔹 Selection UI
        if (position == selectedPosition) {
            holder.tvWeight.setBackgroundColor(
                holder.itemView.context.getColor(R.color.purple_200) // selected color
            )
            holder.tvWeight.setTextColor(
                holder.itemView.context.getColor(android.R.color.white)
            )
        } else {
            holder.tvWeight.setBackgroundColor(
                holder.itemView.context.getColor(android.R.color.transparent)
            )
            holder.tvWeight.setTextColor(
                holder.itemView.context.getColor(android.R.color.black)
            )
        }

        // 🔹 Click handling
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            onClick(weight)
        }
    }

    override fun getItemCount() = weights.size
}