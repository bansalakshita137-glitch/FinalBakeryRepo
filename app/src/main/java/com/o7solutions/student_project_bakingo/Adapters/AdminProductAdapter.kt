package com.o7solutions.student_project_bakingo.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.o7solutions.student_project_bakingo.ProductWrapper
import com.o7solutions.student_project_bakingo.R

class AdminProductAdapter(
    private var list: List<ProductWrapper>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.ivAdminProduct)
        val name = view.findViewById<TextView>(R.id.tvAdminProductName)
        val price = view.findViewById<TextView>(R.id.tvAdminProductPrice)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDeleteProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = item.data.name
        holder.price.text = "₹${item.data.price}"

        Glide.with(holder.itemView.context)
            .load(item.data.images?.getOrNull(0))
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.img)

        holder.btnDelete.setOnClickListener { onDeleteClick(item.key) }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<ProductWrapper>) {
        list = newList
        notifyDataSetChanged()
    }
}