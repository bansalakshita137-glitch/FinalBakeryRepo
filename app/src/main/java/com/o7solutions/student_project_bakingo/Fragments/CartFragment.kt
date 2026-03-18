package com.o7solutions.student_project_bakingo.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        // Reference to this specific user's cart
        databaseRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId)

        setupRecyclerView()
        fetchCartData()
    }

    private fun setupRecyclerView() {
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun fetchCartData() {
        // Show a progress bar if you have one in your XML
        // binding.progressBar.visibility = View.VISIBLE

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartList.clear()
                var totalAmount = 0

                for (data in snapshot.children) {
                    val cartData = data.getValue(CartData::class.java)
                    val key = data.key

                    if (cartData != null && key != null) {
                        cartList.add(CartItemWrapper(key, cartData))

                        // Calculate total (Assuming price is a String like "500")
                        val price = cartData.price?.toIntOrNull() ?: 0
                        totalAmount += price
                    }
                }

                // Update Adapter
                val adapter = CartAdapter(cartList,
                    onDeleteClick = { key -> deleteItem(key) },
                    onOrderClick = { item, key -> placeOrder(item, key) }
                )
                binding.rvCart.adapter = adapter



                // Show/Hide Empty Cart Message
                if (cartList.isEmpty()) {
                    binding.tvEmptyCart.visibility = View.VISIBLE
                    binding.rvCart.visibility = View.GONE
                } else {
                    binding.tvEmptyCart.visibility = View.GONE
                    binding.rvCart.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteItem(key: String) {
        databaseRef.child(key).removeValue().addOnSuccessListener {
            Toast.makeText(requireContext(), "Item removed from cart", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show()
        }
    }

    private fun placeOrder(item: CartData, key: String) {
        val businessPhoneNumber = "+917658848135" // 🔹 Change this to your actual number

        // 1. Format the Message for WhatsApp/SMS
        val orderMessage = """
        *New Cake Order!* 🎂
        --------------------------
        *Product:* ${item.name}
        *Price:* ₹${item.price}
        *Weight:* ${item.weight}
        *Message on Cake:* ${item.message ?: "None"}
        --------------------------
        Customer UID: ${auth.currentUser?.uid}
    """.trimIndent()

        try {
            // 2. Open WhatsApp with pre-filled text
            val uri = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$businessPhoneNumber&text=${android.net.Uri.encode(orderMessage)}")
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
            startActivity(intent)

            // 3. Move the data to the "Orders" node and REMOVE from "Cart"

        } catch (e: Exception) {
            // Fallback: If WhatsApp isn't installed, try SMS
            val smsIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO)
            smsIntent.data = android.net.Uri.parse("smsto:$businessPhoneNumber")
            smsIntent.putExtra("sms_body", orderMessage)
            startActivity(smsIntent)

            // Still remove from cart even if using SMS
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}