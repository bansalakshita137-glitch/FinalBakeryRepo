package com.o7solutions.student_project_bakingo.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.o7solutions.student_project_bakingo.Adapters.OrdersAdapter
import com.o7solutions.student_project_bakingo.OrderData
import com.o7solutions.student_project_bakingo.databinding.FragmentOrdersBinding

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseRef: DatabaseReference
    private val ordersList = mutableListOf<OrderData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        databaseRef = FirebaseDatabase.getInstance().getReference("Orders").child(userId)

        setupRecyclerView()
        fetchOrders()
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        // 🔹 These two lines ensure the newest item stays at the top
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true

        binding.rvOrders.layoutManager = layoutManager
    }

    private fun fetchOrders() {
        // Query by timestamp to keep them sorted
        databaseRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return

                ordersList.clear()
                for (data in snapshot.children) {
                    val order = data.getValue(OrderData::class.java)
                    if (order != null) {
                        // We set the orderId from the key so we can update it later
                        ordersList.add(order.copy(orderId = data.key))
                    }
                }

                binding.rvOrders.adapter = OrdersAdapter(ordersList) { orderId ->
                    updateOrderStatus(orderId)
                }

                binding.tvNoOrders.visibility = if (ordersList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateOrderStatus(orderId: String) {
        databaseRef.child(orderId).child("status").setValue("Completed")
            .addOnSuccessListener {
                if (isAdded) Toast.makeText(requireContext(), "Order marked as Completed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}