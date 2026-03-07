package com.o7solutions.student_project_bakingo.Adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.student_project_bakingo.CarouselItem
import com.o7solutions.student_project_bakingo.R

class CarouselAdapter(private val items: List<CarouselItem>) :
    RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.carouselImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carousel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageResource(items[position].imageResId)
    }

    override fun getItemCount(): Int = items.size
}