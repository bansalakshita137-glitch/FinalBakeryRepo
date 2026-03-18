package com.o7solutions.student_project_bakingo.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.o7solutions.student_project_bakingo.Product
import com.o7solutions.student_project_bakingo.R

class ProductAdapter(
    private val productList: List<Product>,

    private val onItemClick: (Product) -> Unit ,  // 🔹 Click listener
) : RecyclerView.Adapter<ProductAdapter.ProductVH>() {

    inner class ProductVH(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.imgProduct)
        val tvName: TextView = view.findViewById(R.id.tvProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvProductPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductVH(view)
    }

    override fun onBindViewHolder(holder: ProductVH, position: Int) {
        val product = productList[position]

        holder.tvName.text = product.name
        holder.tvPrice.text = "₹${product.price}"

//        holder.itemView.setOnClickListener {
//            itemOnClick.onClick(product)
//        }

        if (!product.images.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(product.images!![0])
                .placeholder(R.drawable.ic_logo)
                .into(holder.imgProduct)
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_logo)
        }

        // 🔹 Handle item click
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size

    interface OnItemClickListener {
        fun onClick(product: Product)
    }
}