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
        CarouselItem(R.drawable.cake1), // Note: Verify if these are drawable IDs or Image IDs
        CarouselItem(R.drawable.cake2),
        CarouselItem(R.drawable.cake3),
        CarouselItem(R.drawable.cake4),
        CarouselItem(R.drawable.cake5)
    )

    // Define the auto-scroll task
    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (imageList.isNotEmpty()) {
                currentPosition = (currentPosition + 1) % imageList.size
                binding.carouselRecyclerView.smoothScrollToPosition(currentPosition)
                handler.postDelayed(this, 3000) // 3 seconds interval
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
                    val list = mutableListOf<Category>()
                    for (data in snapshot.children) {
                        val category = data.getValue(Category::class.java)
                        category?.let { list.add(it) }
                    }
                    binding.rvCategories.adapter = HomeCategoryAdapter(list, { category->

                        if (category.categoryId.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), "Invalid category", Toast.LENGTH_SHORT).show()
                            return@HomeCategoryAdapter
                        }

                        // 🔹 Pass categoryId to ProductsFragment
                        val bundle = Bundle().apply {
                            putString("categoryId", category.categoryId)
                        }

                        findNavController().navigate(
                            R.id.productsFragment,
                            bundle
                        )

                    })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Stop the timer when the fragment is gone
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(scrollRunnable)
        _binding = null
    }
}

