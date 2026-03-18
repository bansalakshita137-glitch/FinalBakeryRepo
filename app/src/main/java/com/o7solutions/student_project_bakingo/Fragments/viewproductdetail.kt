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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [viewproductdetail.newInstance] factory method to
 * create an instance of this fragment.
 */
class viewproductdetail : Fragment() {

    private lateinit var binding: FragmentViewproductdetailBinding
    private var selectedWeight: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewproductdetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        binding.toolbar.setNavigationOnClickListener{
            findNavController().popBackStack()
        }
        val name = arguments?.getString("name")
        val price = arguments?.getString("price")
        val description = arguments?.getString("description")
        val images = arguments?.getStringArrayList("images")

        binding.tvName.text = name
        binding.tvPrice.text = "₹$price"
        binding.tvDescription.text = description

        Glide.with(requireContext())
            .load(images?.getOrNull(0))
            .into(binding.imgProduct)

        // 🔹 Setup Weight Recycler
        val weights = listOf("0.5 Kg", "1 Kg", "2 Kg")

        binding.weightRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.weightRecycler.adapter = WeightAdapter(weights) {
            selectedWeight = it
        }

        // 🔹 Add to Cart
        binding.btnAddToCart.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser

            // 1. Check if user is logged in
            if (currentUser == null) {
                Toast.makeText(requireContext(), "Please login to add items to cart", Toast.LENGTH_SHORT).show()
                // Optional: findNavController().navigate(R.id.loginFragment)
                return@setOnClickListener
            }

            val userId = currentUser.uid // This is now guaranteed not to be null
            val message = binding.etMessage.text.toString()

            if (selectedWeight.isEmpty()) {
                Toast.makeText(requireContext(), "Select weight", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cartItem = CartData(
                name = name,
                price = price,
                description = description,
                categoryId = arguments?.getString("categoryId"),
                images = images,
                weight = selectedWeight,
                message = message
            )

            // 2. Use the verified userId
            FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child(userId)
                .push()
                .setValue(cartItem)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Added to cart", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}