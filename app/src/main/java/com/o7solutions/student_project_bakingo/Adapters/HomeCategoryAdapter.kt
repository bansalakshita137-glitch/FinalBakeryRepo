package com.o7solutions.student_project_bakingo.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.o7solutions.student_project_bakingo.Category
import com.o7solutions.student_project_bakingo.R

class HomeCategoryAdapter(private val list: List<Category>,
                          private val onItemClick: (Category) -> Unit
) :
    RecyclerView.Adapter<HomeCategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ShapeableImageView = view.findViewById(R.id.ivCategory)
        val name: TextView = view.findViewById(R.id.tvCategoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_2, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = item.name
        Glide.with(holder.itemView.context)
            .load(item.categoryUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.img)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = list.size
}