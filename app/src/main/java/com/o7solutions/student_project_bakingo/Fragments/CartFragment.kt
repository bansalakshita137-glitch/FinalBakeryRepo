package com.o7solutions.student_project_bakingo.Fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.o7solutions.student_project_bakingo.Adapters.CartAdapter
import com.o7solutions.student_project_bakingo.CartData
import com.o7solutions.student_project_bakingo.CartItemWrapper
import com.o7solutions.student_project_bakingo.databinding.FragmentCartBinding

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val cartList = mutableListOf<CartItemWrapper>()
    private val SMS_PERMISSION_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: ""

        databaseRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId)

        setupRecyclerView()
        fetchCartData()
    }

    private fun setupRecyclerView() {
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun fetchCartData() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null || !isAdded) return
                cartList.clear()
                for (data in snapshot.children) {
                    val cartData = data.getValue(CartData::class.java)
                    val key = data.key
                    if (cartData != null && key != null) {
                        cartList.add(CartItemWrapper(key, cartData))
                    }
                }

                binding.rvCart.adapter = CartAdapter(cartList,
                    onDeleteClick = { key -> deleteItem(key) },
                    onOrderClick = { item, key -> checkPermissionAndOrder(item, key) }
                )

                binding.tvEmptyCart.visibility = if (cartList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkPermissionAndOrder(item: CartData, key: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
            == PackageManager.PERMISSION_GRANTED) {
            placeOrderAndSendSMS(item, key)
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
        }
    }

    private fun placeOrderAndSendSMS(item: CartData, cartKey: String) {
        val userId = auth.currentUser?.uid ?: return
        val orderRef = FirebaseDatabase.getInstance().getReference("Orders").child(userId)
        val orderId = orderRef.push().key ?: ""

        // 1. Add to Realtime Database (Orders Node)
        orderRef.child(orderId).setValue(item).addOnSuccessListener {

            // 2. Send SMS Directly
            sendDirectSMS(item)

            // 3. Remove from Cart
            databaseRef.child(cartKey).removeValue()

            if (isAdded) Toast.makeText(requireContext(), "Order Placed Successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            if (isAdded) Toast.makeText(requireContext(), "Order Failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendDirectSMS(item: CartData) {
        val phoneNumber = "7658848135"
        val message = "New Order: ${item.name}, Weight: ${item.weight}, Price: ₹${item.price}. User: ${auth.currentUser?.uid}"

        try {
            val smsManager: SmsManager = requireContext().getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            if (isAdded) Toast.makeText(requireContext(), "SMS Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteItem(key: String) {
        databaseRef.child(key).removeValue()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}