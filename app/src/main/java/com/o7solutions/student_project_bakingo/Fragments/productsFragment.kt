package com.o7solutions.student_project_bakingo.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.o7solutions.student_project_bakingo.Adapters.ProductAdapter
import com.o7solutions.student_project_bakingo.Product
import com.o7solutions.student_project_bakingo.R
import com.o7solutions.student_project_bakingo.databinding.FragmentProductsBinding


class productsFragment : Fragment(), ProductAdapter.OnItemClickListener {
    private lateinit var binding: FragmentProductsBinding
    private lateinit var productAdapter: ProductAdapter
    private val productList = ArrayList<Product>()

    private var categoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryId = arguments?.getString("categoryId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        setupRecycler()
        fetchProducts()
    }

    private fun setupRecycler() {

        productAdapter = ProductAdapter(productList) { product ->

            // 🔹 Pass ALL product data
            val bundle = Bundle().apply {
                putString("id", product.time)
                putString("name", product.name)
                putString("price", product.price)
                putString("description", product.description)
                putString("categoryId", product.categoryId)
                putStringArrayList("images", ArrayList(product.images ?: listOf()))
            }

            findNavController().navigate(
                R.id.viewproductdetail,
                bundle
            )
        }
//        productAdapter = ProductAdapter(productList,this)

        binding.productRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun fetchProducts() {
        FirebaseDatabase.getInstance()
            .getReference("Products")
            .orderByChild("categoryId")
            .equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    productList.clear()

                    for (data in snapshot.children) {
                        val product = data.getValue(Product::class.java)
                        product?.let { productList.add(it) }
                    }

                    productAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onClick(product: Product) {

        var bundle = Bundle().apply {
            putString("id",product.time)
        }

        findNavController().navigate(R.id.viewproductdetail,bundle)
    }
}