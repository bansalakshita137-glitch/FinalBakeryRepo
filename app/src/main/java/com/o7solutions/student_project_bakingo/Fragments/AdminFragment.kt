package com.o7solutions.student_project_bakingo.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.o7solutions.student_project_bakingo.Adapters.AdminProductAdapter
import com.o7solutions.student_project_bakingo.Product
import com.o7solutions.student_project_bakingo.ProductWrapper
import com.o7solutions.student_project_bakingo.R
import com.o7solutions.student_project_bakingo.databinding.FragmentAdminBinding

class AdminFragment : Fragment() {

    private lateinit var binding: FragmentAdminBinding
    private lateinit var adapter: AdminProductAdapter
    private var allProducts = mutableListOf<ProductWrapper>()
    private val database = FirebaseDatabase.getInstance().getReference("Products")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        fetchProducts()
        setupSearch()
    }

    private fun setupUI() {
        binding.rvAdminProducts.layoutManager = LinearLayoutManager(requireContext())
        adapter = AdminProductAdapter(allProducts) { key -> deleteProduct(key) }
        binding.rvAdminProducts.adapter = adapter

        binding.btnAddCategory.setOnClickListener {
            findNavController().navigate(R.id.addCategoryFragment)
        }
        binding.btnAddProduct.setOnClickListener {
            findNavController().navigate(R.id.addProductFragment)
        }
    }

    private fun setupSearch() {
        binding.etSearchProduct.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase().trim()
                val filtered = allProducts.filter {
                    it.data.name?.lowercase()?.contains(query) == true
                }
                adapter.updateList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchProducts() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allProducts.clear()
                for (data in snapshot.children) {
                    val product = data.getValue(Product::class.java)
                    data.key?.let { key ->
                        product?.let { allProducts.add(ProductWrapper(key, it)) }
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteProduct(key: String) {
        database.child(key).removeValue().addOnSuccessListener {
            Toast.makeText(requireContext(), "Product Removed", Toast.LENGTH_SHORT).show()
        }
    }
}