package com.o7solutions.student_project_bakingo.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.o7solutions.student_project_bakingo.Adapters.CarouselAdapter
import com.o7solutions.student_project_bakingo.Adapters.HomeCategoryAdapter
import com.o7solutions.student_project_bakingo.CarouselItem
import com.o7solutions.student_project_bakingo.Category
import com.o7solutions.student_project_bakingo.R
import com.o7solutions.student_project_bakingo.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var currentPosition = 0

    private val imageList = listOf(
        CarouselItem(R.drawable.cake1),
        CarouselItem(R.drawable.cake2),
        CarouselItem(R.drawable.cake3),
        CarouselItem(R.drawable.cake4),
        CarouselItem(R.drawable.cake5)
    )

    // 1. Fixed Runnable with Null Safety
    private val scrollRunnable = object : Runnable {
        override fun run() {
            // Check if binding still exists before scrolling
            _binding?.let {
                if (imageList.isNotEmpty()) {
                    currentPosition = (currentPosition + 1) % imageList.size
                    it.carouselRecyclerView.smoothScrollToPosition(currentPosition)
                    handler.postDelayed(this, 3000)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Carousel
        binding.carouselRecyclerView.layoutManager = CarouselLayoutManager()
        binding.carouselRecyclerView.adapter = CarouselAdapter(imageList)

        // Setup Categories
        val layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)

        binding.rvCategories.layoutManager = layoutManager

        fetchCategories()

        // Start Auto Scroll
        handler.postDelayed(scrollRunnable, 3000)
    }

    private fun fetchCategories() {
        FirebaseDatabase.getInstance().getReference("Categories")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    // 2. CRITICAL FIX: Check if binding is null before updating UI
                    if (_binding == null || !isAdded) return

                    val list = mutableListOf<Category>()
                    for (data in snapshot.children) {
                        val category = data.getValue(Category::class.java)
                        category?.let { list.add(it) }
                    }

                    binding.rvCategories.adapter = HomeCategoryAdapter(list) { category ->

                        // Safety check inside click listener
                        if (!isAdded) return@HomeCategoryAdapter

                        if (category.categoryId.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), "Invalid category", Toast.LENGTH_SHORT).show()
                            return@HomeCategoryAdapter
                        }

                        val bundle = Bundle().apply {
                            putString("categoryId", category.categoryId)
                        }

                        findNavController().navigate(
                            R.id.productsFragment,
                            bundle
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    // 3. Stop the timer immediately when the view is destroyed
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(scrollRunnable)
    }

    override fun onResume() {
        super.onResume()
        // Restart scrolling when user comes back to Home
        handler.postDelayed(scrollRunnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(scrollRunnable)
        _binding = null
    }
}