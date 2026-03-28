package com.o7solutions.student_project_bakingo.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.student_project_bakingo.Fragments.Order
import com.o7solutions.student_project_bakingo.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.*

class OrdersAdapter(
    private val list: List<Order>,
    private val onComplete: (String) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = list[position]
        val context = holder.itemView.context

        holder.binding.apply {
            tvOrderName.text = order.itemName
            tvOrderPrice.text = "₹${order.itemPrice}"
            tvOrderWeight.text = "Weight: ${order.itemWeight}"
            tvCustomerPhone.text = "Call: ${order.customerPhone}"
            tvDeliveryAddress.text = "Ship to: ${order.deliveryAddress}"
            tvPaymentMethod.text = "Payment: ${order.paymentMethod} (${order.paymentStatus})"

            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            tvOrderDate.text = sdf.format(Date(order.timestamp))

            // Use paymentStatus as the driver for UI
            if (order.paymentStatus == "Completed") {
                btnComplete.visibility = View.GONE
                tvStatus.text = "Status: Completed"
                tvStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
            } else {
                btnComplete.visibility = View.VISIBLE
                tvStatus.text = "Status: Pending"
                tvStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
            }

            btnComplete.setOnClickListener {
                onComplete(order.orderId)
            }
        }
    }

    override fun getItemCount(): Int = list.size
}