package com.o7solutions.student_project_bakingo.Fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import com.o7solutions.student_project_bakingo.Category
import com.o7solutions.student_project_bakingo.Product
import com.o7solutions.student_project_bakingo.Test.AppwriteManager
import com.o7solutions.student_project_bakingo.databinding.FragmentAddProductBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddProductFragment : Fragment() {

    private lateinit var binding: FragmentAddProductBinding

    private val categoryList = ArrayList<Category>()
    private val categoryNames = ArrayList<String>()
    private var selectedCategoryId: String? = null

    private val selectedImages = ArrayList<Uri>()

    private val databaseRef by lazy {
        FirebaseDatabase.getInstance().reference
    }

    // Gallery picker for multiple images
    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                selectedImages.clear()
                selectedImages.addAll(uris)
                Toast.makeText(requireContext(), "${uris.size} images selected", Toast.LENGTH_SHORT).show()
                // RecyclerView adapter for preview can be attached here
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        fetchCategories()
        setupClicks()
    }

    // ---------------- Toolbar ----------------
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    // ---------------- Category Dropdown ----------------
    private fun fetchCategories() {
        databaseRef.child("Categories")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoryList.clear()
                    categoryNames.clear()

                    for (data in snapshot.children) {
                        val category = data.getValue(Category::class.java)
                        category?.let {
                            it.categoryId = data.key
                            categoryList.add(it)
                            categoryNames.add(it.name ?: "")
                        }
                    }

                    setupSpinner()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categoryNames
        )
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.setOnItemSelectedListener(object :
            android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategoryId = categoryList[position].categoryId
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })
    }

    // ---------------- Clicks ----------------
    private fun setupClicks() {

        binding.btnPickImages.setOnClickListener {
            pickImagesLauncher.launch("image/*")
        }

        binding.btnSaveProduct.setOnClickListener {
            saveProduct()
        }
    }

    // ---------------- Save Product ----------------
    private fun saveProduct() {

        val name = binding.etProductName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (name.isEmpty() || price.isEmpty() || selectedCategoryId == null) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {

            try {

                val appwriteManager = AppwriteManager.getInstance(requireContext())

                val imageUrls = ArrayList<String>()

                // Upload images one by one
                for (uri in selectedImages) {
                    val url = appwriteManager.uploadImageFromUri(uri)
                    imageUrls.add(url)
                }

                val productId = databaseRef.child("Products").push().key ?: return@launch

                val time = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

                val product = Product(
                    name = name,
                    categoryId = selectedCategoryId,
                    price = price,
                    description = description,
                    time = time,
                    sellCount = 0,
                    images = imageUrls
                )

                databaseRef.child("Products").child(productId)
                    .setValue(product)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Product saved", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save product", Toast.LENGTH_SHORT).show()
                    }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Image upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }

        }
    }}