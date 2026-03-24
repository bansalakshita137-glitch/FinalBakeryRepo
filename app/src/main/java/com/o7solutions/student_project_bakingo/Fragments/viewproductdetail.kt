package com.o7solutions.student_project_bakingo.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.o7solutions.student_project_bakingo.Adapters.WeightAdapter
import com.o7solutions.student_project_bakingo.CartData
import com.o7solutions.student_project_bakingo.R
import com.o7solutions.student_project_bakingo.databinding.FragmentViewproductdetailBinding

class viewproductdetail : Fragment() {

    private lateinit var binding: FragmentViewproductdetailBinding
    private var selectedWeight: String = ""
    private var basePrice: Double = 0.0
    private var currentCalculatedPrice: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewproductdetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // 1. Get arguments
        val name = arguments?.getString("name")
        val priceString = arguments?.getString("price") ?: "0"
        val description = arguments?.getString("description")
        val images = arguments?.getStringArrayList("images")
        val categoryId = arguments?.getString("categoryId")

        // 2. Initialize Prices
        basePrice = priceString.toDoubleOrNull() ?: 0.0
        currentCalculatedPrice = basePrice // Default to base price

        // 3. Set initial UI
        binding.tvName.text = name
        binding.tvPrice.text = "₹$basePrice"
        binding.tvDescription.text = description

        Glide.with(requireContext())
            .load(images?.getOrNull(0))
            .into(binding.imgProduct)

        // 4. Setup Weight Recycler
        val weights = listOf("0.5 Kg", "1 Kg", "2 Kg")

        binding.weightRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.weightRecycler.adapter = WeightAdapter(weights) { weight ->
            selectedWeight = weight
            updatePriceUI(weight)
        }

        // 5. Add to Cart Logic
        binding.btnAddToCart.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {
                Toast.makeText(requireContext(), "Please login to add items to cart", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedWeight.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a weight first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = currentUser.uid
            val message = binding.etMessage.text.toString()

            // Use the calculated price for the cart
            val cartItem = CartData(
                name = name,
                price = currentCalculatedPrice.toString(),
                description = description,
                categoryId = categoryId,
                images = images,
                weight = selectedWeight,
                message = message
            )

            FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child(userId)
                .push()
                .setValue(cartItem)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Added to cart successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Updates the price based on weight logic.
     * You can adjust the multipliers (1.8, 3.5) based on your pricing strategy.
     */
    private fun updatePriceUI(weight: String) {
        currentCalculatedPrice = when (weight) {
            "0.5 Kg" -> basePrice
            "1 Kg" -> basePrice * 1.8
            "2 Kg" -> basePrice * 3.4
            else -> basePrice
        }

        // Update the TextView (formatting to remove unnecessary decimals)
        binding.tvPrice.text = "₹${String.format("%.0f", currentCalculatedPrice)}"
    }
}