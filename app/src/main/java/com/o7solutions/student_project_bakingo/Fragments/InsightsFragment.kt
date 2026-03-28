package com.o7solutions.student_project_bakingo.Fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.*
import com.o7solutions.student_project_bakingo.databinding.FragmentInsightsBinding
import java.text.SimpleDateFormat
import java.util.*

class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseRef = FirebaseDatabase.getInstance().getReference("Orders")
        calculateInsights()
    }

    private fun calculateInsights() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productSalesMap = mutableMapOf<String, Int>()
                val dailyOrdersMap = mutableMapOf<String, Int>()
                var totalProfit = 0.0
                val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

                if (!snapshot.exists()) {
                    updateUI(productSalesMap, 0.0, dailyOrdersMap, "N/A")
                    return
                }

                for (orderSnapshot in snapshot.children) {
                    // Manual extraction to ensure no type-mismatch errors
                    val name = orderSnapshot.child("itemName").value?.toString() ?: "Unknown"
                    val priceRaw = orderSnapshot.child("itemPrice").value?.toString() ?: "0"
                    val timestamp = orderSnapshot.child("timestamp").value as? Long ?: System.currentTimeMillis()

                    // 1. Calculate Profit (Clean the string first)
                    val cleanPrice = priceRaw.replace(Regex("[^0-9.]"), "")
                    val price = cleanPrice.toDoubleOrNull() ?: 0.0
                    totalProfit += price

                    // 2. Track Product Sales
                    productSalesMap[name] = productSalesMap.getOrDefault(name, 0) + 1

                    // 3. Track Daily Orders
                    val dateKey = dateFormat.format(Date(timestamp))
                    dailyOrdersMap[dateKey] = dailyOrdersMap.getOrDefault(dateKey, 0) + 1
                }

                val bestSeller = productSalesMap.maxByOrNull { it.value }?.key ?: "N/A"
                updateUI(productSalesMap, totalProfit, dailyOrdersMap, bestSeller)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateUI(salesData: Map<String, Int>, profit: Double, dailyData: Map<String, Int>, bestSeller: String) {
        if (_binding == null) return

        binding.tvTotalProfit.text = "Total Profit: ₹${String.format("%.2f", profit)}"
        binding.tvBestSeller.text = "Most Sold: $bestSeller"

        setupLineChart(dailyData)
        setupBarChart(salesData)
    }

    private fun setupLineChart(dailyData: Map<String, Int>) {
        val lineEntries = mutableListOf<Entry>()
        val dateLabels = dailyData.keys.sorted() // Sort by date

        dateLabels.forEachIndexed { index, date ->
            lineEntries.add(Entry(index.toFloat(), dailyData[date]!!.toFloat()))
        }

        val dataSet = LineDataSet(lineEntries, "Orders per Day").apply {
            color = Color.parseColor("#FF5722")
            setCircleColor(Color.BLACK)
            lineWidth = 3f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#FFE0B2")
            valueTextSize = 10f
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            description.isEnabled = false
            animateX(1000)
            invalidate()
        }
    }

    private fun setupBarChart(salesData: Map<String, Int>) {
        val barEntries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        var index = 0f
        salesData.forEach { (name, count) ->
            barEntries.add(BarEntry(index, count.toFloat()))
            labels.add(name)
            index++
        }

        val dataSet = BarDataSet(barEntries, "Units Sold").apply {
            colors = listOf(Color.CYAN, Color.GREEN, Color.MAGENTA, Color.LTGRAY)
            valueTextSize = 12f
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            description.isEnabled = false
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}