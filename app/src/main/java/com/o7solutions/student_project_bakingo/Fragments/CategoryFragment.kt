package com.o7solutions.student_project_bakingo.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.o7solutions.student_project_bakingo.Adapters.CategoryAdapter
import com.o7solutions.student_project_bakingo.Category
import com.o7solutions.student_project_bakingo.R
import com.o7solutions.student_project_bakingo.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private val categoryList = ArrayList<Category>()

    private val categoryRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Categories")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(categoryList) { category ->

            if (category.categoryId.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Invalid category", Toast.LENGTH_SHORT).show()
                return@CategoryAdapter
            }

            // 🔹 Pass categoryId to ProductsFragment
            val bundle = Bundle().apply {
                putString("categoryId", category.categoryId)
            }

            findNavController().navigate(
                R.id.productsFragment,
                bundle
            )
        }

        binding.categoryRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun fetchCategories() {
        categoryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()

                for (data in snapshot.children) {
                    val category = data.getValue(Category::class.java)
                    category?.let { categoryList.add(it) }
                }

                categoryAdapter.notifyDataSetChanged()

                if (categoryList.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No categories found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    error.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}