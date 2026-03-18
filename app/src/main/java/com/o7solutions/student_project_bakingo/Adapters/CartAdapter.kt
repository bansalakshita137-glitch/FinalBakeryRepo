package com.o7solutions.student_project_bakingo.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.o7solutions.student_project_bakingo.CartData
import com.o7solutions.student_project_bakingo.CartItemWrapper
import com.o7solutions.student_project_bakingo.R

class CartAdapter(
    private val list: List<CartItemWrapper>,
    private val onDeleteClick: (String) -> Unit,
    private val onOrderClick: (CartData, String) -> Unit // Added this
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvCartName)
        val price: TextView = view.findViewById(R.id.tvCartPrice)
        val weight: TextView = view.findViewById(R.id.tvCartWeight)
        val image: ImageView = view.findViewById(R.id.ivCartProduct)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnOrderNow: Button = view.findViewById(R.id.btnOrderNow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val itemWrapper = list[position]
        val item = itemWrapper.data

        holder.name.text = item.name
        holder.price.text = "₹${item.price}"
        holder.weight.text = "Weight: ${item.weight}"

        Glide.with(holder.itemView.context)
            .load(item.images?.getOrNull(0))
            .into(holder.image)

        // Delete Click
        holder.btnDelete.setOnClickListener {
            onDeleteClick(itemWrapper.key)
        }

        // Individual Order Click
        holder.btnOrderNow.setOnClickListener {
            onOrderClick(item, itemWrapper.key)
        }
    }

    override fun getItemCount() = list.size
}