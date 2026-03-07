package com.o7solutions.student_project_bakingo.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.carousel.CarouselLayoutManager
import com.o7solutions.student_project_bakingo.Adapters.CarouselAdapter
import com.o7solutions.student_project_bakingo.CarouselItem
import com.o7solutions.student_project_bakingo.R
import com.o7solutions.student_project_bakingo.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var recyclerView: RecyclerView
    private val handler = Handler(Looper.getMainLooper())
    private var currentPosition = 0


//    cake images
    private val imageList = listOf(
        CarouselItem(R.drawable.cake_one),
        CarouselItem(R.drawable.cake_two),
        CarouselItem(R.drawable.cake_three)
    )

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (imageList.isNotEmpty()) {
                currentPosition = (currentPosition + 1) % imageList.size
                recyclerView.smoothScrollToPosition(currentPosition)
                handler.postDelayed(this, 3000)
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        cake carousel
        val recyclerView = view.findViewById<RecyclerView>(R.id.carouselRecyclerView)
        recyclerView.layoutManager = CarouselLayoutManager()
        recyclerView.adapter = CarouselAdapter(imageList)
    }

}